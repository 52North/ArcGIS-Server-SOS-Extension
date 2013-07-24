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

package org.n52.sos.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.n52.sos.dataTypes.Procedure;
import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IQueryDef;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class AccessGdbForProcedures {

    static Logger LOGGER = Logger.getLogger(AccessGdbForProcedures.class.getName());

    private AccessGDB gdb;

    public AccessGdbForProcedures(AccessGDB accessGDB) {
        this.gdb = accessGDB;
    }
    
    /**
     * This method can be used to retrieve the IDs of all procedures.
     * 
     * @throws AutomationException
     * @throws IOException
     */
    public List<String> getProcedureIdList() throws AutomationException, IOException {
        LOGGER.info("Querying procedure list from DB.");
        
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
}
