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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.n52.oxf.valueDomains.time.ITimePeriod;
import org.n52.sos.dataTypes.EnvelopeWrapper;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.db.AccessGDB;

public class ObservationOfferingCache extends AbstractEntityCache<ObservationOffering> {

	private static final String TOKEN_SEP = "@@";
	private static ObservationOfferingCache instance;

	public static synchronized ObservationOfferingCache instance(String dbName) throws FileNotFoundException {
		if (instance == null) {
			instance = new ObservationOfferingCache(dbName);
		}
		
		return instance;
	}
	
	public static synchronized ObservationOfferingCache instance() throws FileNotFoundException {
		return instance;
	}

	private boolean cancelled;
	
	private ObservationOfferingCache(String dbName) throws FileNotFoundException {
		super(dbName);
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
	
	@Override
	protected boolean mergeWithPreviousEntries() {
		return true;
	}

	protected Collection<ObservationOffering> getCollectionFromDAO(AccessGDB geoDB)
			throws IOException {
		this.cancelled = false;
		clearTempCacheFile();
		
		geoDB.getOfferingAccess().getNetworksAsObservationOfferingsAsync(new OnOfferingRetrieved() {
			
			int count = 0;
			
			@Override
			public void retrieveExpectedOfferingsCount(int c) {
				setMaximumEntries(c);
			}
			
			@Override
			public void retrieveOffering(ObservationOffering oo, int currentOfferingIndex) throws RetrievingCancelledException {
				storeTemporaryEntity(oo);
				setLatestEntryIndex(currentOfferingIndex);
				LOGGER.info(String.format("Added ObservationOffering #%s to the cache.", count++));
				
				if (cancelled) {
					throw new RetrievingCancelledException("Cache update cancelled due to shutdown.");
				}
			}
			
		});
		return Collections.emptyList();
	}


	@Override
	protected AbstractEntityCache<ObservationOffering> getSingleInstance() {
		return instance;
	}

	@Override
	public void cancelCurrentExecution() {
		this.cancelled = true;
	}

}
