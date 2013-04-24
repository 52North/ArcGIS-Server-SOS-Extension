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
    
    /**
     * DUMMY
     */
    public Collection<ObservationOffering> getObservationOfferings() 
    {
        LOGGER.info("Creating new ObservationOfferings.");
        
        List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
        
        try {
            Envelope envelope = new Envelope();
            ObservationOffering offering = new ObservationOffering("id", "name", new String[] { "observedProperties" }, "procedureIdentifier", envelope, new TimePeriod("2013-01-01/2013-03-31"));
            offerings.add(offering);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return offerings;
    }
    
//    /**
//     * This method can be used to retrieve all {@link ObservationOffering}s
//     * associated with the SOS.
//     * 
//     * @return all offerings from the Geodatabase
//     * @throws IOException
//     */
//    public Collection<ObservationOffering> getObservationOfferings() throws IOException
//    {
//        if (observationOfferingsCache == null) { //TODO Do we need to update this cache at some point?
//            
//            List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
//            
//            // create request to get all offerings an the procedures
//            IQueryDef queryDef = workspace.createQueryDef();
//
//            // set tables
//            List<String> tables = new ArrayList<String>();
//            tables.add(Table.OFFERING);
//            tables.add(Table.PROCEDURE);
//            queryDef.setTables(createCommaSeparatedList(tables));
//
//            // set sub fields
//            List<String> subFields = new ArrayList<String>();
//            subFields.add(concatTableAndField(Table.OFFERING, SubField.OFFERING_OBJECTID));
//            subFields.add(concatTableAndField(Table.OFFERING, SubField.OFFERING_OFFERING_NAME));
//            subFields.add(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID));
//            queryDef.setSubFields(createCommaSeparatedList(subFields));
//
//            // create the where clause with joins and constraints
//            StringBuffer whereClause = new StringBuffer();
//
//            // join the tables
//            whereClause.append(concatTableAndField(Table.OFFERING, SubField.OFFERING_PROCEDURE) + " = " + concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_OBJECTID));
//
//            queryDef.setWhereClause(whereClause.toString());
//
//            // evaluate the database query
//            ICursor cursor = queryDef.evaluate();
//
//            // convert cursor entries to abstract observations
//            Fields fields = (Fields) cursor.getFields();
//
//            IRow row;
//            while ((row = cursor.nextRow()) != null) {
//                // offering id
//                String id = row.getValue(fields.findField(concatTableAndField(Table.OFFERING, SubField.OFFERING_OBJECTID))).toString();
//
//                // offering name
//                String name = (String) row.getValue(fields.findField(concatTableAndField(Table.OFFERING, SubField.OFFERING_OFFERING_NAME)));
//
//                // procedure identifier
//                String procedureIdentifier = (String) row.getValue(fields.findField(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID)));
//
//                ObservationOffering offering = new ObservationOffering(id, name, null, procedureIdentifier, null, null);
//
//                offerings.add(offering);
//            }
//
//            for (ObservationOffering offering : offerings) {
//                
//                // request the timeperiod
//                LOGGER.info(offerings.indexOf(offering) + "/" + offerings.size() + ": Request observations for Offering " + offering.getId() + " to get timeperiod");
//                
//                IQueryDef queryDefOffering = workspace.createQueryDef();
//                // set tables
//                queryDefOffering.setTables(Table.OBSERVATION);
//                                
//                // set sub fields
//                List<String> subFieldsOff = new ArrayList<String>();
//                subFieldsOff.add("MIN(" + concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_START_TIME)+") AS MINTIME");
//                subFieldsOff.add("MAX(" + concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_START_TIME)+") AS MAXTIME");
//                queryDefOffering.setSubFields(gdb.createCommaSeparatedList(subFieldsOff));
//                queryDefOffering.setWhereClause(SubField.OBSERVATION_OFFERING + " = " + offering.getId());
//
//                ICursor cursorOffering = queryDefOffering.evaluate();
//                
//                IRow nextRow = cursorOffering.nextRow();
//                
//                Object startValue = nextRow.getValue(0);
//                Object endValue = nextRow.getValue(1);
//                
//                // start time stamp
//                ITimePosition startTime = gdb.createTimePosition(startValue);                
//
//                // end time stamp
//                ITimePosition endTime = gdb.createTimePosition(endValue);
//                
//                // add time extent to offering
//                if (startTime != null && endTime != null) {
//                    offering.setTimeExtent(new TimePeriod(startTime, endTime));
//                }
//                
//                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//                // set observed property
//                LOGGER.info("Request observed properties for the offering " + offering.getId());
//
//                IQueryDef queryDefProp = workspace.createQueryDef();
//
//                // set tables
//                List<String> tablesProp = new ArrayList<String>();
//                tablesProp.add(Table.PROP_OFF);
//                tablesProp.add(Table.PROPERTY);
//                queryDefProp.setTables(gdb.createCommaSeparatedList(tablesProp));
//
//                // set sub fields
//                List<String> subFieldsProp = new ArrayList<String>();
//                subFieldsProp.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_DATA_TYPE));
//                subFieldsProp.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION));
//                subFieldsProp.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_UOM));
//                queryDefProp.setSubFields(gdb.createCommaSeparatedList(subFieldsProp));
//
//                // create the where clause with joins and constraints
//                StringBuffer whereClauseProp = new StringBuffer();
//
//                // join the tables
//                whereClauseProp.append(concatTableAndField(Table.PROP_OFF, SubField.PROP_OFF_PROPERTY_ID) + " = " + concatTableAndField(Table.PROPERTY, SubField.PROPERTY_OBJECTID));
//                whereClauseProp.append(" AND ");
//                whereClauseProp.append(concatTableAndField(Table.PROP_OFF, SubField.PROP_OFF_OFFERING_ID) + " = " + offering.getId());
//
//                LOGGER.info(whereClauseProp.toString());
//
//                queryDefProp.setWhereClause(whereClauseProp.toString());
//
//                // evaluate the database query
//                ICursor cursorProp = queryDefProp.evaluate();
//
//                fields = (Fields) cursorProp.getFields();
//
//                List<ObservedProperty> obsProps = new ArrayList<ObservedProperty>();
//                while ((row = cursorProp.nextRow()) != null) {
//                    String dataType = (String) row.getValue(fields.findField(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_DATA_TYPE)));
//                    String propDesc = (String) row.getValue(fields.findField(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION)));
//                    String uom = (String) row.getValue(fields.findField(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_UOM)));
//                    obsProps.add(new ObservedProperty(propDesc, dataType, uom));
//                }
//                ObservedProperty[] obsPropsArray = new ObservedProperty[obsProps.size()];
//                for (int i = 0; i < obsProps.size(); i++) {
//                    obsPropsArray[i] = obsProps.get(i);
//                }
//                offering.setObservedProperties(obsPropsArray);
//
//                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//                // set envelope through feature positions
//                LOGGER.info("Request features for the offering " + offering.getId());
//
//                IQueryDef queryDefFoi = workspace.createQueryDef();
//
//                // set tables
//                List<String> tablesFoi = new ArrayList<String>();
//                tablesFoi.add(Table.FOI_OFF);
//                tablesFoi.add(Table.FEATURE);
//                queryDefFoi.setTables(gdb.createCommaSeparatedList(tablesFoi));
//
//                // set sub fields
//                List<String> subFieldsFoi = new ArrayList<String>();
//                subFieldsFoi.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_SHAPE));
//                queryDefFoi.setSubFields(gdb.createCommaSeparatedList(subFieldsFoi));
//
//                // create the where clause with joins and constraints
//                StringBuffer whereClauseFoi = new StringBuffer();
//
//                // join the tables
//                whereClauseFoi.append(concatTableAndField(Table.FOI_OFF, SubField.FOI_OFF_FOI_ID) + " = " + concatTableAndField(Table.FEATURE, SubField.FEATURE_OBJECTID));
//                whereClauseFoi.append(" AND ");
//                whereClauseFoi.append(concatTableAndField(Table.FOI_OFF, SubField.FOI_OFF_OFFERING_ID) + " = " + offering.getId());
//
//                LOGGER.info(whereClauseFoi.toString());
//
//                queryDefFoi.setWhereClause(whereClauseFoi.toString());
//
//                // evaluate the database query
//                ICursor cursorFoi = queryDefFoi.evaluate();
//
//                List<Point> points = new ArrayList<Point>();
//                fields = (Fields) cursorFoi.getFields();
//                while ((row = cursorFoi.nextRow()) != null) {
//                    Object shape = row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_SHAPE)));
//                    if (shape instanceof Point) {
//                        points.add((Point) shape);
//                    } else {
//                        LOGGER.warning("Could not cast a shape in offering " + offering.getId() + " to a Point");
//                    }
//                }
//
//                Point[] pointArray = new Point[points.size()];
//                for (int i = 0; i < pointArray.length; i++) {
//                    pointArray[i] = points.get(i);
//                }
//
//                Envelope envelope = new Envelope();
//                envelope.defineFromPoints(pointArray);
//                offering.setObservedArea(envelope);
//
//            }
//
//            observationOfferingsCache = offerings;
//        }
//        return observationOfferingsCache;
//    }
    
    
}
