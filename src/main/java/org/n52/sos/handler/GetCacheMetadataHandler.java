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
package org.n52.sos.handler;

import java.io.FileNotFoundException;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.ows.ExceptionReport;
import org.n52.sos.cache.AbstractEntityCache;
import org.n52.sos.cache.AbstractCacheScheduler;
import org.n52.sos.cache.DummyCache;
import org.n52.sos.cache.ObservationOfferingCache;
import org.n52.sos.db.AccessGDB;
import org.n52.util.CommonUtilities;
import org.n52.util.VersionInfo;

import com.esri.arcgis.server.json.JSONObject;

public class GetCacheMetadataHandler implements OperationRequestHandler {
	
	DateTimeFormatter format = ISODateTimeFormat.dateTimeNoMillis();

	@Override
	public int compareTo(OperationRequestHandler o) {
		return this.getExecutionPriority() - o.getExecutionPriority();
	}

	@Override
	public boolean canHandle(String operationName) {
		return "GetCacheMetadata".equals(operationName);
	}

	@Override
	public byte[] invokeOGCOperation(AccessGDB geoDB, JSONObject inputObject,
			String[] responseProperties) throws ExceptionReport {
		JSONObject result = new JSONObject();
		
		AbstractCacheScheduler cache = AbstractCacheScheduler.Instance.instance();
		if (cache != null) {
			for (AbstractEntityCache<?> aec : cache.getCandidates()) {
				JSONObject candidateObject = new JSONObject();
				long lastUpdate = aec.lastUpdated();
				candidateObject.put("lastUpdated", format.print(lastUpdate));
				candidateObject.put("lastUpdatedUnixTimestamp", lastUpdate / 1000);
				candidateObject.put("lastUpdateDuration", aec.getLastUpdateDuration());
				candidateObject.put("maximumEntries", aec.getMaximumEntries());
				candidateObject.put("latestEntryIndex", aec.getLatestEntryIndex());
				
				String className = aec.getClass().getSimpleName();
				if (className.equals(DummyCache.class.getSimpleName())) {
					className = ObservationOfferingCache.class.getSimpleName();
				}
				
				result.put(className, candidateObject);
			}

			result.put("currentlyLocked", cache.isCurrentyLocked());
			result.put("updateCacheOnStartup", cache.isUpdateCacheOnStartup());
			try {
				result.put("cacheBaseDir", CommonUtilities.resolveCacheBaseDir(geoDB.getDatabaseName()));
			}
			catch (FileNotFoundException e) {
				result.put("cacheBaseDir", "n/a");
			}
		}
		
		result.put("VersionInfo", new VersionInfo().toString());
		
		return result.toString().getBytes();
	}

	@Override
	public void initialize(String urlSosExtension) {
	}

	@Override
	public int getExecutionPriority() {
		return 0;
	}

}
