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

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.db.AccessGDB;
import org.n52.util.logging.Logger;

public class CacheScheduler {
	
	public Logger LOGGER = Logger.getLogger(CacheScheduler.class.getName());

	private static final long PERIOD = 1000 * 60 * 60;
	private Timer timer;
	private AccessGDB geoDB;

	public CacheScheduler(AccessGDB geoDB) {
		this.geoDB = geoDB;
		this.timer = new Timer(true);
		
		/*
		 * now + 5 seconds
		 */
		this.timer.schedule(new UpdateCacheTask(), 5000);
		
		/*
		 * every midnight
		 */
		Calendar c = new GregorianCalendar();
		c.add(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		this.timer.scheduleAtFixedRate(new UpdateCacheTask(), c.getTime(), PERIOD * 24);
		
		LOGGER.info("Next scheduled cache update: "+c.getTime().toString());
	}

	public void shutdown() {
		this.timer.cancel();
	}
	
	private class UpdateCacheTask extends TimerTask {

		@Override
		public void run() {
			try {
				LOGGER.info("update observation offerings cache... using thread "+ Thread.currentThread().getName());
				Collection<ObservationOffering> entities = geoDB.getOfferingAccess().getNetworksAsObservationOfferings();
				ObservationOfferingCache.instance().storeEntityCollection(entities);
				LOGGER.info("observation offerings cache updated!");
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
