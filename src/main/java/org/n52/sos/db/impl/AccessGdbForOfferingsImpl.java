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
import java.util.Iterator;
import java.util.List;

import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimePeriod;
import org.n52.sos.dataTypes.AGSEnvelope;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.db.AccessGdbForOfferings;
import org.n52.util.logging.Logger;

import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IQueryDef;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.Point;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class AccessGdbForOfferingsImpl implements AccessGdbForOfferings {

    static Logger LOGGER = Logger.getLogger(AccessGdbForOfferingsImpl.class.getName());

    private AccessGDBImpl gdb;
    
    private Collection<ObservationOffering> observationOfferingsCache;

    public AccessGdbForOfferingsImpl(AccessGDBImpl accessGDB) {
        this.gdb = accessGDB;
    }
    
    
    /**
     * This method can be used to retrieve all {@link ObservationOffering}s
     * associated with the SOS.
     * 
     * @return all offerings from the Geodatabase
     * @throws IOException
     */
    public synchronized Collection<ObservationOffering> getNetworksAsObservationOfferings() throws IOException
    {
        LOGGER.info("getNetworksAsObservationOfferings() is called. "+System.identityHashCode(this));
        
        List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
        
        // ~~~~~~~~~~~~~~~~~~~~
        // request all networks:
        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();

        // set tables
        List<String> tables = new ArrayList<String>();
        tables.add(Table.NETWORK);
        queryDef.setTables(AccessGDBImpl.createCommaSeparatedList(tables));

        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(AccessGDBImpl.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID));
        queryDef.setSubFields(AccessGDBImpl.createCommaSeparatedList(subFields));
        
        // evaluate the database query
        ICursor cursor = queryDef.evaluate();

        // convert cursor entries to abstract observations
        Fields fields = (Fields) cursor.getFields();

        IRow row;
        while ((row = cursor.nextRow()) != null) {
            
            // We will use the 'network identifier' as the 'offering id' and 'offering name'  
            String networkIdentifier = (String) row.getValue(fields.findField(AccessGDBImpl.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID)));
            // offering name
            String name = networkIdentifier;
            // offering id
            String id = networkIdentifier;
            ObservationOffering offering = new ObservationOffering(id, name, null, networkIdentifier, null, null);

            offerings.add(offering);
        }

        List<ObservationOffering> offeringsWithoutObservations = new ArrayList<ObservationOffering>();
        for (ObservationOffering offering : offerings) {
            LOGGER.debug("Working on offering (id: '" + offering.getId() + "') at index " + offerings.indexOf(offering) + " out of " + offerings.size());
            
            safetySleep(200);
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
            queryDefTime.setTables(AccessGDBImpl.createCommaSeparatedList(tablesTime));
//            LOGGER.info("Tables clause := " + queryDefTime.getTables());
            
            // set sub fields
            List<String> subFieldsOff = new ArrayList<String>();
            subFieldsOff.add("MIN(" + AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END)+") AS MINTIME");
            subFieldsOff.add("MAX(" + AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END)+") AS MAXTIME");
            queryDefTime.setSubFields(AccessGDBImpl.createCommaSeparatedList(subFieldsOff));
            
            // create where clause with joins and constraints
            StringBuilder whereClauseTime = new StringBuilder();
            whereClauseTime.append(AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_FK_OBSERVATION) + " = " + AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_PK_OBSERVATION));
            whereClauseTime.append(" AND ");
            whereClauseTime.append(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_SAMPLINGPOINT) + " = " + AccessGDBImpl.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT));
            whereClauseTime.append(" AND ");
            whereClauseTime.append(AccessGDBImpl.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_FK_STATION) + " = " + AccessGDBImpl.concatTableAndField(Table.STATION, SubField.STATION_PK_STATION));
            whereClauseTime.append(" AND ");
            whereClauseTime.append(AccessGDBImpl.concatTableAndField(Table.STATION, SubField.STATION_FK_NETWORK_GID) + " = " + AccessGDBImpl.concatTableAndField(Table.NETWORK, SubField.NETWORK_PK_NETWOK));
            whereClauseTime.append(" AND ");
            whereClauseTime.append(AccessGDBImpl.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID) + " = '" + offering.getId() + "'");
            queryDefTime.setWhereClause(whereClauseTime.toString());

            LOGGER.debug(String.format("Evaluating time query for network: '%s'", offering.getId()));

            ICursor cursorOffering = queryDefTime.evaluate();
            
            IRow nextRow = cursorOffering.nextRow();
            
            Object startValue = nextRow.getValue(0);
            Object endValue = nextRow.getValue(1);
            
            if (startValue == null || endValue == null) {
            	LOGGER.debug("skipping network");
            	offeringsWithoutObservations.add(offering);
            	continue;
            }
            else {
//                LOGGER.info("start time: " + startValue);
//                LOGGER.info("end time: " + endValue);
                
                // start time stamp
                ITimePosition startTime = AccessGDBImpl.createTimePosition(startValue);                

                // end time stamp
                ITimePosition endTime = AccessGDBImpl.createTimePosition(endValue);
                
                // add time extent to offering
                if (startTime != null && endTime != null) {
                    offering.setTimeExtent(new TimePeriod(startTime, endTime));
                }
            }
            
            safetySleep(200);
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // set observed property
            IQueryDef queryDefProp = gdb.getWorkspace().createQueryDef();

            // set tables
            List<String> tablesProp = new ArrayList<String>();
            tablesProp.add(Table.PROPERTY);
            tablesProp.add(Table.OBSERVATION);
            tablesProp.add(Table.SAMPLINGPOINT);
            tablesProp.add(Table.STATION);
            tablesProp.add(Table.NETWORK);
            queryDefProp.setTables(AccessGDBImpl.createCommaSeparatedList(tablesProp));

            // set sub fields
            List<String> subFieldsProp = new ArrayList<String>();
            subFieldsProp.add(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
            queryDefProp.setSubFields(AccessGDBImpl.createCommaSeparatedList(subFieldsProp));

            // create where clause with joins and constraints
            StringBuilder whereClauseProp = new StringBuilder();
            whereClauseProp.append(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROPERTY) + " = " + AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY));
            whereClauseProp.append(" AND ");
            whereClauseProp.append(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_SAMPLINGPOINT) + " = " + AccessGDBImpl.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT));
            whereClauseProp.append(" AND ");
            whereClauseProp.append(AccessGDBImpl.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_FK_STATION) + " = " + AccessGDBImpl.concatTableAndField(Table.STATION, SubField.STATION_PK_STATION));
            whereClauseProp.append(" AND ");
            whereClauseProp.append(AccessGDBImpl.concatTableAndField(Table.STATION, SubField.STATION_FK_NETWORK_GID) + " = " + AccessGDBImpl.concatTableAndField(Table.NETWORK, SubField.NETWORK_PK_NETWOK));
            whereClauseProp.append(" AND ");
            whereClauseProp.append(AccessGDBImpl.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID) + " = '" + offering.getId() + "'");
            queryDefProp.setWhereClause(whereClauseProp.toString());
            LOGGER.debug(String.format("Evaluating property query for network: '%s'", offering.getId()));

            // evaluate the database query
            ICursor cursorProp = queryDefProp.evaluate();
            
            fields = (Fields) cursorProp.getFields();
            List<String> obsProps = new ArrayList<String>();
            while ((row = cursorProp.nextRow()) != null) {
                String obsPropID = (String) row.getValue(fields.findField(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID)));
                if (! obsProps.contains(obsPropID)) {
                    obsProps.add(obsPropID);
                }
            }
            
            // copy obsProps list to String Array:
            String[] obsPropsArray = new String[obsProps.size()];
            int i=0;
            for (Iterator<String> iterator = obsProps.iterator(); iterator.hasNext();) {
                obsPropsArray[i++] = (String) iterator.next();
            }
            
            offering.setObservedProperties(obsPropsArray);
            
            safetySleep(200);
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // set envelope through feature positions
            IQueryDef queryDefFoi = gdb.getWorkspace().createQueryDef();
            
            // set tables
            List<String> tablesFoi = new ArrayList<String>();
            tablesFoi.add(Table.FEATUREOFINTEREST);
            tablesFoi.add(Table.OBSERVATION);
            tablesFoi.add(Table.SAMPLINGPOINT);
            tablesFoi.add(Table.STATION);
            tablesFoi.add(Table.NETWORK);
            queryDefFoi.setTables(AccessGDBImpl.createCommaSeparatedList(tablesFoi));
            
            // set sub fields
            List<String> subFieldsFoi = new ArrayList<String>();
            subFieldsFoi.add(AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_SHAPE));
            queryDefFoi.setSubFields(AccessGDBImpl.createCommaSeparatedList(subFieldsFoi));
            
            // create the where clause with joins and constraints
            StringBuilder whereClauseFoi = new StringBuilder();
            whereClauseFoi.append(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_FEATUREOFINTEREST) + " = " + AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST));
            whereClauseFoi.append(" AND ");
            whereClauseFoi.append(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_SAMPLINGPOINT) + " = " + AccessGDBImpl.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT));
            whereClauseFoi.append(" AND ");
            whereClauseFoi.append(AccessGDBImpl.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_FK_STATION) + " = " + AccessGDBImpl.concatTableAndField(Table.STATION, SubField.STATION_PK_STATION));
            whereClauseFoi.append(" AND ");
            whereClauseFoi.append(AccessGDBImpl.concatTableAndField(Table.STATION, SubField.STATION_FK_NETWORK_GID) + " = " + AccessGDBImpl.concatTableAndField(Table.NETWORK, SubField.NETWORK_PK_NETWOK));
            whereClauseFoi.append(" AND ");
            whereClauseFoi.append(AccessGDBImpl.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID) + " = '" + offering.getId() + "'");
            queryDefFoi.setWhereClause(whereClauseFoi.toString());
//                LOGGER.info("Where clause := " + queryDefFoi.getWhereClause());

            LOGGER.debug(String.format("Evaluating FOI query for network: '%s'", offering.getId()));
            // evaluate the database query
            ICursor cursorFoi = queryDefFoi.evaluate();
            
            List<Point> points = new ArrayList<Point>();
            fields = (Fields) cursorFoi.getFields();
            while ((row = cursorFoi.nextRow()) != null) {
                Object shape = row.getValue(fields.findField(AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_SHAPE)));
                if (shape instanceof Point) {
                    points.add((Point) shape);
                } else {
                    throw new IllegalArgumentException("Could not cast a shape in offering " + offering.getId() + " to a Point");
                }
            }

            Point[] pointArray = new Point[points.size()];
            for (int j = 0; j < pointArray.length; j++) {
                pointArray[j] = points.get(j);
            }

            Envelope envelope = new Envelope();
            envelope.defineFromPoints(pointArray);
            offering.setObservedArea(new AGSEnvelope(envelope));
            
        }
        
        offerings.removeAll(offeringsWithoutObservations);
        
        LOGGER.info("Networks with observations: "+offerings.size());
        
        return offerings;
    }
    
    private void safetySleep(int i) {
    	try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			LOGGER.warn(e.getMessage(), e);
		}		
	}


	/**
     * META: Currently this method is not used. Instead {@link getNetworksAsObservationOfferings()} is used.
     * 
     * This method can be used to retrieve all {@link ObservationOffering}s associated with the SOS.
     * Thereby, there is 1 ObservationOffering per 1 Procedure.
     * 
     * @return all offerings from the Geodatabase
     * @throws IOException
     */
    public synchronized Collection<ObservationOffering> getProceduresAsObservationOfferings() throws IOException
    {
        LOGGER.debug("getObservationOfferings() is called.");
        
        if (observationOfferingsCache != null) { //TODO Do we need to update this cache at some point?
            LOGGER.debug("Using Offerings cache.");
        }
        else { 
            
            List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
            
            // create request to get all offerings an the procedures
            IQueryDef queryDef = gdb.getWorkspace().createQueryDef();

            // set tables
            List<String> tables = new ArrayList<String>();
            tables.add(Table.PROCEDURE);
            queryDef.setTables(AccessGDBImpl.createCommaSeparatedList(tables));

            // set sub fields
            List<String> subFields = new ArrayList<String>();
            subFields.add(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
            queryDef.setSubFields(AccessGDBImpl.createCommaSeparatedList(subFields));
            
            // evaluate the database query
            ICursor cursor = queryDef.evaluate();

            // convert cursor entries to abstract observations
            Fields fields = (Fields) cursor.getFields();

            IRow row;
            while ((row = cursor.nextRow()) != null) {
                
                // We will use the 'procedure identifier' also as the 'offering id' and 'offering name', since there is only one procedure per offering.  
                //
                // procedure identifier
                String procedureIdentifier = (String) row.getValue(fields.findField(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID)));
                // offering name
                String name = procedureIdentifier;
                // offering id
                String id = procedureIdentifier;
                ObservationOffering offering = new ObservationOffering(id, name, null, procedureIdentifier, null, null);

                offerings.add(offering);
            }

            for (ObservationOffering offering : offerings) {
                
                LOGGER.debug("Working on offering (id: '" + offering.getId() + "') at index " + offerings.indexOf(offering) + " out of " + offerings.size());
                
                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // request the timeperiod
                IQueryDef queryDefTime = gdb.getWorkspace().createQueryDef();
                
                // set tables
                List<String> tablesTime = new ArrayList<String>();
                tablesTime.add(Table.OBSERVATION);
                tablesTime.add(Table.VALUE);
                tablesTime.add(Table.PROCEDURE);
                queryDefTime.setTables(AccessGDBImpl.createCommaSeparatedList(tablesTime));
//                LOGGER.info("Tables clause := " + queryDefTime.getTables());
                                
                // set sub fields
                List<String> subFieldsOff = new ArrayList<String>();
                subFieldsOff.add("MIN(" + AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END)+") AS MINTIME");
                subFieldsOff.add("MAX(" + AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END)+") AS MAXTIME");
                queryDefTime.setSubFields(AccessGDBImpl.createCommaSeparatedList(subFieldsOff));
                
                // create where clause with joins and constraints
                StringBuilder whereClauseTime = new StringBuilder();
                whereClauseTime.append(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE) + " = " + AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE));
                whereClauseTime.append(" AND ");
                whereClauseTime.append(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID) + " = '" + offering.getId() + "'");
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
                    ITimePosition startTime = AccessGDBImpl.createTimePosition(startValue);                
    
                    // end time stamp
                    ITimePosition endTime = AccessGDBImpl.createTimePosition(endValue);
                    
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
                    queryDefProp.setTables(AccessGDBImpl.createCommaSeparatedList(tablesProp));
//                    LOGGER.info("Tables clause := " + queryDefProp.getTables());
    
                    // set sub fields
                    List<String> subFieldsProp = new ArrayList<String>();
                    subFieldsProp.add(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
                    queryDefProp.setSubFields(AccessGDBImpl.createCommaSeparatedList(subFieldsProp));
//                    LOGGER.info("Subfields clause := " + queryDefProp.getSubFields());
    
                    // create where clause with joins and constraints
                    StringBuilder whereClauseProp = new StringBuilder();
                    whereClauseProp.append(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROPERTY) + " = " + AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY));
                    whereClauseProp.append(" AND ");
                    whereClauseProp.append(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE) + " = " + AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE));
                    whereClauseProp.append(" AND ");
                    whereClauseProp.append(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID) + " = '" + offering.getId() + "'");
                    queryDefProp.setWhereClause(whereClauseProp.toString());
//                    LOGGER.info("Where clause := " + queryDefProp.getWhereClause());
    
                    // evaluate the database query
                    ICursor cursorProp = queryDefProp.evaluate();
                    
                    fields = (Fields) cursorProp.getFields();
                    List<String> obsProps = new ArrayList<String>();
                    while ((row = cursorProp.nextRow()) != null) {
                        String obsPropID = (String) row.getValue(fields.findField(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID)));
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
                    queryDefFoi.setTables(AccessGDBImpl.createCommaSeparatedList(tablesFoi));
                    
                    // set sub fields
                    List<String> subFieldsFoi = new ArrayList<String>();
                    subFieldsFoi.add(AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_SHAPE));
                    queryDefFoi.setSubFields(AccessGDBImpl.createCommaSeparatedList(subFieldsFoi));
                    
                    // create the where clause with joins and constraints
                    StringBuilder whereClauseFoi = new StringBuilder();
                    whereClauseFoi.append(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_FEATUREOFINTEREST) + " = " + AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST));
                    whereClauseFoi.append(" AND ");
                    whereClauseFoi.append(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE) + " = " + AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE));
                    whereClauseFoi.append(" AND ");
                    whereClauseFoi.append(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID) + " = '" + offering.getId() + "'");
                    queryDefFoi.setWhereClause(whereClauseFoi.toString());
//                    LOGGER.info("Where clause := " + queryDefFoi.getWhereClause());
    
                    // evaluate the database query
                    ICursor cursorFoi = queryDefFoi.evaluate();
                    
                    List<Point> points = new ArrayList<Point>();
                    fields = (Fields) cursorFoi.getFields();
                    while ((row = cursorFoi.nextRow()) != null) {
                        Object shape = row.getValue(fields.findField(AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_SHAPE)));
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
                    offering.setObservedArea(new AGSEnvelope(envelope));
                }
                // no observations associated with this offering/procedure yet, so an empty envelope is attached:
                else {
                    Envelope envelope = new Envelope();
                    offering.setObservedArea(new AGSEnvelope(envelope));
                }
            }

            observationOfferingsCache = offerings;
        }
        return observationOfferingsCache;
    }
    
    
}
