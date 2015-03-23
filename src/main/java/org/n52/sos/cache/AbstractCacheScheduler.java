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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.n52.sos.cache.quartz.QuartzCacheScheduler;
import org.n52.sos.db.AccessGDB;
import org.n52.util.CommonUtilities;
import org.n52.util.logging.Logger;

public abstract class AbstractCacheScheduler {
	
	private static final Logger LOGGER = Logger.getLogger(AbstractCacheScheduler.class.getName());
	
	public static final long ONE_HOUR_MS = 1000 * 60 * 60;
	public static final long FIFTEEN_MINS_MS = 1000 * 60 * 15;
	
	private List<AbstractEntityCache<?>> candidates = new ArrayList<>();
	private boolean updateCacheOnStartup;
	private AccessGDB geoDB;

	private File lockFile;

	private LocalTime cacheUpdateTime;

	public AbstractCacheScheduler(AccessGDB geoDB, boolean updateCacheOnStartup, LocalTime cacheUpdateTime) {
		this.geoDB = geoDB;
		this.updateCacheOnStartup = updateCacheOnStartup;
		this.cacheUpdateTime = cacheUpdateTime;
		
		String dbName;
		dbName = resolveDatabaseName(geoDB);
		
		/*
		 * first use the PUMC, others might depend on it
		 */
		try {
			candidates.add(PropertyUnitMappingCache.instance(dbName));
		} catch (FileNotFoundException e) {
			LOGGER.warn(e.getMessage(), e);
		}

		try {
			candidates.add(ObservationOfferingCache.instance(dbName));
		} catch (FileNotFoundException e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

	private String resolveDatabaseName(AccessGDB geoDB) {
		String dbName;
		if (geoDB != null) {
			dbName = geoDB.getDatabaseName();
		}
		else {
			dbName = "Airquality_E2a";
		}
		return dbName;
	}

	protected AccessGDB getGeoDB() {
		return this.geoDB;
	}

	public abstract void shutdown();
	
	public List<AbstractEntityCache<?>> getCandidates() {
		return candidates;
	}

	public boolean isUpdateCacheOnStartup() {
		return updateCacheOnStartup;
	}
	
	public LocalTime getCacheUpdateTime() {
		return this.cacheUpdateTime;
	}
	
	protected void freeCacheUpdateLock() throws IOException {
		synchronized (AbstractCacheScheduler.class) {
			File lockFile = resolveCacheLockFile();
			
			if (lockFile.exists()) {
				lockFile.delete();
			}	
		}
	}

	protected synchronized File resolveCacheLockFile() throws FileNotFoundException {
		if (lockFile == null) {
			File dir = CommonUtilities.resolveCacheBaseDir(resolveDatabaseName(geoDB));
			lockFile = new File(dir, "cache.lock");	
		}
		
		return lockFile;
	}

	public abstract void forceUpdate();
	
	public boolean isCurrentyLocked() {
		synchronized (AbstractCacheScheduler.class) {
			File lockFile;
			try {
				lockFile = resolveCacheLockFile();
			} catch (FileNotFoundException e) {
				return false;
			}
			
			return lockFile.exists();
		}
	}
	
	protected boolean retrieveCacheUpdateLock() throws IOException {
		synchronized (AbstractCacheScheduler.class) {
			File lockFile = resolveCacheLockFile();
			
			if (!lockFile.exists()) {
				boolean worked = lockFile.createNewFile();
				if (worked) {
					return true;
				}
				else {
					LOGGER.info("Could not create cache.lock file!");
					return false;
				}
			}
		}
		
		return false;
	}
	
	public MutableDateTime resolveNextScheduleDate(LocalTime localTime, DateTime referenceTime) {
		/*
		 * every 4am, starting with next
		 */
		MutableDateTime mdt = referenceTime.toMutableDateTime();
		mdt.setHourOfDay(localTime.getHourOfDay());
		mdt.setMinuteOfHour(localTime.getMinuteOfHour());
		mdt.setSecondOfMinute(localTime.getSecondOfMinute());
		
		if (!referenceTime.isBefore(mdt)) {
			mdt.addDays(1);
		}
		
		Random random = new Random();
		mdt.addSeconds(random.nextInt(11)*2);
		return mdt;
	}
	

	protected List<AbstractEntityCache<?>> cacheUpdateRequired() throws FileNotFoundException {
		List<AbstractEntityCache<?>> result = new ArrayList<>();
		
		for (AbstractEntityCache<?> cand : getCandidates()) {
			if (cand.requiresUpdate()) {
				result.add(cand);
			}
		}
		
		return result;
	}
	
	public static class Instance {
		
		private static AbstractCacheScheduler instance;

		public static synchronized AbstractCacheScheduler init(AccessGDB geoDB, boolean updateCacheOnStartup, LocalTime cacheUpdateTime) {
			if (instance == null) {
				instance = new QuartzCacheScheduler(geoDB, updateCacheOnStartup, cacheUpdateTime);
			}
			return instance;
		}
		
		public static synchronized AbstractCacheScheduler instance() {
			return instance;
		}
		
	}

}
