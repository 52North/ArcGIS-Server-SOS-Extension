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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.n52.sos.db.AccessGDB;
import org.n52.util.logging.Logger;

public class CacheScheduler {
	
	public static Logger LOGGER = Logger.getLogger(CacheScheduler.class.getName());

	private static final long PERIOD = 1000 * 60 * 60;

	public static final long MINIMUM_UPDATE_DELTA = 1000 * 60 * 15;
	private Timer timer;
	private AccessGDB geoDB;
	
	
	static List<AbstractEntityCache<?>> candidates = new ArrayList<>();
	
	static {
		try {
			candidates.add(ObservationOfferingCache.instance());
		} catch (FileNotFoundException e) {
			LOGGER.warn(e.getMessage(), e);
		}
		
		try {
			candidates.add(PropertyUnitMappingCache.instance());
		} catch (FileNotFoundException e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

	public CacheScheduler(AccessGDB geoDB, boolean updateCacheOnStartup) {
		this.geoDB = geoDB;
		this.timer = new Timer(true);
		
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
					this.timer.schedule(new UpdateCacheTask(requiresUpdates), 0);	
				}
				else {
					LOGGER.info("No cache update required. Last update not longer ago than ms "+MINIMUM_UPDATE_DELTA);
				}
			} catch (FileNotFoundException e) {
				LOGGER.warn(e.getMessage(), e);
				LOGGER.warn("could not initialize cache. disabling scheduled updates.");
				return;
			}			
		}
		
		/*
		 * every midnight
		 */
		Calendar c = new GregorianCalendar();
		c.add(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		this.timer.scheduleAtFixedRate(new UpdateCacheTask(candidates), c.getTime(), PERIOD * 24);
		
		LOGGER.info("Next scheduled cache update: "+c.getTime().toString());
	}

	private List<AbstractEntityCache<?>> cacheUpdateRequired() throws FileNotFoundException {
		List<AbstractEntityCache<?>> result = new ArrayList<>();
		
		for (AbstractEntityCache<?> cand : candidates) {
			if (cand.requiresUpdate()) {
				result.add(cand);
			}
		}
		
		return result;
	}

	public void shutdown() {
		this.timer.cancel();
		try {
			ObservationOfferingCache.instance().freeUpdateLock();
		} catch (FileNotFoundException e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}
	
	private class UpdateCacheTask extends TimerTask {

		private List<AbstractEntityCache<?>> candidates;

		public UpdateCacheTask(List<AbstractEntityCache<?>> candidates) {
			this.candidates = candidates;
		}

		@Override
		public void run() {
			try {
				LOGGER.info("update cache... using thread "+ Thread.currentThread().getName());
				
				for (AbstractEntityCache<?> aec : this.candidates) {
					aec.updateCache(geoDB);
					LOGGER.info("cache updated: "+aec.getClass());
				}
				
				LOGGER.info("all caches updated!");
			} catch (IOException e) {
				LOGGER.warn(e.getMessage(), e);
			} catch (CacheException e) {
				LOGGER.warn(e.getMessage(), e);
			} catch (RuntimeException e) {
				LOGGER.warn(e.getMessage(), e);
			}
			
		}
		
	}

}
