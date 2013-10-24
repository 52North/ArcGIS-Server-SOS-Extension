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
import java.util.Iterator;
import java.util.List;

import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimePeriod;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.dataTypes.Procedure;
import org.n52.sos.db.AccessGdbForProcedures;
import org.n52.util.logging.Logger;

import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IQueryDef;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.Point;
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
     * 
     */
    public Collection<Procedure> getProceduresForNetwork(String networkID) throws IOException
    {   
        Collection<Procedure> procedures = new ArrayList<Procedure>();
        
        IQueryDef queryDefProp = gdb.getWorkspace().createQueryDef();

        // set tables
        List<String> tablesProp = new ArrayList<String>();
        tablesProp.add(Table.NETWORK);
        tablesProp.add(Table.STATION);
        tablesProp.add(Table.SAMPLINGPOINT);
        tablesProp.add(Table.OBSERVATION);
        tablesProp.add(Table.PROCEDURE);
        queryDefProp.setTables(gdb.createCommaSeparatedList(tablesProp));
        LOGGER.debug("Tables clause := " + queryDefProp.getTables());

        // set sub fields
        List<String> subFieldsProp = new ArrayList<String>();
        subFieldsProp.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
        queryDefProp.setSubFields(gdb.createCommaSeparatedList(subFieldsProp));
        LOGGER.debug("Subfields clause := " + queryDefProp.getSubFields());

        // create where clause with joins and constraints
        StringBuilder whereClauseProp = new StringBuilder();
        whereClauseProp.append(gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_PK_NETWOK) + " = " + gdb.concatTableAndField(Table.STATION, SubField.STATION_FK_NETWORK_GID));
        whereClauseProp.append(" AND ");
        whereClauseProp.append(gdb.concatTableAndField(Table.STATION, SubField.STATION_PK_STATION) + " = " + gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_FK_STATION));
        whereClauseProp.append(" AND ");
        whereClauseProp.append(gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT) + " = " + gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_SAMPLINGPOINT));
        whereClauseProp.append(" AND ");
        whereClauseProp.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE) + " = " + gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE));
        queryDefProp.setWhereClause(whereClauseProp.toString());
        LOGGER.debug("Where clause := " + queryDefProp.getWhereClause());

        // evaluate the database query
        ICursor cursorProp = queryDefProp.evaluate();
        
        List<String> proceduresList = new ArrayList<String>();
        Fields fields = (Fields) cursorProp.getFields();

        IRow row;
        while ((row = cursorProp.nextRow()) != null) {
            String procedureID = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID)));
            if (! proceduresList.contains(procedureID)) {
            	proceduresList.add(procedureID);
            }
        }
        
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // now query all procedures in the list:
        
        String[] procedureIdentifierArray = new String[]{};
        procedureIdentifierArray = proceduresList.toArray(procedureIdentifierArray);
        
        procedures = this.getProcedures(procedureIdentifierArray); 
        
        return procedures;
    }
}
