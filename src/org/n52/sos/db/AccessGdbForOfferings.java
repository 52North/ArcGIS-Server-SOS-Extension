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

import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimePeriod;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.dataTypes.ObservedProperty;
import org.n52.util.Utilities;
import org.n52.util.logging.Log;

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
public class AccessGdbForOfferings {

    static Logger LOGGER = Logger.getLogger(AccessGdbForOfferings.class.getName());

    private AccessGDB gdb;
    
    private Collection<ObservationOffering> observationOfferingsCache;

    public AccessGdbForOfferings(AccessGDB accessGDB) {
        this.gdb = accessGDB;
    }
    
//    /**
//     * DUMMY
//     */
//    public Collection<ObservationOffering> getObservationOfferings() 
//    {
//        LOGGER.info("Creating new ObservationOfferings.");
//        
//        List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
//        
//        try {
//            Envelope envelope = new Envelope();
//            ObservationOffering offering = new ObservationOffering("id", "name", new String[] { "observedProperties" }, "procedureIdentifier", envelope, new TimePeriod("2013-01-01/2013-03-31"));
//            offerings.add(offering);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return offerings;
//    }
    
    /**
     * This method can be used to retrieve all {@link ObservationOffering}s
     * associated with the SOS.
     * 
     * @return all offerings from the Geodatabase
     * @throws IOException
     */
    public Collection<ObservationOffering> getObservationOfferings() throws IOException
    {
        if (observationOfferingsCache == null) { //TODO Do we need to update this cache at some point?
            
            List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
            
            // create request to get all offerings an the procedures
            IQueryDef queryDef = gdb.getWorkspace().createQueryDef();

            // set tables
            List<String> tables = new ArrayList<String>();
            tables.add(Table.PROCEDURE);
            queryDef.setTables(gdb.createCommaSeparatedList(tables));

            // set sub fields
            List<String> subFields = new ArrayList<String>();
            subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
            queryDef.setSubFields(gdb.createCommaSeparatedList(subFields));
            
            // evaluate the database query
            ICursor cursor = queryDef.evaluate();

            // convert cursor entries to abstract observations
            Fields fields = (Fields) cursor.getFields();

            IRow row;
            while ((row = cursor.nextRow()) != null) {
                
                // procedure identifier
                String procedureIdentifier = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID)));
                
                // offering name
                String name = procedureIdentifier;
                
                // offering id
                String id = procedureIdentifier;
                
                ObservationOffering offering = new ObservationOffering(id, name, null, procedureIdentifier, null, null);

                offerings.add(offering);
            }

            for (ObservationOffering offering : offerings) {
                
                LOGGER.info("Working on offering " + offerings.indexOf(offering) + " out of " + offerings.size());
                
                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // request the timeperiod
                LOGGER.info("Request observations for Offering " + offering.getId() + " to get timeperiod");
                
                IQueryDef queryDefTime = gdb.getWorkspace().createQueryDef();
                
                // set tables
                queryDefTime.setTables(Table.OBSERVATION);
                queryDefTime.setTables(Table.VALUE);
                                
                // set sub fields
                List<String> subFieldsOff = new ArrayList<String>();
                subFieldsOff.add("MIN(" + gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END)+") AS MINTIME");
                subFieldsOff.add("MAX(" + gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END)+") AS MAXTIME");
                queryDefTime.setSubFields(gdb.createCommaSeparatedList(subFieldsOff));
                queryDefTime.setWhereClause(SubField.OBSERVATION_FK_PROCEDURE + " = " + offering.getId());

                ICursor cursorOffering = queryDefTime.evaluate();
                
                IRow nextRow = cursorOffering.nextRow();
                
                Object startValue = nextRow.getValue(0);
                Object endValue = nextRow.getValue(1);
                
                // start time stamp
                ITimePosition startTime = gdb.createTimePosition(startValue);                

                // end time stamp
                ITimePosition endTime = gdb.createTimePosition(endValue);
                
                // add time extent to offering
                if (startTime != null && endTime != null) {
                    offering.setTimeExtent(new TimePeriod(startTime, endTime));
                }
                
                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // set observed property
                LOGGER.info("Request observed properties for the offering " + offering.getId());

                IQueryDef queryDefProp = gdb.getWorkspace().createQueryDef();

                // set tables
                List<String> tablesProp = new ArrayList<String>();
                tablesProp.add(Table.OBSERVATION);
                tablesProp.add(Table.PROPERTY);
                queryDefProp.setTables(gdb.createCommaSeparatedList(tablesProp));
                LOGGER.info("Tables clause := " + queryDefProp.getTables());

                // set sub fields
                List<String> subFieldsProp = new ArrayList<String>();
                subFieldsProp.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
                queryDefProp.setSubFields(gdb.createCommaSeparatedList(subFieldsProp));
                LOGGER.info("Subfields clause := " + queryDefProp.getSubFields());

                // create where clause with joins and constraints
                StringBuffer whereClauseProp = new StringBuffer();
                whereClauseProp.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROPERTY) + " = " + gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
                whereClauseProp.append(" AND ");
                whereClauseProp.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE) + " = " + offering.getId());
                queryDefProp.setWhereClause(whereClauseProp.toString());
                LOGGER.info("Where clause := " + queryDefProp.getWhereClause());

                // evaluate the database query
                ICursor cursorProp = queryDefProp.evaluate();

                fields = (Fields) cursorProp.getFields();

                List<String> obsProps = new ArrayList<String>();
                while ((row = cursorProp.nextRow()) != null) {
                    String obsPropID = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID)));
                    obsProps.add(obsPropID);
                }
                offering.setObservedProperties(Utilities.toArray(obsProps));

                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // set envelope through feature positions
                LOGGER.info("Request features for the offering " + offering.getId());

                IQueryDef queryDefFoi = gdb.getWorkspace().createQueryDef();

                // set tables
                List<String> tablesFoi = new ArrayList<String>();
                tablesFoi.add(Table.OBSERVATION);
                tablesFoi.add(Table.FEATUREOFINTEREST);
                queryDefFoi.setTables(gdb.createCommaSeparatedList(tablesFoi));

                // set sub fields
                List<String> subFieldsFoi = new ArrayList<String>();
                subFieldsFoi.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_SHAPE));
                queryDefFoi.setSubFields(gdb.createCommaSeparatedList(subFieldsFoi));

                // create the where clause with joins and constraints
                StringBuffer whereClauseFoi = new StringBuffer();
                whereClauseFoi.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_FEATUREOFINTEREST) + " = " + gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID));
                whereClauseFoi.append(" AND ");
                whereClauseFoi.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE) + " = " + offering.getId());
                queryDefFoi.setWhereClause(whereClauseFoi.toString());
                LOGGER.info("Where clause := " + queryDefFoi.getWhereClause());

                // evaluate the database query
                ICursor cursorFoi = queryDefFoi.evaluate();

                List<Point> points = new ArrayList<Point>();
                fields = (Fields) cursorFoi.getFields();
                while ((row = cursorFoi.nextRow()) != null) {
                    Object shape = row.getValue(fields.findField(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_SHAPE)));
                    if (shape instanceof Point) {
                        points.add((Point) shape);
                    } else {
                        throw new IllegalArgumentException("Could not cast a shape in offering " + offering.getId() + " to a Point");
                    }
                }

                Point[] pointArray = new Point[points.size()];
                for (int i = 0; i < pointArray.length; i++) {
                    pointArray[i] = points.get(i);
                }

                Envelope envelope = new Envelope();
                envelope.defineFromPoints(pointArray);
                offering.setObservedArea(envelope);

            }

            observationOfferingsCache = offerings;
        }
        return observationOfferingsCache;
    }
    
    
}
