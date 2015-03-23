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
package org.n52.sos.db.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.ows.NoApplicableCodeException;
import org.n52.sos.cache.CacheNotYetAvailableException;
import org.n52.sos.cache.PropertyUnitMappingCache;
import org.n52.sos.dataTypes.Output;
import org.n52.sos.dataTypes.Procedure;
import org.n52.sos.dataTypes.PropertyUnitMapping;
import org.n52.sos.dataTypes.Unit;
import org.n52.sos.db.AccessGdbForProcedures;
import org.n52.util.logging.Logger;

import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class AccessGdbForProceduresImpl implements AccessGdbForProcedures {

   

	static Logger LOGGER = Logger.getLogger(AccessGdbForProceduresImpl.class.getName());

    private AccessGDBImpl gdb;

    public AccessGdbForProceduresImpl(AccessGDBImpl accessGDB) {
        this.gdb = accessGDB;
    }
    

	/**
     * This method can be used to retrieve the IDs of all procedures.
     * 
     * @throws AutomationException
     * @throws IOException
     */
    public List<String> getProcedureIdList() throws AutomationException, IOException {
        LOGGER.debug("Querying procedure list from DB.");
        
        // set tables
        List<String> tables = new ArrayList<String>();
        tables.add(Table.PROCEDURE);
//        LOGGER.info("Table clause := " + queryDef.getTables());
        
        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
//        LOGGER.info("Subfields clause := " + queryDef.getSubFields());
        
        // evaluate the database query
        ICursor cursor = DatabaseUtils.evaluateQuery(AccessGDBImpl.createCommaSeparatedList(tables),
        		"", AccessGDBImpl.createCommaSeparatedList(subFields), gdb);
        
        IRow row;
        List<String> procedureIdList = new ArrayList<String>();
        String key;
        while ((row = cursor.nextRow()) != null) {
        	key = AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID);
            String procedureId = row.getValue(subFields.indexOf(key)).toString();
            
            procedureIdList.add(procedureId);
        }
        
        return procedureIdList;
    }
    
    /**
     * This method returns all {@link Procedure}s for the identifiers given in the procedureIdentifierArray.
     * HOWEVER: this method only fills the ID and RESOURCE attributes of the Procedures.
     * REASON:  much better performance AND more information in the end not needed.
     */
    public Collection<Procedure> getProceduresWithIdAndResource(String[] procedureIdentifierArray) throws AutomationException, IOException
    {
        // set tables
        List<String> tables = new ArrayList<String>();
        tables.add(Table.PROCEDURE);
//        LOGGER.info("Table clause := " + queryDef.getTables());
        
        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE));
//        LOGGER.info("Subfields clause := " + queryDef.getSubFields());

        StringBuilder whereClause = new StringBuilder();
        if (procedureIdentifierArray != null) {
            whereClause.append(AccessGDBImpl.createOrClause(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID), procedureIdentifierArray));
        }
//        LOGGER.info(queryDef.getWhereClause());

        // evaluate the database query
        ICursor cursor = DatabaseUtils.evaluateQuery(AccessGDBImpl.createCommaSeparatedList(tables),
        		whereClause.toString(), AccessGDBImpl.createCommaSeparatedList(subFields),
        		gdb);

        IRow row;
        List<Procedure> procedures = new ArrayList<Procedure>();
        while ((row = cursor.nextRow()) != null) {

            String id = row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID))).toString();

            String resource = (String) row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE)));

            procedures.add(new Procedure(id, resource));
        }

        return procedures;
    }
    
    /**
     * This method returns all {@link Procedure}s for the identifiers given in the procedureIdentifierArray.
     * ALL attributes of the {@link Procedure}s are set.
     */
    public Collection<Procedure> getProceduresComplete(String[] procedureIdentifierArray) throws AutomationException, IOException 
    {
//        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();

        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE)); //this field is only needed so that DISTINCT works
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.UNIT, SubField.UNIT_NOTATION));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_LABEL));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_ID));
        
//        // set tables
//        List<String> tables = new ArrayList<String>();
//        tables.add(Table.PROCEDURE);
//        tables.add(Table.OBSERVATION);
//        tables.add(Table.SAMPLINGPOINT);
//        tables.add(Table.STATION);     
//        tables.add(Table.NETWORK);
//        tables.add(Table.UNIT);
//        tables.add(Table.AGGREGATIONTYPE);
//        tables.add(Table.VALUE);
//        tables.add(Table.PROPERTY);
//        tables.add(Table.FEATUREOFINTEREST);
//        queryDef.setTables(gdb.createCommaSeparatedList(tables));
        
        // create FROM clause
        String fromClause = "" + Table.PROCEDURE + 
    	" LEFT JOIN " + Table.OBSERVATION + " ON " + SubField.OBSERVATION_FK_PROCEDURE + " = " + SubField.PROCEDURE_PK_PROCEDURE +
    	" LEFT JOIN " + Table.PROPERTY + " ON " + SubField.OBSERVATION_FK_PROPERTY + " = " + SubField.PROPERTY_PK_PROPERTY + 
    	" LEFT JOIN " + Table.FEATUREOFINTEREST + " ON " + SubField.OBSERVATION_FK_FEATUREOFINTEREST + " = " + SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST + 
    	" LEFT JOIN " + Table.VALUE + " ON " + SubField.VALUE_FK_OBSERVATION + " = " + SubField.OBSERVATION_PK_OBSERVATION + 
    	" LEFT JOIN " + Table.UNIT + " ON " + SubField.VALUE_FK_UNIT + " = " + SubField.UNIT_PK_UNIT + 
    	" LEFT JOIN " + Table.AGGREGATIONTYPE + " ON " + SubField.VALUE_FK_AGGREGATIONTYPE + " = " + SubField.AGGREGATIONTYPE_PK_AGGREGATIONTYPE;
        
        StringBuilder whereClause = new StringBuilder();
        if (procedureIdentifierArray != null) {
//        	// joins:
//        	whereClause.append(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE) + " = " + gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE));
//        	whereClause.append(" AND ");
//        	whereClause.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROPERTY) + " = " + gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY));
//        	whereClause.append(" AND ");
//        	whereClause.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_FEATUREOFINTEREST) + " = " + gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST));
//        	whereClause.append(" AND ");
//        	whereClause.append(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_FK_OBSERVATION) + " = " + gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_PK_OBSERVATION));
//        	whereClause.append(" AND ");
//        	whereClause.append(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_FK_UNIT) + " = " + gdb.concatTableAndField(Table.UNIT, SubField.UNIT_PK_UNIT));
//        	whereClause.append(" AND ");
//        	whereClause.append(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_FK_AGGREGATIONTYPE) + " = " + gdb.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_PK_AGGREGATIONTYPE));
//        	whereClause.append(" AND ");
        	
        	// identifiers:
        	whereClause.append(AccessGDBImpl.createOrClause(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE), procedureIdentifierArray));
            
        }
        
        List<Procedure> procedureList = new ArrayList<Procedure>();

		// evaluate the database query
        ICursor cursor = DatabaseUtils.evaluateQuery(fromClause, whereClause.toString(), " DISTINCT " + AccessGDBImpl.createCommaSeparatedList(subFields),
        		gdb);
        IRow row;
        while ((row = cursor.nextRow()) != null) {

            String procedureID 	= row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID))).toString();
            String resource 	= row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE))).toString();
        	
            String unit = null;
            Object unitField = row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.UNIT, SubField.UNIT_NOTATION)));
            if (unitField != null) {
            	unit = unitField.toString();
            }
        	
            String property = null;
            Object propertyField = row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID)));
            if (propertyField != null) {
            	property = propertyField.toString();
            }
        	
            String propertyLabel = null;
            Object propertyLabelField = row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_LABEL)));
            if (propertyLabelField != null) {
            	propertyLabel = propertyField.toString();
            }
        	
            String feature = null;
            Object featureField = row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE)));
            if (featureField != null) {
            	feature = featureField.toString();
            }
        	
            String aggrTypeID = null;
            Object aggrTypeIDField = row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_ID)));
            if (aggrTypeIDField != null) {
            	aggrTypeID = aggrTypeIDField.toString();
            }
        	
            // case: procedure new
        	Procedure newProcedure = new Procedure(procedureID, resource);
            if (procedureList.contains(newProcedure) == false) {
            	
            	if (feature != null) {
            		newProcedure.addFeatureOfInterest(feature);
            	}
            	
            	if (aggrTypeID != null) {
            		newProcedure.addAggregationTypeID(aggrTypeID);
            	}
            	
            	/*
            	 * also check for unit. this addresses issues #40.
            	 * TODO Check if this breaks functionality
            	 */
            	outer:
            	if (property != null && propertyLabel != null && unit != null) {
            		if (newProcedure.getOutputs() != null) {
                		for (Output o : newProcedure.getOutputs()) {
    						if (o.getObservedPropertyID().equals(property) &&
    								o.getObservedPropertyLabel().equals(propertyLabel)) {
    							LOGGER.info("Ignoring output as this property is already present: "+property);
    							break outer;
    						}
    					}           			
            		}

            		newProcedure.addOutput(property, propertyLabel, unit);
            	}
            	
            	procedureList.add(newProcedure);
            }
            // case: procedure is already present in procedureList
            else {
                int index = procedureList.indexOf(newProcedure);
                Procedure existingProcedure = procedureList.get(index);
                                
                existingProcedure.addFeatureOfInterest(feature);
                existingProcedure.addAggregationTypeID(aggrTypeID);
                
            	/*
            	 * check for unit. this addresses issues #40.
            	 * TODO Check if this breaks functionality
            	 */
                outer:
            	if (property != null && propertyLabel != null && unit != null) {
            		if (existingProcedure.getOutputs() != null) {
            			for (Output o : existingProcedure.getOutputs()) {
    						if (o.getObservedPropertyID().equals(property) &&
    								o.getObservedPropertyLabel().equals(propertyLabel)) {
    							LOGGER.info("Ignoring output as this property is already present: "+property);
    							break outer;
    						}
    					}	
            		}
            		
            		existingProcedure.addOutput(property, propertyLabel, unit);
            	}
            }
        }
        
        return procedureList;
    }
    
    /**
     * @return a {@link Collection} of all {@link Procedure}s for a given network.
     * @throws NoApplicableCodeException 
     */
    public Collection<Procedure> getProceduresForNetwork(String networkID) throws IOException, NoApplicableCodeException
    {
    	PropertyUnitMappingCache pumCache = PropertyUnitMappingCache.instance(gdb.getDatabaseName());
    	Map<Integer, Unit> propertyUnitMap;
		try {
			propertyUnitMap = pumCache.resolvePropertyUnitMappings(gdb);
		} catch (CacheNotYetAvailableException e) {
			throw new NoApplicableCodeException(e);
		}
    	Unit fallbackDefaultUnit = pumCache.getDefaultFallbackUnit();
    	
    	LOGGER.debug("propertyUnitMap= "+propertyUnitMap.toString());
    	LOGGER.debug("fallbackDefaultUnit= "+fallbackDefaultUnit);
    	
    	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // request all procedures for network with ID 'networkID':
//        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();
        
        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE)); //this field is only needed so that DISTINCT works
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_LABEL));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE));
        
        String fromClause = 
        		Table.OBSERVATION +
        		" INNER JOIN " + Table.FEATUREOFINTEREST+ " ON " + Table.OBSERVATION + "." + SubField.OBSERVATION_FK_FEATUREOFINTEREST	+ " = " + Table.FEATUREOFINTEREST + "." + SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST +
        		" INNER JOIN " + Table.PROCEDURE 		+ " ON " + Table.OBSERVATION + "." + SubField.OBSERVATION_FK_PROCEDURE 			+ " = " + Table.PROCEDURE + "." + SubField.PROCEDURE_PK_PROCEDURE +
        		" INNER JOIN " + Table.PROPERTY 		+ " ON " + Table.OBSERVATION + "." + SubField.OBSERVATION_FK_PROPERTY 			+ " = " + Table.PROPERTY + "." + SubField.PROPERTY_PK_PROPERTY +
        		" INNER JOIN " + Table.SAMPLINGPOINT 	+ " ON " + Table.OBSERVATION + "." + SubField.OBSERVATION_FK_SAMPLINGPOINT 		+ " = " + Table.SAMPLINGPOINT + "." + SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT + 
        		" INNER JOIN " + Table.STATION 			+ " ON " + Table.SAMPLINGPOINT + "." + SubField.SAMPLINGPOINT_FK_STATION 		+ " = " + Table.STATION + "." + SubField.STATION_PK_STATION +
        		" INNER JOIN " + Table.NETWORK 			+ " ON " + Table.NETWORK + "." + SubField.NETWORK_PK_NETWOK 					+ " = " + Table.STATION + "." + SubField.STATION_FK_NETWORK_GID +
        		" INNER JOIN " + Table.VALUE 			+ " ON " + Table.OBSERVATION + "." + SubField.OBSERVATION_PK_OBSERVATION		+ " = " + Table.VALUE + "." + SubField.VALUE_FK_OBSERVATION;
       
		List<Procedure> procedureList = new ArrayList<Procedure>();

		// evaluate the database query
        ICursor cursor = DatabaseUtils.evaluateQuery(fromClause,
        		AccessGDBImpl.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID + " = '" + networkID + "'"),
        		"DISTINCT "+AccessGDBImpl.createCommaSeparatedList(subFields),
        		gdb);
        
        IRow row;
        while ((row = cursor.nextRow()) != null) {

            String procedureID 	= row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID))).toString();
            String resource 	= row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE))).toString();
            String propertyPk 	= row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY))).toString();
            String property 	= row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID))).toString();
        	String propertyLabel= row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_LABEL))).toString();
        	String feature 		= row.getValue(subFields.indexOf(AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE))).toString();
        	
        	//TODO: check for null value -> resolve a "default for all properties" unit
        	Unit relatedUnit = propertyUnitMap.get(Integer.parseInt(propertyPk));
        	if (relatedUnit == null) {
        		LOGGER.warn("No property to unit mapping for: "+propertyPk);
        		relatedUnit = fallbackDefaultUnit;
        	}
        	String unit = relatedUnit.getNotation();
        	
            Procedure procedure;
			// case: procedure new
            if (procedureList.contains(procedureID) == false) {
            	procedure = new Procedure(procedureID, resource);
            	procedureList.add(procedure);
            }
            // case: procedure is already present in procedureList
            else {
                int index = procedureList.indexOf(procedureID);
                procedure = procedureList.get(index);
            }
            
            procedure.addFeatureOfInterest(feature);
        	procedure.addOutput(property, propertyLabel, unit);
        }
        
        return procedureList;
	}


	@Override
	public boolean isNetwork(String procedureID) throws AutomationException, IOException {
        ICursor cursor = DatabaseUtils.evaluateQuery(Table.NETWORK,
        		AccessGDBImpl.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID) + " = '" + procedureID + "'",
        		AccessGDBImpl.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID),
        		gdb);
        
        IRow row;
        while ((row = cursor.nextRow()) != null) {
            String networkID = row.getValue(0).toString();
            
            if (networkID != null && networkID.equalsIgnoreCase(procedureID)) {
            	return true;
            }
        }
        
		return false;
	}

	@Override
	public boolean isProcedure(String procedureResourceID) throws AutomationException, IOException {

        ICursor cursor = DatabaseUtils.evaluateQuery(Table.PROCEDURE,
        		AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE) + " = '" + procedureResourceID + "'",
        		AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE),
        		gdb);
        
        IRow row;
        while ((row = cursor.nextRow()) != null) {
            String procedureIdFromDB = row.getValue(0).toString();
            
            if (procedureIdFromDB != null && procedureIdFromDB.equalsIgnoreCase(procedureResourceID)) {
            	return true;
            }
        }
        
		return false;
	}

	@Override
	public Collection<PropertyUnitMapping> getPropertyUnitMappings() throws IOException {
		PropertyUnitMapping result = new PropertyUnitMapping();
		
		String subFields = AccessGDBImpl.createCommaSeparatedList(
				AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY),
				AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_FK_UNIT)
				);
		
		String tables = AccessGDBImpl.createCommaSeparatedList(
				Table.PROPERTY,
				Table.OBSERVATION,
				Table.VALUE
				);
		
		StringBuilder whereClause = new StringBuilder();
		whereClause.append(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY));
		whereClause.append(" = ");
		whereClause.append(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROPERTY));
		whereClause.append(" AND ");
		
		whereClause.append(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_PK_OBSERVATION));
		whereClause.append(" = ");
		whereClause.append(AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_FK_OBSERVATION));
		whereClause.append(" AND ");
		
		whereClause.append(AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_FK_UNIT));
		whereClause.append(" IS NOT NULL");
		
		ICursor cursor = DatabaseUtils.evaluateQuery(tables, whereClause.toString(),
				"DISTINCT ".concat(subFields), gdb, true);
        
        IRow row;
        int count = 0;
        while ((row = cursor.nextRow()) != null) {
        	LOGGER.debug("Working on row "+ count++);
        	String propertyId = row.getValue(0).toString();
        	String valueFkUnit = row.getValue(1).toString();
        	
        	try {
        		Integer propertyIntId = Integer.parseInt(propertyId);
        		if (result.containsKey(propertyIntId)) {
        			LOGGER.warn(String.format("Multiple mappings for property '%s' - skipping candidate unit: '%s'",
        					propertyIntId, valueFkUnit));
        		} else {
        			result.put(propertyIntId, Integer.parseInt(valueFkUnit));
        		}
        	}
        	catch (NumberFormatException e) {
        		LOGGER.warn(e.getMessage(), e);
        	}
        }
		
		return Collections.singleton(result);
	}


	@Override
	public Map<Integer, Unit> getUnitsOfMeasure() throws IOException {
		ICursor result = DatabaseUtils.evaluateQuery(Table.UNIT, null, "*", gdb);

		Map<Integer, Unit> units = new HashMap<>();
		IRow row;
		Unit u;
		while ((row = result.nextRow()) != null) {
			u = Unit.fromRow(row);
			units.put(u.getPkUnit(), u);
		}
		LOGGER.debug(String.format("Resolved units: %s", units));
		
		return units;
	}
	
}
