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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimePeriod;
import org.n52.sos.dataTypes.ObservationOffering;
import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IQueryDef;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.Point;

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
    
//  /**
//  * DUMMY
//  */
// public Collection<ObservationOffering> getObservationOfferings() 
// {
//     LOGGER.info("Creating DUMMY ObservationOfferings.");
//     
//     List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
//     
//     try {
//         Envelope envelope = new Envelope();
//         ObservationOffering offering = new ObservationOffering("id", "name", new String[] { "observedProperties" }, "procedureIdentifier", envelope, new TimePeriod("2013-01-01/2013-03-31"));
//         offerings.add(offering);
//     } catch (Exception e) {
//         e.printStackTrace();
//     }
//     return offerings;
// }
    
    /**
     * This method can be used to retrieve all {@link ObservationOffering}s
     * associated with the SOS.
     * 
     * @return all offerings from the Geodatabase
     * @throws IOException
     */
    public Collection<ObservationOffering> getNetworksAsObservationOfferings() throws IOException
    {
        LOGGER.info("getObservationOfferings() is called.");
        
        List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
        
        // create request to get all networks as offerings:
        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();

        // set tables
        List<String> tables = new ArrayList<String>();
        tables.add(Table.NETWORK);
        queryDef.setTables(gdb.createCommaSeparatedList(tables));

        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID));
        queryDef.setSubFields(gdb.createCommaSeparatedList(subFields));
        
        // evaluate the database query
        ICursor cursor = queryDef.evaluate();

        // convert cursor entries to abstract observations
        Fields fields = (Fields) cursor.getFields();

        IRow row;
        while ((row = cursor.nextRow()) != null) {
            
            // We will use the 'network identifier' as the 'offering id' and 'offering name'  
            String networkIdentifier = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID)));
            // offering name
            String name = networkIdentifier;
            // offering id
            String id = networkIdentifier;
            ObservationOffering offering = new ObservationOffering(id, name, null, networkIdentifier, null, null);

            offerings.add(offering);
        }

        for (ObservationOffering offering : offerings) {
            
            LOGGER.info("Working on offering (id: '" + offering.getId() + "') at index " + offerings.indexOf(offering) + " out of " + offerings.size());
            
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // request the timeperiod
            IQueryDef queryDefTime = gdb.getWorkspace().createQueryDef();
            
            // set tables
            List<String> tablesTime = new ArrayList<String>();
            tablesTime.add(Table.VALUE);
            tablesTime.add(Table.OBSERVATION);
            tablesTime.add(Table.SAMPLINGPOINT);
            tablesTime.add(Table.STATION);
            tablesTime.add(Table.NETWORK);
            queryDefTime.setTables(gdb.createCommaSeparatedList(tablesTime));
//            LOGGER.info("Tables clause := " + queryDefTime.getTables());
            
            // set sub fields
            List<String> subFieldsOff = new ArrayList<String>();
            subFieldsOff.add("MIN(" + gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END)+") AS MINTIME");
            subFieldsOff.add("MAX(" + gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END)+") AS MAXTIME");
            queryDefTime.setSubFields(gdb.createCommaSeparatedList(subFieldsOff));
            
            // create where clause with joins and constraints
            StringBuilder whereClauseTime = new StringBuilder();
            whereClauseTime.append(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_FK_OBSERVATION) + " = " + gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_PK_OBSERVATION));
            whereClauseTime.append(" AND ");
            whereClauseTime.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_SAMPLINGPOINT) + " = " + gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT));
            whereClauseTime.append(" AND ");
            whereClauseTime.append(gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_FK_STATION) + " = " + gdb.concatTableAndField(Table.STATION, SubField.STATION_PK_STATION));
            whereClauseTime.append(" AND ");
            whereClauseTime.append(gdb.concatTableAndField(Table.STATION, SubField.STATION_FK_NETWORK_GID) + " = " + gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_PK_NETWOK));
            whereClauseTime.append(" AND ");
            whereClauseTime.append(gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID) + " = '" + offering.getId() + "'");
            queryDefTime.setWhereClause(whereClauseTime.toString());
            LOGGER.info("Where clause := " + queryDefTime.getWhereClause());

            ICursor cursorOffering = queryDefTime.evaluate();
            
            IRow nextRow = cursorOffering.nextRow();
            
            Object startValue = nextRow.getValue(0);
            Object endValue = nextRow.getValue(1);
                
            boolean noObservationsForOffering = false;
            if (startValue == null || endValue == null) {
                noObservationsForOffering = true;
            }
            else {
//                LOGGER.info("start time: " + startValue);
//                LOGGER.info("end time: " + endValue);
                
                // start time stamp
                ITimePosition startTime = gdb.createTimePosition(startValue);                

                // end time stamp
                ITimePosition endTime = gdb.createTimePosition(endValue);
                
                // add time extent to offering
                if (startTime != null && endTime != null) {
                    offering.setTimeExtent(new TimePeriod(startTime, endTime));
                }
            }
            
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // set observed property
            
            if (noObservationsForOffering == false) {
                IQueryDef queryDefProp = gdb.getWorkspace().createQueryDef();

                // set tables
                List<String> tablesProp = new ArrayList<String>();
                tablesProp.add(Table.PROPERTY);
                tablesProp.add(Table.OBSERVATION);
                tablesProp.add(Table.SAMPLINGPOINT);
                tablesProp.add(Table.STATION);
                tablesProp.add(Table.NETWORK);
                queryDefProp.setTables(gdb.createCommaSeparatedList(tablesProp));
                LOGGER.info("Tables clause := " + queryDefProp.getTables());

                // set sub fields
                List<String> subFieldsProp = new ArrayList<String>();
                subFieldsProp.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
                queryDefProp.setSubFields(gdb.createCommaSeparatedList(subFieldsProp));
                LOGGER.info("Subfields clause := " + queryDefProp.getSubFields());

                // create where clause with joins and constraints
                StringBuilder whereClauseProp = new StringBuilder();
                whereClauseProp.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROPERTY) + " = " + gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY));
                whereClauseProp.append(" AND ");
                whereClauseProp.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_SAMPLINGPOINT) + " = " + gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT));
                whereClauseProp.append(" AND ");
                whereClauseProp.append(gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_FK_STATION) + " = " + gdb.concatTableAndField(Table.STATION, SubField.STATION_PK_STATION));
                whereClauseProp.append(" AND ");
                whereClauseProp.append(gdb.concatTableAndField(Table.STATION, SubField.STATION_FK_NETWORK_GID) + " = " + gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_PK_NETWOK));
                whereClauseProp.append(" AND ");
                whereClauseProp.append(gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID) + " = '" + offering.getId() + "'");
                queryDefProp.setWhereClause(whereClauseProp.toString());
                LOGGER.info("Where clause := " + queryDefProp.getWhereClause());

                // evaluate the database query
                ICursor cursorProp = queryDefProp.evaluate();
                
                fields = (Fields) cursorProp.getFields();
                List<String> obsProps = new ArrayList<String>();
                while ((row = cursorProp.nextRow()) != null) {
                    String obsPropID = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID)));
                    if (! obsProps.contains(obsPropID)) {
                        obsProps.add(obsPropID);
                    }
                }
                
                // copy obsProps list to String Array:
                String[] obsPropsArray = new String[obsProps.size()];
                int i=0;
                for (Iterator<String> iterator = obsProps.iterator(); iterator.hasNext();) {
                    obsPropsArray[i] = (String) iterator.next();
                    i++;
                }
                
                offering.setObservedProperties(obsPropsArray);
            }
            // no observations associated with this offering/procedure yet, so an empty String array is attached:
            else {
                offering.setObservedProperties(new String[0]);
            }
            
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // set envelope through feature positions
            
            if (noObservationsForOffering == false) {
                IQueryDef queryDefFoi = gdb.getWorkspace().createQueryDef();
                
                // set tables
                List<String> tablesFoi = new ArrayList<String>();
                tablesFoi.add(Table.FEATUREOFINTEREST);
                tablesFoi.add(Table.OBSERVATION);
                tablesFoi.add(Table.SAMPLINGPOINT);
                tablesFoi.add(Table.STATION);
                tablesFoi.add(Table.NETWORK);
                queryDefFoi.setTables(gdb.createCommaSeparatedList(tablesFoi));
                
                // set sub fields
                List<String> subFieldsFoi = new ArrayList<String>();
                subFieldsFoi.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_SHAPE));
                queryDefFoi.setSubFields(gdb.createCommaSeparatedList(subFieldsFoi));
                
                // create the where clause with joins and constraints
                StringBuilder whereClauseFoi = new StringBuilder();
                whereClauseFoi.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_FEATUREOFINTEREST) + " = " + gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST));
                whereClauseFoi.append(" AND ");
                whereClauseFoi.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_SAMPLINGPOINT) + " = " + gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT));
                whereClauseFoi.append(" AND ");
                whereClauseFoi.append(gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_FK_STATION) + " = " + gdb.concatTableAndField(Table.STATION, SubField.STATION_PK_STATION));
                whereClauseFoi.append(" AND ");
                whereClauseFoi.append(gdb.concatTableAndField(Table.STATION, SubField.STATION_FK_NETWORK_GID) + " = " + gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_PK_NETWOK));
                whereClauseFoi.append(" AND ");
                whereClauseFoi.append(gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID) + " = '" + offering.getId() + "'");
                queryDefFoi.setWhereClause(whereClauseFoi.toString());
//                LOGGER.info("Where clause := " + queryDefFoi.getWhereClause());

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
            // no observations associated with this offering/procedure yet, so an empty envelope is attached:
            else {
                Envelope envelope = new Envelope();
                offering.setObservedArea(envelope);
            }
        }
        
        
        return offerings;
    }
    
    /**
     * This method can be used to retrieve all {@link ObservationOffering}s
     * associated with the SOS.
     * 
     * @return all offerings from the Geodatabase
     * @throws IOException
     */
    public Collection<ObservationOffering> getProceduresAsObservationOfferings() throws IOException
    {
        LOGGER.info("getObservationOfferings() is called.");
        
        if (observationOfferingsCache != null) { //TODO Do we need to update this cache at some point?
            LOGGER.info("Using Offerings cache.");
        }
        else { 
            
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
                
                // We will use the 'procedure identifier' also as the 'offering id' and 'offering name', since there is only one procedure per offering.  
                //
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
                
                LOGGER.info("Working on offering (id: '" + offering.getId() + "') at index " + offerings.indexOf(offering) + " out of " + offerings.size());
                
                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // request the timeperiod
                IQueryDef queryDefTime = gdb.getWorkspace().createQueryDef();
                
                // set tables
                List<String> tablesTime = new ArrayList<String>();
                tablesTime.add(Table.OBSERVATION);
                tablesTime.add(Table.VALUE);
                tablesTime.add(Table.PROCEDURE);
                queryDefTime.setTables(gdb.createCommaSeparatedList(tablesTime));
//                LOGGER.info("Tables clause := " + queryDefTime.getTables());
                                
                // set sub fields
                List<String> subFieldsOff = new ArrayList<String>();
                subFieldsOff.add("MIN(" + gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END)+") AS MINTIME");
                subFieldsOff.add("MAX(" + gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END)+") AS MAXTIME");
                queryDefTime.setSubFields(gdb.createCommaSeparatedList(subFieldsOff));
                
                // create where clause with joins and constraints
                StringBuilder whereClauseTime = new StringBuilder();
                whereClauseTime.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE) + " = " + gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE));
                whereClauseTime.append(" AND ");
                whereClauseTime.append(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID) + " = '" + offering.getId() + "'");
                queryDefTime.setWhereClause(whereClauseTime.toString());
//                LOGGER.info("Where clause := " + queryDefTime.getWhereClause());

                ICursor cursorOffering = queryDefTime.evaluate();
                
                IRow nextRow = cursorOffering.nextRow();
                
                Object startValue = nextRow.getValue(0);
                Object endValue = nextRow.getValue(1);
                    
                boolean noObservationsForOffering = false;
                if (startValue == null || endValue == null) {
                    noObservationsForOffering = true;
                }
                else {
//                    LOGGER.info("start time: " + startValue);
//                    LOGGER.info("end time: " + endValue);
                    
                    // start time stamp
                    ITimePosition startTime = gdb.createTimePosition(startValue);                
    
                    // end time stamp
                    ITimePosition endTime = gdb.createTimePosition(endValue);
                    
                    // add time extent to offering
                    if (startTime != null && endTime != null) {
                        offering.setTimeExtent(new TimePeriod(startTime, endTime));
                    }
                }
                
                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // set observed property
                
                if (noObservationsForOffering == false) {
                    IQueryDef queryDefProp = gdb.getWorkspace().createQueryDef();
    
                    // set tables
                    List<String> tablesProp = new ArrayList<String>();
                    tablesProp.add(Table.OBSERVATION);
                    tablesProp.add(Table.PROPERTY);
                    tablesProp.add(Table.PROCEDURE);
                    queryDefProp.setTables(gdb.createCommaSeparatedList(tablesProp));
//                    LOGGER.info("Tables clause := " + queryDefProp.getTables());
    
                    // set sub fields
                    List<String> subFieldsProp = new ArrayList<String>();
                    subFieldsProp.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
                    queryDefProp.setSubFields(gdb.createCommaSeparatedList(subFieldsProp));
//                    LOGGER.info("Subfields clause := " + queryDefProp.getSubFields());
    
                    // create where clause with joins and constraints
                    StringBuilder whereClauseProp = new StringBuilder();
                    whereClauseProp.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROPERTY) + " = " + gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY));
                    whereClauseProp.append(" AND ");
                    whereClauseProp.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE) + " = " + gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE));
                    whereClauseProp.append(" AND ");
                    whereClauseProp.append(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID) + " = '" + offering.getId() + "'");
                    queryDefProp.setWhereClause(whereClauseProp.toString());
//                    LOGGER.info("Where clause := " + queryDefProp.getWhereClause());
    
                    // evaluate the database query
                    ICursor cursorProp = queryDefProp.evaluate();
                    
                    fields = (Fields) cursorProp.getFields();
                    List<String> obsProps = new ArrayList<String>();
                    while ((row = cursorProp.nextRow()) != null) {
                        String obsPropID = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID)));
                        obsProps.add(obsPropID);
                    }
                    
                    // copy obsProps list to String Array:
                    String[] obsPropsArray = new String[obsProps.size()];
                    int i=0;
                    for (Iterator<String> iterator = obsProps.iterator(); iterator.hasNext();) {
                        obsPropsArray[i] = (String) iterator.next();
                        i++;
                    }
                    
                    offering.setObservedProperties(obsPropsArray);
                }
                // no observations associated with this offering/procedure yet, so an empty String array is attached:
                else {
                    offering.setObservedProperties(new String[0]);
                }
                
                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // set envelope through feature positions
                
                if (noObservationsForOffering == false) {
                    IQueryDef queryDefFoi = gdb.getWorkspace().createQueryDef();
                    
                    // set tables
                    List<String> tablesFoi = new ArrayList<String>();
                    tablesFoi.add(Table.OBSERVATION);
                    tablesFoi.add(Table.FEATUREOFINTEREST);
                    tablesFoi.add(Table.PROCEDURE);
                    queryDefFoi.setTables(gdb.createCommaSeparatedList(tablesFoi));
                    
                    // set sub fields
                    List<String> subFieldsFoi = new ArrayList<String>();
                    subFieldsFoi.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_SHAPE));
                    queryDefFoi.setSubFields(gdb.createCommaSeparatedList(subFieldsFoi));
                    
                    // create the where clause with joins and constraints
                    StringBuilder whereClauseFoi = new StringBuilder();
                    whereClauseFoi.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_FEATUREOFINTEREST) + " = " + gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST));
                    whereClauseFoi.append(" AND ");
                    whereClauseFoi.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE) + " = " + gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE));
                    whereClauseFoi.append(" AND ");
                    whereClauseFoi.append(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID) + " = '" + offering.getId() + "'");
                    queryDefFoi.setWhereClause(whereClauseFoi.toString());
//                    LOGGER.info("Where clause := " + queryDefFoi.getWhereClause());
    
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
                // no observations associated with this offering/procedure yet, so an empty envelope is attached:
                else {
                    Envelope envelope = new Envelope();
                    offering.setObservedArea(envelope);
                }
            }

            observationOfferingsCache = offerings;
        }
        return observationOfferingsCache;
    }
    
    
}
