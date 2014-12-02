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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.n52.oxf.valueDomains.time.ITimePeriod;
import org.n52.sos.dataTypes.EnvelopeWrapper;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.db.AccessGDB;
import org.n52.util.logging.Logger;

public class DummyCache extends AbstractEntityCache<ObservationOffering> {

	public static Logger LOGGER = Logger.getLogger(DummyCache.class.getName());
	
	public DummyCache(String dbName) throws FileNotFoundException {
		super(dbName);
	}

	private static final String TOKEN_SEP = "@@";
	private static DummyCache instance;

	public static synchronized DummyCache instance(String dbName) throws FileNotFoundException {
		if (instance == null) {
			instance = new DummyCache(dbName);
		}
		
		return instance;
	}
	

	@Override
	protected String getCacheFileName() {
		return "observationOfferingsList.cache";
	}

	@Override
	protected String serializeEntity(ObservationOffering entity) throws CacheException {
		StringBuilder sb = new StringBuilder();
		
		sb.append(entity.getId());
		sb.append(TOKEN_SEP);
		sb.append(entity.getName());
		sb.append(TOKEN_SEP);
		sb.append(entity.getProcedureIdentifier());
		sb.append(TOKEN_SEP);
		try {
			sb.append(EnvelopeEncoderDecoder.encode(entity.getObservedArea()));
		} catch (IOException e) {
			throw new CacheException(e);
		}
		sb.append(TOKEN_SEP);
		sb.append(Arrays.toString(entity.getObservedProperties()));
		sb.append(TOKEN_SEP);
		sb.append(TimePeriodEncoder.encode(entity.getTimeExtent()));
		
		return sb.toString();
	}

	@Override
	protected ObservationOffering deserializeEntity(String line) {
		String[] values = line.split(TOKEN_SEP);
		
		if (values == null || values.length != 6) {
			return null;
		}
		
		String id = values[0].trim();
		String name = values[1].trim();
		String proc = values[2].trim();
		EnvelopeWrapper env = EnvelopeEncoderDecoder.decode(values[3]);
		String[] props = decodeStringArray(values[4]);
		ITimePeriod time = TimePeriodEncoder.decode(values[5]);
		
		return new ObservationOffering(id, name, props, proc, env, time);
	}

	protected Collection<ObservationOffering> getCollectionFromDAO(AccessGDB geoDB)
			throws IOException {
		clearTempCacheFile();
		
		InputStream res = getClass().getResourceAsStream("/oo-dummy-cache.cache");
		BufferedReader br = new BufferedReader(new InputStreamReader(res));
		
		List<ObservationOffering> result = new ArrayList<>();
		
		while (br.ready()) {
			result.add(deserializeEntity(br.readLine()));
			
			LOGGER.info("Added Observation #"+result.size());
			
			try {
				Thread.sleep(1000 * 13);
			} catch (InterruptedException e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}
		
		return result;
	}

	@Override
	protected AbstractEntityCache<ObservationOffering> getSingleInstance() {
		return instance;
	}


	@Override
	public void cancelCurrentExecution() {
	}

}
