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
import java.util.logging.Logger;

import org.n52.util.logging.Log;

import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IQueryDef;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.server.json.JSONObject;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class AccessGDBAnalysis {

    static Logger LOGGER = Logger.getLogger(AccessGDBAnalysis.class.getName());

    private AccessGDB gdb;

    public AccessGDBAnalysis(AccessGDB accessGDB) {
        this.gdb = accessGDB;
    }
    
    /**
     * @throws IOException 
     * @throws AutomationException 
     * 
     */
    public JSONObject analyzeDB () throws AutomationException, IOException {
        
        JSONObject json = new JSONObject();
        
        /*
         * check number of observations!
         * build something like: SELECT COUNT(column_name) FROM table_name
         */
        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();
        queryDef.setTables(Table.OBSERVATION);
        queryDef.setSubFields("COUNT(" + SubField.OBSERVATION_ID + ")");
        queryDef.evaluate();
        ICursor cursor = queryDef.evaluate();
        IRow row;
        while ((row = cursor.nextRow()) != null) {
            Object observationCount = row.getValue(0);
            String observationCountAsString = observationCount.toString();
            
            json.append("observationCount", observationCountAsString);
        }
        
        /*
         * check number of features!
         */
        queryDef = gdb.getWorkspace().createQueryDef();
        queryDef.setTables(Table.FEATUREOFINTEREST);
        queryDef.setSubFields("COUNT(" + SubField.FEATUREOFINTEREST_ID + ")");
        queryDef.evaluate();
        cursor = queryDef.evaluate();
        while ((row = cursor.nextRow()) != null) {
            Object featureCount = row.getValue(0);
            String featureCountAsString = featureCount.toString();
            
            json.append("featureCount", featureCountAsString);
        }
        
        /*
         * check number of procedures!
         */
        queryDef = gdb.getWorkspace().createQueryDef();
        queryDef.setTables(Table.PROCEDURE);
        queryDef.setSubFields("COUNT(" + SubField.PROCEDURE_ID + ")");
        queryDef.evaluate();
        cursor = queryDef.evaluate();
        while ((row = cursor.nextRow()) != null) {
            Object procedureCount = row.getValue(0);
            String procedureCountAsString = procedureCount.toString();
            
            json.append("procedureCount", procedureCountAsString);
        }
        
        /*
         * check number of observedPropertys!
         */
        queryDef = gdb.getWorkspace().createQueryDef();
        queryDef.setTables(Table.PROPERTY);
        queryDef.setSubFields("COUNT(" + SubField.PROPERTY_ID + ")");
        queryDef.evaluate();
        cursor = queryDef.evaluate();
        while ((row = cursor.nextRow()) != null) {
            Object observedPropertyCount = row.getValue(0);
            String observedPropertyCountAsString = observedPropertyCount.toString();
            
            json.append("observedPropertyCount", observedPropertyCountAsString);
        }
        
        return json;
    }
}
