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
     * This method can be used to retrieve all {@link Procedure}s associated
     * with the SOS complying to the filter as specified by the parameters. The
     * method basically reflects the SOS:DescribeSensor() operation on Java
     * level.
     * 
     * If one of the method parameters is <b>null</b>, it shall not be
     * considered in the query.
     * 
     * @param procedureIdentifierArray
     *            an array of unique IDs.
     * @return all procedures from the Geodatabase which comply to the specified
     *         parameters.
     * @throws IOException
     * @throws AutomationException
     */
    public Collection<Procedure> getProcedures(String[] procedureIdentifierArray) throws AutomationException, IOException 
    {
        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();
        
        // set tables
        List<String> tables = new ArrayList<String>();
        tables.add(Table.PROCEDURE);
        tables.add(Table.OBSERVATION);
        tables.add(Table.VALUE);
        tables.add(Table.UNIT);
        tables.add(Table.PROPERTY);
        tables.add(Table.FEATUREOFINTEREST);
        queryDef.setTables(gdb.createCommaSeparatedList(tables));
        LOGGER.info("Table clause := " + queryDef.getTables());
        
        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_NOTATION));
        subFields.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID));
        queryDef.setSubFields(gdb.createCommaSeparatedList(subFields));
        LOGGER.info("Subfields clause := " + queryDef.getSubFields());

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
        	
        	// identifiers:
        	whereClause.append(gdb.createOrClause(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID), procedureIdentifierArray));
            
            queryDef.setWhereClause(whereClause.toString());
        }
        LOGGER.info(queryDef.getWhereClause());
        
        // evaluate the database query
        ICursor cursor = queryDef.evaluate();
        
        Fields fields = (Fields) cursor.getFields();
        
        IRow row;
        List<Procedure> procedureList = new ArrayList<Procedure>();
        while ((row = cursor.nextRow()) != null) {

            String procedureID 	= row.getValue(fields.findField(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID))).toString();
            String resource 	= row.getValue(fields.findField(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE))).toString();
        	String unit 		= row.getValue(fields.findField(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_NOTATION))).toString();
        	String property 	= row.getValue(fields.findField(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID))).toString();
        	String feature 		= row.getValue(fields.findField(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID))).toString();
        	
            // case: procedure new
            if (procedureList.contains(procedureID) == false) {
            	Procedure procedure = new Procedure(procedureID, resource);
            	
            	procedure.addFeatureOfInterest(feature);
            	procedure.addOutput(property, unit);
            	
            	procedureList.add(procedure);
            }
            // case: procedure is already present in procedureList
            else {
                int index = procedureList.indexOf(procedureID);
                Procedure procedure = procedureList.get(index);
                                
                procedure.addFeatureOfInterest(feature);
                procedure.addOutput(property, unit);
            }
        }

        return procedureList;
    }
    

    /**
     * @return a {@link Collection} of all {@link Procedure}s for a given networkID.
     */
    public Collection<Procedure> getProceduresForNetwork(String networkID) throws IOException
    {   
    	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // request all procedures for network with ID 'networkID':
        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();
        
        //IQueryDef2 queryDef = (IQueryDef2) queryDef1;
        
        //queryDef.setPrefixClause("DISTINCT");
        
        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE));
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_NOTATION));
        subFields.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID));
        queryDef.setSubFields(" DISTINCT " + gdb.createCommaSeparatedList(subFields));
        LOGGER.info("SELECT " + queryDef.getSubFields());

        // set tables
        //queryDef.setTables(createFromClause());
        List<String> tables = new ArrayList<String>();
        tables.add(Table.PROCEDURE);
        tables.add(Table.OBSERVATION);
        tables.add(Table.SAMPLINGPOINT);
        tables.add(Table.STATION);     
        tables.add(Table.NETWORK);
        tables.add(Table.UNIT);
        tables.add(Table.VALUE);
        tables.add(Table.PROPERTY);
        tables.add(Table.FEATUREOFINTEREST);
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
    	whereClause.append(gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT) + " = " + gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_SAMPLINGPOINT));
	    whereClause.append(" AND ");
    	whereClause.append(gdb.concatTableAndField(Table.STATION, SubField.STATION_PK_STATION) + " = " + gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_FK_STATION));
	    whereClause.append(" AND ");
	    whereClause.append(gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_PK_NETWOK) + " = " + gdb.concatTableAndField(Table.STATION, SubField.STATION_FK_NETWORK_GID));
	    whereClause.append(" AND ");
	    // query network:
	    whereClause.append(gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID) + " = '" + networkID + "'");
	    
	    queryDef.setWhereClause(whereClause.toString());
	    LOGGER.debug("WHERE " + queryDef.getWhereClause());
        
        // evaluate the database query
        ICursor cursor = queryDef.evaluate();
        
        Fields fields = (Fields) cursor.getFields();
        
        IRow row;
        List<Procedure> procedureList = new ArrayList<Procedure>();
        while ((row = cursor.nextRow()) != null) {

            String procedureID 	= row.getValue(fields.findField(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID))).toString();
            String resource 	= row.getValue(fields.findField(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE))).toString();
        	String unit 		= row.getValue(fields.findField(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_NOTATION))).toString();
        	String property 	= row.getValue(fields.findField(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID))).toString();
        	String feature 		= row.getValue(fields.findField(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID))).toString();
        	
            // case: procedure new
            if (procedureList.contains(procedureID) == false) {
            	Procedure procedure = new Procedure(procedureID, resource);
            	
            	procedure.addFeatureOfInterest(feature);
            	procedure.addOutput(property, unit);
            	
            	procedureList.add(procedure);
            }
            // case: procedure is already present in procedureList
            else {
                int index = procedureList.indexOf(procedureID);
                Procedure procedure = procedureList.get(index);
                                
                procedure.addFeatureOfInterest(feature);
                procedure.addOutput(property, unit);
            }
        }
        
        return procedureList;
    }
}
