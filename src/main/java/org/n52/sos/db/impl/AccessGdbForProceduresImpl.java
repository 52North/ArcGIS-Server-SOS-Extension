/*
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 * 
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.List;

import org.n52.sos.dataTypes.Procedure;
import org.n52.sos.db.AccessGdbForProcedures;
import org.n52.util.logging.Logger;

import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IFeatureWorkspace;
import com.esri.arcgis.geodatabase.IQueryDef;
import com.esri.arcgis.geodatabase.IQueryDef2;
import com.esri.arcgis.geodatabase.IQueryDef2Proxy;
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
        
        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();
        
        // set tables
        List<String> tables = new ArrayList<String>();
        tables.add(Table.PROCEDURE);
        queryDef.setTables(gdb.createCommaSeparatedList(tables));
//        LOGGER.info("Table clause := " + queryDef.getTables());
        
        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
        queryDef.setSubFields(gdb.createCommaSeparatedList(subFields));
//        LOGGER.info("Subfields clause := " + queryDef.getSubFields());
        
        // evaluate the database query
        ICursor cursor = queryDef.evaluate();
        
        Fields fields = (Fields) cursor.getFields();
        IRow row;
        List<String> procedureIdList = new ArrayList<String>();
        while ((row = cursor.nextRow()) != null) {
            String procedureId = row.getValue(fields.findField(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID))).toString();
            
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
        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();

        // set tables
        List<String> tables = new ArrayList<String>();
        tables.add(Table.PROCEDURE);
        queryDef.setTables(gdb.createCommaSeparatedList(tables));
//        LOGGER.info("Table clause := " + queryDef.getTables());
        
        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE));
        queryDef.setSubFields(gdb.createCommaSeparatedList(subFields));
//        LOGGER.info("Subfields clause := " + queryDef.getSubFields());

        StringBuilder whereClause = new StringBuilder();
        if (procedureIdentifierArray != null) {
            whereClause.append(gdb.createOrClause(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID), procedureIdentifierArray));
            
            queryDef.setWhereClause(whereClause.toString());
        }
//        LOGGER.info(queryDef.getWhereClause());

        // evaluate the database query
        ICursor cursor = queryDef.evaluate();

        Fields fields = (Fields) cursor.getFields();
        IRow row;
        List<Procedure> procedures = new ArrayList<Procedure>();
        while ((row = cursor.nextRow()) != null) {

            String id = row.getValue(fields.findField(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID))).toString();

            String resource = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE)));

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
        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();

        // set sub fields
        List<String> subFields = createSubFields();
        queryDef.setSubFields(" DISTINCT " + gdb.createCommaSeparatedList(subFields));
        LOGGER.info("SELECT " + queryDef.getSubFields());
        
        // set tables
        List<String> tables = createTables();
        queryDef.setTables(gdb.createCommaSeparatedList(tables));
        LOGGER.info("FROM " + queryDef.getTables());
        
        StringBuilder whereClause = new StringBuilder();
        if (procedureIdentifierArray != null) {
        	// joins:
        	whereClause.append(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE) + " = " + gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE));
        	whereClause.append(" AND ");
        	whereClause.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROPERTY) + " = " + gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY));
        	whereClause.append(" AND ");
        	whereClause.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_FEATUREOFINTEREST) + " = " + gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST));
        	whereClause.append(" AND ");
        	whereClause.append(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_FK_OBSERVATION) + " = " + gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_PK_OBSERVATION));
        	whereClause.append(" AND ");
        	whereClause.append(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_FK_UNIT) + " = " + gdb.concatTableAndField(Table.UNIT, SubField.UNIT_PK_UNIT));
        	whereClause.append(" AND ");
        	whereClause.append(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_FK_AGGREGATIONTYPE) + " = " + gdb.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_PK_AGGREGATIONTYPE));
        	whereClause.append(" AND ");
        	
        	// identifiers:
        	whereClause.append(gdb.createOrClause(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE), procedureIdentifierArray));
            
            queryDef.setWhereClause(whereClause.toString());
        }
        LOGGER.info("WHERE " + queryDef.getWhereClause());
        
        List<Procedure> procedureList = evaluateQueryAndCreateProcedureList(queryDef);

        return procedureList;
    }
    

    /**
     * @return a {@link Collection} of all {@link Procedure}s for a given array of network IDs.
     */
    public Collection<Procedure> getProceduresForNetwork_old(String[] networkIDs) throws IOException
    {   
    	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // request all procedures for network with ID 'networkID':
        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();
        
        // set sub fields
        List<String> subFields = createSubFields();
        queryDef.setSubFields(" DISTINCT " + gdb.createCommaSeparatedList(subFields));
        LOGGER.info("SELECT " + queryDef.getSubFields());

        // set tables
        //queryDef.setTables(createFromClause());
        List<String> tables = createTables();
        queryDef.setTables(gdb.createCommaSeparatedList(tables));
        LOGGER.debug("FROM " + queryDef.getTables());

        // create where clause with joins and constraints
        StringBuilder whereClause = new StringBuilder();
    	// joins:
    	whereClause.append(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE) + " = " + gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE));
    	whereClause.append(" AND ");
    	whereClause.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROPERTY) + " = " + gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY));
    	whereClause.append(" AND ");
    	whereClause.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_FEATUREOFINTEREST) + " = " + gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST));
    	whereClause.append(" AND ");
    	whereClause.append(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_FK_OBSERVATION) + " = " + gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_PK_OBSERVATION));
    	whereClause.append(" AND ");
    	whereClause.append(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_FK_UNIT) + " = " + gdb.concatTableAndField(Table.UNIT, SubField.UNIT_PK_UNIT));
    	whereClause.append(" AND ");
    	whereClause.append(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_FK_AGGREGATIONTYPE) + " = " + gdb.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_PK_AGGREGATIONTYPE));
    	whereClause.append(" AND ");
    	whereClause.append(gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT) + " = " + gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_SAMPLINGPOINT));
	    whereClause.append(" AND ");
    	whereClause.append(gdb.concatTableAndField(Table.STATION, SubField.STATION_PK_STATION) + " = " + gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_FK_STATION));
	    whereClause.append(" AND ");
	    whereClause.append(gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_PK_NETWOK) + " = " + gdb.concatTableAndField(Table.STATION, SubField.STATION_FK_NETWORK_GID));
	    whereClause.append(" AND ");
	    // query network:
	    whereClause.append(gdb.createOrClause(gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID), networkIDs));

	    /*// only for testing purposes, to make queries faster, the following lines can be included:
	    whereClause.append(" AND ");
	    whereClause.append(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID) + " = 'GB_StationProcess_3422'");
	    */
	    
	    queryDef.setWhereClause(whereClause.toString());
	    LOGGER.debug("WHERE " + queryDef.getWhereClause());
        
        List<Procedure> procedureList = evaluateQueryAndCreateProcedureList(queryDef);
        
        return procedureList;
    }

    /**
     * @return a {@link Collection} of all {@link Procedure}s for a given array of network IDs.
     */
    public Collection<Procedure> getProceduresForNetwork(String[] networkIDs) throws IOException
    {   
    	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // request all procedures for network with ID 'networkID':
        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();
        
        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE)); //this field is only needed so that DISTINCT works
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
        subFields.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_LABEL));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE));
        queryDef.setSubFields(gdb.createCommaSeparatedList(subFields));
        LOGGER.info("SELECT " + queryDef.getSubFields());

        String fromClause = 
        		Table.OBSERVATION +
        		" LEFT JOIN " + Table.FEATUREOFINTEREST	+ " ON " + Table.OBSERVATION + "." + SubField.OBSERVATION_FK_FEATUREOFINTEREST	+ " = " + Table.FEATUREOFINTEREST + "." + SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST +
        		" LEFT JOIN " + Table.PROCEDURE 		+ " ON " + Table.OBSERVATION + "." + SubField.OBSERVATION_FK_PROCEDURE 			+ " = " + Table.PROCEDURE + "." + SubField.PROCEDURE_PK_PROCEDURE +
        		" LEFT JOIN " + Table.PROPERTY 			+ " ON " + Table.OBSERVATION + "." + SubField.OBSERVATION_FK_PROPERTY 			+ " = " + Table.PROPERTY + "." + SubField.PROPERTY_PK_PROPERTY +
        		" LEFT JOIN " + Table.SAMPLINGPOINT 	+ " ON " + Table.OBSERVATION + "." + SubField.OBSERVATION_FK_SAMPLINGPOINT 		+ " = " + Table.SAMPLINGPOINT + "." + SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT + 
        		" LEFT JOIN " + Table.STATION 			+ " ON " + Table.SAMPLINGPOINT + "." + SubField.SAMPLINGPOINT_FK_STATION 		+ " = " + Table.STATION + "." + SubField.STATION_PK_STATION +
        		" LEFT JOIN " + Table.NETWORK 			+ " ON " + Table.NETWORK + "." + SubField.NETWORK_PK_NETWOK 					+ " = " + Table.STATION + "." + SubField.STATION_FK_NETWORK_GID;
        queryDef.setTables(fromClause);
        LOGGER.debug("FROM " + queryDef.getTables());
        
	    queryDef.setWhereClause(gdb.createOrClause(gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID), networkIDs));
	    LOGGER.debug("WHERE " + queryDef.getWhereClause());
        
        List<Procedure> procedureList = evaluateQueryAndCreateProcedureList(queryDef);
        
        return procedureList;
    }
    

	/**
     * Support method to create tables.
     */
	private List<String> createTables() {
		List<String> tables = new ArrayList<String>();
        tables.add(Table.PROCEDURE);
        tables.add(Table.OBSERVATION);
        tables.add(Table.SAMPLINGPOINT);
        tables.add(Table.STATION);     
        tables.add(Table.NETWORK);
        tables.add(Table.UNIT);
        tables.add(Table.AGGREGATIONTYPE);
        tables.add(Table.VALUE);
        tables.add(Table.PROPERTY);
        tables.add(Table.FEATUREOFINTEREST);
		return tables;
	}

	/**
     * Support method to create sub fields.
     */
	private List<String> createSubFields() {
		List<String> subFields = new ArrayList<String>();
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE)); //this field is only needed so that DISTINCT works
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_NOTATION));
        subFields.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
        subFields.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_LABEL));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_ID));
		return subFields;
	}
	
    /**
     * Support method to evaluate the passed query and create the list of procedures.
     */
	private List<Procedure> evaluateQueryAndCreateProcedureList(IQueryDef queryDef) throws AutomationException, IOException {
		
		List<Procedure> procedureList = new ArrayList<Procedure>();

		// evaluate the database query
        ICursor cursor = queryDef.evaluate();
        Fields fields = (Fields) cursor.getFields();
        IRow row;
        while ((row = cursor.nextRow()) != null) {

            String procedureID 	= row.getValue(fields.findField(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID))).toString();
            String resource 	= row.getValue(fields.findField(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE))).toString();
        	String unit 		= row.getValue(fields.findField(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_NOTATION))).toString();
        	String property 	= row.getValue(fields.findField(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID))).toString();
        	String propertyLabel= row.getValue(fields.findField(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_LABEL))).toString();
        	String feature 		= row.getValue(fields.findField(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE))).toString();
        	String aggrTypeID   = row.getValue(fields.findField(gdb.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_ID))).toString();
        	
            // case: procedure new
            if (procedureList.contains(procedureID) == false) {
            	Procedure procedure = new Procedure(procedureID, resource);
            	
            	procedure.addFeatureOfInterest(feature);
            	procedure.addOutput(property, propertyLabel, unit);
            	procedure.addAggregationTypeID(aggrTypeID);
            	
            	procedureList.add(procedure);
            }
            // case: procedure is already present in procedureList
            else {
                int index = procedureList.indexOf(procedureID);
                Procedure procedure = procedureList.get(index);
                                
                procedure.addFeatureOfInterest(feature);
                procedure.addOutput(property, propertyLabel, unit);
                procedure.addAggregationTypeID(aggrTypeID);
            }
        }
        
        return procedureList;
	}

}
