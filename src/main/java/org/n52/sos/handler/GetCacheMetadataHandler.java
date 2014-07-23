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
import org.n52.sos.cache.AbstractEntityCache;
import org.n52.sos.cache.CacheScheduler;
import org.n52.sos.db.AccessGDB;
import org.n52.util.CommonUtilities;

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
			String[] responseProperties) throws Exception {
		JSONObject result = new JSONObject();
		
		CacheScheduler cache = CacheScheduler.instance();
		if (cache != null) {
			for (AbstractEntityCache<?> aec : cache.getCandidates()) {
				JSONObject candidateObject = new JSONObject();
				long lastUpdate = aec.lastUpdated();
				candidateObject.put("lastUpdated", format.print(lastUpdate));
				candidateObject.put("lastUpdatedUnixTimestamp", lastUpdate / 1000);
				candidateObject.put("lastUpdateDuration", aec.getLastUpdateDuration());
				result.put(aec.getClass().getSimpleName(), candidateObject);
			}
			
			result.put("updateCacheOnStartup", cache.isUpdateCacheOnStartup());
			try {
				result.put("cacheBaseDir", CommonUtilities.resolveCacheBaseDir());
			}
			catch (FileNotFoundException e) {
				result.put("cacheBaseDir", "n/a");
			}
		}
		
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