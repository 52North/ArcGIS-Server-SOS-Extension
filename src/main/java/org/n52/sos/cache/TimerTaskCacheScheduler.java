/**
 * Copyright (C) 2012 52°North Initiative for Geospatial Open Source Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.sos.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.n52.sos.db.AccessGDB;
import org.n52.util.logging.Logger;

public class TimerTaskCacheScheduler extends AbstractCacheScheduler {
	
	public static Logger LOGGER = Logger.getLogger(TimerTaskCacheScheduler.class.getName());

	private static TimerTaskCacheScheduler instance;

	private static final long ONE_HOUR_MS = 1000 * 60 * 60;
	public static final long FIFTEEN_MINS_MS = 1000 * 60 * 15;
	
	private Timer cacheTimer;
	
	public long lastSchedulerThread = Long.MIN_VALUE;

	private Timer monitorTimer;

	public static synchronized void init(AccessGDB geoDB, boolean updateCacheOnStartup, LocalTime lt) {
		if (instance == null) {
			instance = new TimerTaskCacheScheduler(geoDB, updateCacheOnStartup, lt);
		}
	}
	
	public static synchronized TimerTaskCacheScheduler instance() {
		return instance;
	}
	
	private TimerTaskCacheScheduler(AccessGDB geoDB, boolean updateCacheOnStartup, LocalTime lt) {
		super(geoDB, updateCacheOnStartup, lt);
		
		this.cacheTimer = new Timer(true);
		this.monitorTimer = new Timer(true);
		
		if (!updateCacheOnStartup) {
			LOGGER.info("Update cache on startup disabled!");
		}
		else {
			try {
				List<AbstractEntityCache<?>> requiresUpdates = cacheUpdateRequired();
				if (!requiresUpdates.isEmpty()) {
					LOGGER.info(String.format("Cache update required for: %s", requiresUpdates.toString()));
					/*
					 * now
					 */
					this.cacheTimer.schedule(new UpdateCacheTask(requiresUpdates), 0);	
				}
				else {
					LOGGER.info("No cache update required. Last update not longer ago than minutes "+FIFTEEN_MINS_MS/(1000*60));
				}
			} catch (FileNotFoundException e) {
				LOGGER.warn(e.getMessage(), e);
				LOGGER.warn("could not initialize cache. disabling scheduled updates.");
				return;
			}			
		}
		
		MutableDateTime mdt = resolveNextScheduleDate(lt, new DateTime());
		
		this.cacheTimer.scheduleAtFixedRate(new UpdateCacheTask(getCandidates()), mdt.toDate(), ONE_HOUR_MS * 24);
		
//		Calendar c = new GregorianCalendar();
//		c.add(Calendar.MINUTE, 5);
//		
//		this.cacheTimer.scheduleAtFixedRate(new UpdateCacheTask(candidates), c.getTime(), ONE_HOUR_MS/2);
		
		LOGGER.severe("Next scheduled cache update: "+mdt.toString());
		
		/*
		 * start ONE monitoring after 30 minutes and check if the .lock file
		 * is older than 30 minutes -> an artifact .lock file!!
		 */
		this.monitorTimer.schedule(new MonitorCacheTask(ONE_HOUR_MS/2), ONE_HOUR_MS/60);
	}


	public void shutdown() {
		this.cacheTimer.cancel();
		this.monitorTimer.cancel();
		try {
			freeCacheUpdateLock();
		} catch (IOException e) {
			LOGGER.warn(e.getMessage(), e);
		}
//		for (AbstractEntityCache<?> aec : candidates) {
//			try {
//				aec.getSingleInstance().freeUpdateLock();
//			} catch (FileNotFoundException e) {
//				LOGGER.warn(e.getMessage(), e);
//			}	
//		}
		
	}
	

	public void forceUpdate() {
		if (isCurrentyLocked()) {
			LOGGER.info("chache updating locked. skipping");
			return;
		}
		else {
			this.cacheTimer.schedule(new UpdateCacheTask(getCandidates()), new Date());		
		}
	}
	
	
	private class UpdateCacheTask extends TimerTask {

		private List<AbstractEntityCache<?>> candidates;

		public UpdateCacheTask(List<AbstractEntityCache<?>> candidates) {
			this.candidates = candidates;
		}

		@Override
		public void run() {
			/*
			 * cache updates can take very long. it has been observed
			 * on some SOE instances that the update got stuck and did
			 * not continue, leaving a stale cache.lock file behind.
			 * 
			 * this monitor takes care of cleaning up a staled lock file
			 * after a certain amount of time
			 */
			TimerTaskCacheScheduler.this.monitorTimer.schedule(new MonitorCacheTask(ONE_HOUR_MS/2), ONE_HOUR_MS/2);
			
			try {
				if (!retrieveCacheUpdateLock()) {
					LOGGER.info("chache updating locked. skipping");
					return;
				}

				TimerTaskCacheScheduler.this.lastSchedulerThread = Thread.currentThread().getId();
				LOGGER.info("update cache... using thread "+ lastSchedulerThread);
				
				for (AbstractEntityCache<?> aec : this.candidates) {
					aec.updateCache(getGeoDB());
				}
				
				freeCacheUpdateLock();
				
				LOGGER.info("all caches updated!");					
			} catch (IOException | CacheException | RuntimeException e) {
				LOGGER.warn(e.getMessage(), e);
			}
			
		}

		
	}
	
	private class MonitorCacheTask extends TimerTask {
		
		private long maximumAge = Long.MIN_VALUE;

		
		/**
		 * @param maximumAge delete only a file that is older than this age
		 */
		public MonitorCacheTask(long maximumAge) {
			this.maximumAge = maximumAge;
		}
		
		@Override
		public void run() {
			LOGGER.info(String.format("Monitoring cache update using thread %s; considers .lock age? %s",
					Thread.currentThread().getId(),
					maximumAge != Long.MIN_VALUE));
			
			Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
			
			Thread target = null;
			
			/*
			 * try to find the 
			 */
			if (TimerTaskCacheScheduler.this.lastSchedulerThread != Long.MIN_VALUE) {
				for (Thread t : stacks.keySet()) {
					if (t.getId() == TimerTaskCacheScheduler.this.lastSchedulerThread) {
						target = t;
						break;
					}
				}		
			}
			
			if (target != null) {
				State state = target.getState();
				LOGGER.info("lastSchedulerThread status: " + state);
				LOGGER.info("lastSchedulerThread isalive: " + target.isAlive());
				LOGGER.info("lastSchedulerThread isinterrupted: " + target.isInterrupted());
				
				if (state != State.RUNNABLE && state != State.BLOCKED) {
					LOGGER.info(String.format("Updater thread's stack: %s", createStackTrace(target.getStackTrace())));
				}
			}
			else {
				LOGGER.warn("Could not find lastSchedulerThread in current stack traces");
			}
			
			boolean isLocked = true;
			
			isLocked = isCurrentyLocked();
			if (isLocked) {
				if (target != null && (target.getState() == Thread.State.TIMED_WAITING
						|| target.getState() == Thread.State.WAITING)) {
					LOGGER.warn("The cache update may have taken too long. trying to interrupt cache update.");
					target.interrupt();
				}
				
				try {
					if (this.maximumAge != Long.MIN_VALUE) {
						LOGGER.info("Resolving age of cache.lock file");
						File f = resolveCacheLockFile();
						if (System.currentTimeMillis() - f.lastModified() > this.maximumAge) {
							LOGGER.info("Trying to remove artifact cache.lock file due to its age");
							freeCacheUpdateLock();	
						}
						else {
							LOGGER.info("cache.lock to young, an update might be in progress.");
						}
					}
					else if (TimerTaskCacheScheduler.this.lastSchedulerThread != Long.MIN_VALUE) {
						/*
						 * only free if this CacheScheduler instance (singleton in an SOE instance)
						 * has already started a cache update. otherwise lastSchedulerThread has
						 * not been set yet
						 */
						LOGGER.info("freeing cache.lock");
						freeCacheUpdateLock();
					}
				}
				catch (IOException e) {
					LOGGER.warn(e.getMessage(), e);
				}
				
			}
			else {
				LOGGER.info("No stale cache.lock file found.");
			}
			
		}

		private String createStackTrace(StackTraceElement[] stackTrace) {
			StringBuilder sb = new StringBuilder();
			
			String sep = System.getProperty("line.separator");
			for (StackTraceElement ste : stackTrace) {
				sb.append(ste.toString());
				sb.append(sep);
			}
			
			return sb.toString();
		}
		
	}

	
}
