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
package org.n52.sos.cache.quartz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.n52.sos.cache.AbstractEntityCache;
import org.n52.sos.cache.CacheException;
import org.n52.sos.cache.AbstractCacheScheduler;
import org.n52.sos.db.AccessGDB;
import org.n52.util.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

public class QuartzCacheScheduler extends AbstractCacheScheduler {
	
	public static Logger LOGGER = Logger.getLogger(QuartzCacheScheduler.class.getName());

	public long lastSchedulerThread = Long.MIN_VALUE;

	private Scheduler quartz;

	private LocalJobFactory quartzFactory;
	
//	@Override
//	public List<AbstractEntityCache<?>> getCandidates() {
//		List<AbstractEntityCache<?>> result = new ArrayList<>();
//		try {
//			result.add(new DummyCache());
//		} catch (FileNotFoundException e) {
//			LOGGER.warn(e.getMessage(), e);
//		}
//		return result;
//	}

	public QuartzCacheScheduler(AccessGDB geoDB, boolean updateCacheOnStartup) {
		super(geoDB, updateCacheOnStartup);
		
		try {
			this.quartz = new StdSchedulerFactory().getScheduler();
			this.quartzFactory = new LocalJobFactory();
			this.quartz.setJobFactory(this.quartzFactory);
			this.quartz.start();
		} catch (SchedulerException e) {
			LOGGER.warn("Could not initialize cache scheduling", e);
			return;
		}
		
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
					schedule(new UpdateCacheTask(requiresUpdates), 0);	
				}
				else {
					LOGGER.info("No cache update required. Last update not longer ago than minutes "+FIFTEEN_MINS_MS/(1000*60));
				}
			} catch (FileNotFoundException | SchedulerException e) {
				LOGGER.warn(e.getMessage(), e);
				LOGGER.warn("could not initialize cache. disabling scheduled updates.");
				return;
			}			
		}
		
		MutableDateTime mdt = new DateTime().plusMinutes(2).toMutableDateTime();
		DateTime now = new DateTime();
		
		try {
			schedule(new UpdateCacheTask(getCandidates()), mdt.getMillis() - now.getMillis(),
					ONE_HOUR_MS / 3);
		} catch (SchedulerException e) {
			LOGGER.warn(e.getMessage(), e);
		}
		
		LOGGER.severe("Next scheduled cache update: "+mdt.toString());
		
		/*
		 * start ONE monitoring after 30 minutes and check if the .lock file
		 * is older than 30 minutes -> an artifact .lock file!!
		 */
		try {
			schedule(new MonitorCacheTask(ONE_HOUR_MS/2), ONE_HOUR_MS/60);
		} catch (SchedulerException e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

	private void schedule(NamedJob updateCacheTask, long delay) throws SchedulerException {
		schedule(updateCacheTask, delay, 0);
	}
	
	private void schedule(NamedJob job, long delay, long period) throws SchedulerException {
		JobDetailImpl jobDetail = new JobDetailImpl();
		jobDetail.setKey(new JobKey(job.getName()));
		jobDetail.setJobClass(job.getClass());
		
		this.quartzFactory.register(job.getName(), job, period == 0 ? false : true);
		
		TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger()
				.withIdentity(jobDetail.getKey().getName().concat("_trigger"));
		
		if (delay != 0) {
			Date delayDate = new Date();
			delayDate = new Date(delayDate.getTime() + delay);
			builder = builder.startAt(delayDate);
		}
		else {
			builder = builder.startNow();
		}
		
		if (period != 0) {
			builder.withSchedule(
					SimpleScheduleBuilder.simpleSchedule()
							.withIntervalInMilliseconds(period)
							.repeatForever());
		}
		
		Trigger trigger = builder.build();

		try {
			this.quartz.scheduleJob(jobDetail, trigger);
			LOGGER.info(
					String.format("Added Job '%s' (%s) to scheduling. Next execution at %s; Recurring: %s",
							jobDetail.getKey().getName(),
							job.getClass().getSimpleName(),
							trigger.getStartTime(),
							period));
		} catch (SchedulerException e) {
			LOGGER.warn(e.getMessage(), e);
			throw e;
		}
	}
	

	public void forceUpdate() {
		if (isCurrentyLocked()) {
			LOGGER.info("chache updating locked. skipping");
			return;
		}
		else {
			try {
				schedule(new UpdateCacheTask(getCandidates()), 0);
			} catch (SchedulerException e) {
				LOGGER.warn(e.getMessage(), e);
			}		
		}
	}

	@Override
	public void shutdown() {
		try {
			this.quartz.shutdown();
			freeCacheUpdateLock();
		} catch (IOException | SchedulerException e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}
	
	private String createStackTrace(StackTraceElement[] stackTrace) {
		if (stackTrace == null || stackTrace.length == 0) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		
		String sep = System.getProperty("line.separator");
		for (StackTraceElement ste : stackTrace) {
			sb.append(ste.toString());
			sb.append(sep);
		}
		
		return sb.toString();
	}
	
	private abstract class NamedJob implements Job {
		
		String uuid = UUID.randomUUID().toString();
		
		public String getName() {
			return uuid;
		}
		
	}
	
	private class UpdateCacheTask extends NamedJob {

		private List<AbstractEntityCache<?>> candidates;

		public UpdateCacheTask(List<AbstractEntityCache<?>> candidates) {
			this.candidates = candidates;
		}

		@Override
		public void execute(JobExecutionContext arg0)
				throws JobExecutionException {
			
			try {
				schedule(new MonitorCacheTask(ONE_HOUR_MS/2), ONE_HOUR_MS/2);
			} catch (SchedulerException e) {
				writeExceptionFile(e);
			}
			
			/*
			 * do a cache.lock check now and do it sequentially
			 */
			MonitorCacheTask freeTask = new MonitorCacheTask(ONE_HOUR_MS/2);
			freeTask.execute(null);
			
			final AtomicBoolean running = new AtomicBoolean(true);
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						doCacheUpdate();
					}
					catch (RuntimeException e) {
						LOGGER.warn(e.getMessage(), e);
					}
					catch (Throwable t) {
						LOGGER.warn(t.getMessage(), t);
						running.getAndSet(false);
						throw t;
					}
					running.getAndSet(false);
				}
			});
			
			t.setDaemon(true);
			t.start();
			
			long start = System.currentTimeMillis();
			while (running.get()) {
				if (System.currentTimeMillis()-start > 1000 * 60 * 10) {
					LOGGER.warn("update thread taking more than 10 minutes...");
					Map<Thread, StackTraceElement[]> stacks = Collections.singletonMap(t, t.getStackTrace());
					dumpAllThreads(stacks);
				}
				else if (System.currentTimeMillis()-start > 1000 * 60 * 30) {
					LOGGER.warn("update thread took more than 30 minutes... cancelling");
					try {
						freeCacheUpdateLock();
					} catch (IOException e) {
						LOGGER.warn(e.getMessage(), e);
					}
					return;
				}
				else {
					LOGGER.info("still waiting for update thread to finish...");
				}
				
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					LOGGER.warn(e.getMessage(), e);
				}
			}
			
			LOGGER.info("update thread finished...");
		}
		
		private void doCacheUpdate() {
			/*
			 * cache updates can take very long. it has been observed
			 * on some SOE instances that the update got stuck and did
			 * not continue, leaving a stale cache.lock file behind.
			 * 
			 * this monitor takes care of cleaning up a staled lock file
			 * after a certain amount of time
			 */
			try {
				
				if (!retrieveCacheUpdateLock()) {
					LOGGER.info("chache updating locked. skipping");
					return;
				}

				QuartzCacheScheduler.this.lastSchedulerThread = Thread.currentThread().getId();
				LOGGER.info("update cache... using thread "+ lastSchedulerThread);
				
				for (AbstractEntityCache<?> aec : this.candidates) {
					aec.updateCache(getGeoDB());
				}
				
				freeCacheUpdateLock();
				
				LOGGER.info("all caches updated!");					
			} catch (IOException | CacheException | RuntimeException e) {
//				LOGGER.warn(e.getMessage(), e);
				writeExceptionFile(e);
			} catch (Throwable t) {
//				LOGGER.severe("Unrecoverable error", t);
				writeExceptionFile(t);
				throw t;
			}
		}

		private void writeExceptionFile(Throwable e) {
			FileOutputStream fos = null;
			try {
				File p = resolveCacheLockFile().getParentFile();
				fos = new FileOutputStream(new File(p, "exception-"+ UUID.randomUUID().toString().substring(0, 6)+".log"), false);
				fos.write((e.getClass() +" "+createStackTrace(e.getStackTrace())).getBytes());
				
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		
		

	}
	
	private class MonitorCacheTask extends NamedJob {
		
		private long maximumAge = Long.MIN_VALUE;

		
		/**
		 * @param maximumAge delete only a file that is older than this age
		 */
		public MonitorCacheTask(long maximumAge) {
			this.maximumAge = maximumAge;
		}
		
		@Override
		public void execute(JobExecutionContext arg0)
				throws JobExecutionException {
			LOGGER.info(String.format("Monitoring cache update using thread %s; considers .lock age? %s",
					Thread.currentThread().getId(),
					maximumAge != Long.MIN_VALUE));
			
			Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
			
			Thread target = null;
			
			/*
			 * try to find the 
			 */
			if (QuartzCacheScheduler.this.lastSchedulerThread != Long.MIN_VALUE) {
				for (Thread t : stacks.keySet()) {
					if (t.getId() == QuartzCacheScheduler.this.lastSchedulerThread) {
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
				LOGGER.info("Could not find lastSchedulerThread in current stack traces");
			}
			
			boolean isLocked = true;
			
			isLocked = isCurrentyLocked();
			if (isLocked) {
//				if (target != null && (target.getState() == Thread.State.TIMED_WAITING
//						|| target.getState() == Thread.State.WAITING)) {
//					LOGGER.warn("The cache update may have taken too long. trying to interrupt cache update.");
//					target.interrupt();
//				}
				
//				
//				dumpAllThreads(stacks);
				
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
					else if (QuartzCacheScheduler.this.lastSchedulerThread != Long.MIN_VALUE) {
						/*
						 * only free if this QuartzCacheScheduler instance (singleton in an SOE instance)
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

	}
	
	private class LocalJobFactory implements JobFactory {

		private final Map<String, Job> jobs = new HashMap<>();
		private final Set<String> recurring = new HashSet<>();

		@Override
		public synchronized Job newJob(TriggerFiredBundle bundle, Scheduler scheduler)
				throws SchedulerException {
			String name = bundle.getJobDetail().getKey().getName();
			Job result = jobs.get(name);
			
			if (!recurring.contains(name)) {
				jobs.remove(name);
			}
			
			return result;
		}
		
		public synchronized void register(String name, Job job, boolean recurring) {
			jobs.put(name, job);
			if (recurring) {
				this.recurring.add(name);
			}
		}
		
	}

	protected void dumpAllThreads(Map<Thread, StackTraceElement[]> stacks) {
		try {
			String p = resolveCacheLockFile().getParent();
			FileOutputStream fos = new FileOutputStream(new File(p, "threaddump-"+ UUID.randomUUID().toString().substring(0, 6)+".log"), true);
			byte[] sep = System.getProperty("line.separator").getBytes();
			for (Thread t : stacks.keySet()) {
				fos.write("###### ".getBytes());
				fos.write(new DateTime().toString().getBytes());
				fos.write(sep);
				fos.write((t.getName() +" "+ t.getId() +" "+ t.getState()).getBytes());
				fos.write(sep);
				for (StackTraceElement ste : stacks.get(t)) {
					fos.write(ste.toString().getBytes());
					fos.write(sep);
				}
				fos.write(sep);
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
}
