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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.n52.sos.dataTypes.PropertyUnitMapping;
import org.n52.sos.dataTypes.Unit;
import org.n52.sos.db.AccessGDB;
import org.n52.util.logging.Logger;

public class PropertyUnitMappingCache extends
		AbstractEntityCache<PropertyUnitMapping> {

	private static final Logger logger = Logger
			.getLogger(PropertyUnitMappingCache.class.getName());
	private static PropertyUnitMappingCache instance;

	private Map<Integer, Unit> propertyUnitMap = null;
	private Unit defaultFallbackUnit = new Unit(-1, "n/a", "n/a", "mg/m3", "n/a", "n/a");

	public static synchronized PropertyUnitMappingCache instance(String dbName)
			throws FileNotFoundException {
		if (instance == null) {
			instance = new PropertyUnitMappingCache(dbName);
		}

		return instance;
	}

	private PropertyUnitMappingCache(String dbName) throws FileNotFoundException {
		super(dbName);
	}

	@Override
	protected String getCacheFileName() {
		return "propertyUnitMappings.cache";
	}

	@Override
	protected String serializeEntity(PropertyUnitMapping entity)
			throws CacheException {
		logger.debug(String.format("Serializing %s mappings", entity.size()));

		if (entity.size() == 0) {
			throw new CacheException("No entries in PropertyUnitMappings! Check the database query");
		}
		
		StringBuilder sb = new StringBuilder();

		Integer valueFkUnit;
		for (Integer propertyId : entity.keySet()) {
			valueFkUnit = entity.get(propertyId);
			sb.append(propertyId);
			sb.append("=");
			sb.append(valueFkUnit);
			sb.append(";");
		}

		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	@Override
	protected PropertyUnitMapping deserializeEntity(String line) {
		PropertyUnitMapping result = new PropertyUnitMapping();

		String[] entries = line.split(";");
		String[] kvp;
		for (String e : entries) {
			kvp = e.split("=");

			try {
				Integer key = Integer.parseInt(kvp[0]);
				Integer value = Integer.parseInt(kvp[1]);
				result.put(key, value);
			} catch (NumberFormatException | IndexOutOfBoundsException ex) {
				logger.warn(ex.getMessage(), ex);
			}
		}
		
		logger.debug("Deserialized mapping: "+result.toString());
		return result;
	}

	@Override
	protected Collection<PropertyUnitMapping> getCollectionFromDAO(
			AccessGDB geoDB) throws IOException {
		logger.info("Retrieving Mappings...");
		return geoDB.getProcedureAccess().getPropertyUnitMappings();
	}

	@Override
	protected AbstractEntityCache<PropertyUnitMapping> getSingleInstance() {
		return instance;
	}
	
	@Override
	public void updateCache(AccessGDB geoDB) throws CacheException, IOException {
		super.updateCache(geoDB);
		try {
			resolvePropertyUnitMappings(geoDB);
		} catch (CacheNotYetAvailableException e) {
			throw new CacheException(e);
		}
	}

	public synchronized Map<Integer, Unit> resolvePropertyUnitMappings(AccessGDB gdb) throws CacheNotYetAvailableException {
		if (propertyUnitMap != null && propertyUnitMap.isEmpty() && !requiresUpdate()) {
			logger.debug("returning in memory instance of propertyUnitMap");
			return new HashMap<>(propertyUnitMap);
		}
		
		propertyUnitMap = new HashMap<>();

		try {
			Map<Integer, Unit> units = gdb.getProcedureAccess()
					.getUnitsOfMeasure();
			logger.debug("Available units: "+units.toString());

			Map<String, PropertyUnitMapping> mappings = instance.getEntityCollection(gdb);
			logger.debug(String.format("PropertyUnitMapping entries from cache: %s",
					mappings.size()));

			for (String key : mappings.keySet()) {
				PropertyUnitMapping value = mappings.get(key);

				for (Integer propertyPk : value.keySet()) {
					propertyUnitMap.put(propertyPk,
							units.get(value.get(propertyPk)));
				}
			}

			logger.debug(String.format("PropertyUnitMappings resolved: %s (%s)",
					propertyUnitMap.size(), propertyUnitMap.toString()));

		} catch (IOException | NumberFormatException | CacheException e) {
			logger.warn("Failed to resolve property to unit mappings", e);
		}
		
		updateDefaultFallbackUnit();
		
		return new HashMap<>(propertyUnitMap);
	}

	public Unit getDefaultFallbackUnit() {
		return defaultFallbackUnit;
	}
	
	private void updateDefaultFallbackUnit() {
		Collection<Unit> values = propertyUnitMap.values();
		Unit max = null;
		int maxFrequency = 0;
		for (Unit u : values) {
			if (Collections.frequency(values, u) > maxFrequency) {
				max = u;
			}
		}
		
		if (max != null) {
			logger.debug("default fallback: "+ max);
			defaultFallbackUnit = max;
		}
	}

	@Override
	public void cancelCurrentExecution() {
	}

}
