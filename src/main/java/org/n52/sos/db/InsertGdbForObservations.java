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
import java.util.Date;
import java.util.logging.Logger;

import com.esri.arcgis.interop.AutomationException;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class InsertGdbForObservations {

    static Logger LOGGER = Logger.getLogger(InsertGdbForObservations.class.getName());


    public InsertGdbForObservations(AccessGDB accessGDB) {
    	//TODO: never used?
//        this.gdb = accessGDB;
    }
    
    public int insertObservation(int offeringID,
            Date phenomenonTime,
            int featureID,
            int observedPropertyID,
            int procedureID,
            float result) throws Exception
    {
    	// TODO: insert observation needs to be adjusted to new AQ e-Reporting data model
    	
    	throw new UnsupportedOperationException();
    	
//        LOGGER.info("Starting to add new observation");
//
//        try {
//            gdb.getWorkspace().startEditing(true);
//            gdb.getWorkspace().startEditOperation();
//            
//            LOGGER.info("Started editing workspace.");
//            
//            ITable observationFC = gdb.getWorkspace().openTable(Table.OBSERVATION);
//            IRow newObservation = observationFC.createRow();
//            LOGGER.info("New row created.");
//            
//            IFields fields = observationFC.getFields();
//            for (int i = 0; i < fields.getFieldCount(); i++) {
//                if (fields.getField(i).getName().equals(SubField.OBSERVATION_OFFERING)) {
//                    newObservation.setValue(i, offeringID);
//                } else if (fields.getField(i).getName().equals(SubField.OBSERVATION_END_TIME)) {
//
//                    // Date phenomenonTimeStatic = new Date();
//                    // short year = 2012;
//                    // short month = 8;
//                    // short day = 16;
//                    // short hour = 15;
//                    // short minute = 45;
//                    // short second = 0;
//                    // phenomenonTimeStatic.setYear(year);
//                    // phenomenonTimeStatic.setMonth(month);
//                    // phenomenonTimeStatic.setDate(day);
//                    // phenomenonTimeStatic.setHours(hour);
//                    // phenomenonTimeStatic.setMinutes(minute);
//                    // phenomenonTimeStatic.setSeconds(second);
//                    // LOGGER.info("phenomenonTimeStatic to set: '" +
//                    // phenomenonTimeStatic + "'");
//                    // newObservation.setValue(i, phenomenonTimeStatic);
//
//                    LOGGER.info("phenomenonTime to set: '" + phenomenonTime + "'");
//                    newObservation.setValue(i, phenomenonTime);
//                } else if (fields.getField(i).getName().equals(SubField.OBSERVATION_PROPERTY)) {
//                    newObservation.setValue(i, observedPropertyID);
//                } else if (fields.getField(i).getName().equals(SubField.OBSERVATION_PROCEDURE)) {
//                    newObservation.setValue(i, procedureID);
//                } else if (fields.getField(i).getName().equals(SubField.OBSERVATION_FEATURE)) {
//                    newObservation.setValue(i, featureID);
//                } else if (fields.getField(i).getName().equals(SubField.OBSERVATION_NUMERIC_VALUE)) {
//                    newObservation.setValue(i, result);
//                }
//            }
//            LOGGER.info("New observation created.");
//
//            newObservation.store();
//            LOGGER.info("New observation stored.");
//
//            int observationID = newObservation.getOID();
//            LOGGER.info("New observation successfully added to DB: " + observationID);
//
//            gdb.getWorkspace().stopEditOperation();
//            gdb.getWorkspace().stopEditing(true);
//
//            return observationID;
//
//        } catch (Exception e) {
//            gdb.getWorkspace().stopEditOperation();
//            gdb.getWorkspace().stopEditing(false);
//
//            LOGGER.severe("There was a problem while trying to insert new observation: \n" + ExceptionSupporter.createStringFromStackTrace(e));
//            throw e;
//        }
    }
    
    /**
     * @return the maximum ObjectID of all observations.
     * @throws IOException
     * @throws AutomationException
     */
    protected int getObservationMaxID() throws AutomationException, IOException
    {
    	// TODO: insert observation needs to be adjusted to new AQ e-Reporting data model
    	
    	throw new UnsupportedOperationException();
    	
//        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();
//
//        queryDef.setTables(Table.OBSERVATION);
//
//        queryDef.setSubFields("MAX(" + SubField.OBSERVATION_OBJECTID + ")");
//
//        // evaluate the database query
//        ICursor cursor = queryDef.evaluate();
//
//        IRow row = cursor.nextRow();
//
//        return (Integer) row.getValue(0);
    }
}
