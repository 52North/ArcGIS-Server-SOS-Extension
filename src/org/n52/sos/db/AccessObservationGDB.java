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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.n52.gml.Identifier;
import org.n52.om.observation.AbstractObservation;
import org.n52.om.observation.Measurement;
import org.n52.om.observation.MultiMeasurement;
import org.n52.om.observation.TextObservation;
import org.n52.om.observation.collections.GenericObservationCollection;
import org.n52.om.result.MeasureResult;
import org.n52.om.result.MultiMeasureResult;
import org.n52.om.result.TextResult;
import org.n52.om.sampling.AQDSample;
import org.n52.om.sampling.Feature;
import org.n52.oxf.valueDomains.time.ITime;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimeConverter;
import org.n52.oxf.valueDomains.time.TimeFactory;
import org.n52.oxf.valueDomains.time.TimePeriod;
import org.n52.oxf.valueDomains.time.TimePosition;
import org.n52.sos.Constants;
import org.n52.sos.SOSExt;
import org.n52.sos.dataTypes.ContactDescription;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.dataTypes.ObservedProperty;
import org.n52.sos.dataTypes.Procedure;
import org.n52.sos.dataTypes.ServiceDescription;
import org.n52.util.ExceptionSupporter;
import org.n52.util.logging.Log;

import com.esri.arcgis.carto.IMapServer3;
import com.esri.arcgis.datasourcesGDB.SdeWorkspaceFactory;
import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.IMemoryRelationshipClassFactory;
import com.esri.arcgis.geodatabase.IObjectClass;
import com.esri.arcgis.geodatabase.IObjectClassProxy;
import com.esri.arcgis.geodatabase.IQueryDef;
import com.esri.arcgis.geodatabase.IRelQueryTableFactory;
import com.esri.arcgis.geodatabase.IRelationshipClass;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.ISpatialFilter;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.IWorkspaceFactory;
import com.esri.arcgis.geodatabase.MemoryRelationshipClassFactory;
import com.esri.arcgis.geodatabase.RelQueryTableFactory;
import com.esri.arcgis.geodatabase.SpatialFilter;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriFieldType;
import com.esri.arcgis.geodatabase.esriRelCardinality;
import com.esri.arcgis.geodatabase.esriSpatialRelEnum;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.Multipoint;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.server.json.JSONObject;
import com.esri.arcgis.system.ServerUtilities;

/**
 * This class is responsible for the database connection and access.
 * 
 * @author <a href="mailto:j.schulte@52north.org">Jan Schulte</a>
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class AccessObservationGDB {

    static Logger LOGGER = Logger.getLogger(AccessObservationGDB.class.getName());

    private SOSExt sos;

    private Workspace workspace;
    
    private static int MAX_NUMBER_OF_RESULTS;

    private Collection<ObservationOffering> observationOfferings;
    
    private ServiceDescription serviceDescription;

    private com.esri.arcgis.geodatabase.Table fc;

    /**
     * Creates an AccessObservationGDB object and connects to the DB of the
     * ArcGIS MapServer handed over as a parameter.
     * 
     * @throws AutomationException
     * @throws IOException
     */
    public AccessObservationGDB(SOSExt sos) throws AutomationException, IOException {

        LOGGER.info("Creating AccessObservationGDB.");

        this.sos = sos;

        IMapServer3 ms = (IMapServer3) sos.getMapServerDataAccess();
        String mapName = ms.getDefaultMapName();

        // Load properties for data access
        Properties props = new Properties();
        props.load(AccessObservationGDB.class.getResourceAsStream("/arcGisSos.properties"));

        // init the table names for accessing the geodatabase:
        Table.initTableNames(props);

        // init the field names:
        SubField.initSubfieldNames(props);

        // init maxNumberOfResults:
        MAX_NUMBER_OF_RESULTS = Integer.parseInt(props.getProperty("database.maxNumberOfResults"));

        fc = new com.esri.arcgis.geodatabase.Table(sos.getMapServerDataAccess().getDataSource(mapName, 6));
        LOGGER.info("FeatureClass name: " + fc.getName());

        workspace = new Workspace(fc.getWorkspace());
        
        // initialize the capabilities
        long start = System.currentTimeMillis();
        getServiceDescription();
        getObservationOfferings();
        long delta = System.currentTimeMillis() - start;
        LOGGER.info("Initialize in " + delta/1000 + " seconds!");

        LOGGER.info("Workspace name: " + workspace.getName());

        LOGGER.info("End of creating AccessObservationGDB.");
    }

    /**
     * Creates an AccessObservationGDB object and connects to the DB specified
     * in the arcGisSosLocal.properties file.
     * 
     * This constructor is for testing this class NOT for usage in the ArcGIS
     * Server.
     * 
     * @throws AutomationException
     * @throws IOException
     */
    public AccessObservationGDB() throws AutomationException, IOException {

        LOGGER.info("Creating AccessObservationGDB.");

        // Load properties for data access
        Properties props = new Properties();
        props.load(AccessObservationGDB.class.getResourceAsStream("/arcGisSosLocal.properties"));
        String dbPath = props.getProperty("database.path");

        LOGGER.info("Using database connection '" + dbPath + "'.");

        // init the table names for accessing the geodatabase:
        Table.initTableNames(props);

        // init the field names:
        SubField.initSubfieldNames(props);

        // init maxNumberOfResults:
        MAX_NUMBER_OF_RESULTS = Integer.parseInt(props.getProperty("database.maxNumberOfResults"));

        // Workspace creation
        IWorkspaceFactory factory = new SdeWorkspaceFactory();// FileGDBWorkspaceFactory();
        workspace = new Workspace(factory.openFromFile(dbPath, 0));

        fc = new com.esri.arcgis.geodatabase.Table(workspace.openTable(Table.OBSERVATION));

        // Log data access setup
        LOGGER.info("Using Geodatabase: " + dbPath);
    }

    /**
     * This method can be used to retrieve a {@link ServiceDescription} of the
     * SOS.
     * @throws IOException 
     * @throws AutomationException 
     * 
     * @throws Exception
     */
    public ServiceDescription getServiceDescription() throws IOException
    {
        if(this.serviceDescription == null) {
            LOGGER.info("Create new ServiceDescription");
            
            String title = sos.getSosTitle();
            String description = sos.getSosDescription();
            String[] keywordArray = sos.getSosKeywords().split(",");
            String providerName = sos.getSosProviderName();
            String providerSite = sos.getSosProviderSite();        
            String individualName = sos.getSosContactPersonName();
            String positionName = sos.getSosContactPersonPosition();
            String phone = sos.getSosContactPersonPhone();
            String facsimile = sos.getSosContactPersonFax();
            String deliveryPoint = sos.getSosContactPersonAddress();
            String city = sos.getSosContactPersonCity();
            String administrativeArea = sos.getSosContactPersonAdminArea();
            String postalCode  = sos.getSosContactPersonPostalCode();
            String country = sos.getSosContactPersonCountry();
            String electronicMailAddress = sos.getSosContactPersonEmail();
         
            ContactDescription serviceContact = new ContactDescription(individualName, positionName, phone, facsimile, deliveryPoint, city, administrativeArea, postalCode, country, electronicMailAddress);
            
            // request procedures
            Collection<Procedure> procedures = getProcedures(null);

            this.serviceDescription = new ServiceDescription(title, description, keywordArray, providerName, providerSite, new ContactDescription[]{serviceContact}, procedures); 
        }
        return this.serviceDescription;
    }

    /**
     * This method can be used to retrieve all {@link ObservationOffering}s
     * associated with the SOS.
     * 
     * @return all offerings from the Geodatabase
     * @throws IOException
     * @throws UnknownHostException
     */
    public Collection<ObservationOffering> getObservationOfferings() throws IOException
    {
        if (observationOfferings == null) {
            
            List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();

            LOGGER.info("Start getObservationOffering");

            // create request to get all offerings an the procedures
            IQueryDef queryDef = workspace.createQueryDef();

            // set tables
            List<String> tables = new ArrayList<String>();
            tables.add(Table.OFFERING);
            tables.add(Table.PROCEDURE);
            queryDef.setTables(createCommaSeparatedList(tables));

            // set sub fields
            List<String> subFields = new ArrayList<String>();
            subFields.add(concatTableAndField(Table.OFFERING, SubField.OFFERING_OBJECTID));
            subFields.add(concatTableAndField(Table.OFFERING, SubField.OFFERING_OFFERING_NAME));
            subFields.add(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID));
            queryDef.setSubFields(createCommaSeparatedList(subFields));

            // create the where clause with joins and constraints
            StringBuffer whereClause = new StringBuffer();

            // join the tables
            whereClause.append(concatTableAndField(Table.OFFERING, SubField.OFFERING_PROCEDURE) + " = " + concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_OBJECTID));

            queryDef.setWhereClause(whereClause.toString());

            // evaluate the database query
            ICursor cursor = queryDef.evaluate();

            // convert cursor entries to abstract observations
            Fields fields = (Fields) cursor.getFields();

            IRow row;
            while ((row = cursor.nextRow()) != null) {
                // offering id
                String id = row.getValue(fields.findField(concatTableAndField(Table.OFFERING, SubField.OFFERING_OBJECTID))).toString();

                // offering name
                String name = (String) row.getValue(fields.findField(concatTableAndField(Table.OFFERING, SubField.OFFERING_OFFERING_NAME)));

                // procedure identifier
                String procedureIdentifier = (String) row.getValue(fields.findField(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID)));

                ObservationOffering offering = new ObservationOffering(id, name, null, procedureIdentifier, null, null);

                offerings.add(offering);
            }

            for (ObservationOffering offering : offerings) {
                
                // request the timeperiod
                LOGGER.info(offerings.indexOf(offering) + "/" + offerings.size() + ": Request observations for Offering " + offering.getId() + " to get timeperiod");
                
                IQueryDef queryDefOffering = workspace.createQueryDef();
                // set tables
                queryDefOffering.setTables(Table.OBSERVATION);
                                
                // set sub fields
                List<String> subFieldsOff = new ArrayList<String>();
                subFieldsOff.add("MIN(" + concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_START_TIME)+") AS MINTIME");
                subFieldsOff.add("MAX(" + concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_START_TIME)+") AS MAXTIME");
                queryDefOffering.setSubFields(createCommaSeparatedList(subFieldsOff));
                queryDefOffering.setWhereClause(SubField.OBSERVATION_OFFERING + " = " + offering.getId());

                ICursor cursorOffering = queryDefOffering.evaluate();
                
                IRow nextRow = cursorOffering.nextRow();
                
                Object startValue = nextRow.getValue(0);
                Object endValue = nextRow.getValue(1);
                
                // start time stamp
                ITimePosition startTime = createTimePosition(startValue);                

                // end time stamp
                ITimePosition endTime = createTimePosition(endValue);
                
                // add time extent to offering
                if (startTime != null && endTime != null) {
                    offering.setTimeExtent(new TimePeriod(startTime, endTime));
                }
                
                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // set observed property
                LOGGER.info("Request observed properties for the offering " + offering.getId());

                IQueryDef queryDefProp = workspace.createQueryDef();

                // set tables
                List<String> tablesProp = new ArrayList<String>();
                tablesProp.add(Table.PROP_OFF);
                tablesProp.add(Table.PROPERTY);
                queryDefProp.setTables(createCommaSeparatedList(tablesProp));

                // set sub fields
                List<String> subFieldsProp = new ArrayList<String>();
                subFieldsProp.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_DATA_TYPE));
                subFieldsProp.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION));
                subFieldsProp.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_UOM));
                queryDefProp.setSubFields(createCommaSeparatedList(subFieldsProp));

                // create the where clause with joins and constraints
                StringBuffer whereClauseProp = new StringBuffer();

                // join the tables
                whereClauseProp.append(concatTableAndField(Table.PROP_OFF, SubField.PROP_OFF_PROPERTY_ID) + " = " + concatTableAndField(Table.PROPERTY, SubField.PROPERTY_OBJECTID));
                whereClauseProp.append(" AND ");
                whereClauseProp.append(concatTableAndField(Table.PROP_OFF, SubField.PROP_OFF_OFFERING_ID) + " = " + offering.getId());

                LOGGER.info(whereClauseProp.toString());

                queryDefProp.setWhereClause(whereClauseProp.toString());

                // evaluate the database query
                ICursor cursorProp = queryDefProp.evaluate();

                fields = (Fields) cursorProp.getFields();

                List<ObservedProperty> obsProps = new ArrayList<ObservedProperty>();
                while ((row = cursorProp.nextRow()) != null) {
                    String dataType = (String) row.getValue(fields.findField(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_DATA_TYPE)));
                    String propDesc = (String) row.getValue(fields.findField(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION)));
                    String uom = (String) row.getValue(fields.findField(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_UOM)));
                    obsProps.add(new ObservedProperty(propDesc, dataType, uom));
                }
                ObservedProperty[] obsPropsArray = new ObservedProperty[obsProps.size()];
                for (int i = 0; i < obsProps.size(); i++) {
                    obsPropsArray[i] = obsProps.get(i);
                }
                offering.setObservedProperties(obsPropsArray);

                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // set envelope through feature positions
                LOGGER.info("Request features for the offering " + offering.getId());

                IQueryDef queryDefFoi = workspace.createQueryDef();

                // set tables
                List<String> tablesFoi = new ArrayList<String>();
                tablesFoi.add(Table.FOI_OFF);
                tablesFoi.add(Table.FEATURE);
                queryDefFoi.setTables(createCommaSeparatedList(tablesFoi));

                // set sub fields
                List<String> subFieldsFoi = new ArrayList<String>();
                subFieldsFoi.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_SHAPE));
                queryDefFoi.setSubFields(createCommaSeparatedList(subFieldsFoi));

                // create the where clause with joins and constraints
                StringBuffer whereClauseFoi = new StringBuffer();

                // join the tables
                whereClauseFoi.append(concatTableAndField(Table.FOI_OFF, SubField.FOI_OFF_FOI_ID) + " = " + concatTableAndField(Table.FEATURE, SubField.FEATURE_OBJECTID));
                whereClauseFoi.append(" AND ");
                whereClauseFoi.append(concatTableAndField(Table.FOI_OFF, SubField.FOI_OFF_OFFERING_ID) + " = " + offering.getId());

                LOGGER.info(whereClauseFoi.toString());

                queryDefFoi.setWhereClause(whereClauseFoi.toString());

                // evaluate the database query
                ICursor cursorFoi = queryDefFoi.evaluate();

                List<Point> points = new ArrayList<Point>();
                fields = (Fields) cursorFoi.getFields();
                while ((row = cursorFoi.nextRow()) != null) {
                    Object shape = row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_SHAPE)));
                    if (shape instanceof Point) {
                        points.add((Point) shape);
                    } else {
                        LOGGER.warning("Could not cast a shape in offering " + offering.getId() + " to a Point");
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

            observationOfferings = offerings;
        }
        return observationOfferings;
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

        IQueryDef queryDef = workspace.createQueryDef();

        // set tables
        List<String> tables = new ArrayList<String>();
        tables.add(Table.PROCEDURE);
        tables.add(Table.CONTACT_DESCRIPTION);

        queryDef.setTables(createCommaSeparatedList(tables));

        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_OBJECTID));
        subFields.add(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID));
        subFields.add(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_LONG_NAME));
        subFields.add(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_INTENDED_APPLICATION));
        subFields.add(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_SHAPE));
        subFields.add(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_SENSOR_TYPE));
        subFields.add(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_DESCRIPTION));

        subFields.add(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_INDIVIDUAL_NAME));
        subFields.add(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_POSITION_NAME));
        subFields.add(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_PHONE));
        subFields.add(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_FACSIMILE));
        subFields.add(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_DELIVERY_POINT));
        subFields.add(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_CITY));
        subFields.add(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_ADMINISTRATIVE_AREA));
        subFields.add(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_POSTAL_CODE));
        subFields.add(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_COUNTRY));
        subFields.add(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_ELECTRONIC_MAIL_ADDRESS));

        queryDef.setSubFields(createCommaSeparatedList(subFields));

        StringBuffer whereClause = new StringBuffer();

        whereClause.append(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_CONTACT) + " = " + concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_OBJECTID));

        if (procedureIdentifierArray != null) {
            whereClause.append(" AND ");
            whereClause.append(createOrClause(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID), procedureIdentifierArray));
        }

        queryDef.setWhereClause(whereClause.toString());

        LOGGER.info(queryDef.getWhereClause());

        // evaluate the database query
        ICursor cursor = queryDef.evaluate();

        Fields fields = (Fields) cursor.getFields();
        IRow row;
        List<Procedure> procedures = new ArrayList<Procedure>();
        while ((row = cursor.nextRow()) != null) {

            String objectId = row.getValue(fields.findField(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_OBJECTID))).toString();

            String id = (String) row.getValue(fields.findField(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID)));

            String name = (String) row.getValue(fields.findField(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_LONG_NAME)));

            String description = (String) row.getValue(fields.findField(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_DESCRIPTION)));

            String intendedApplication = (String) row.getValue(fields.findField(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_INTENDED_APPLICATION)));

            String sensorType = (String) row.getValue(fields.findField(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_SENSOR_TYPE)));

            Object shape = row.getValue(fields.findField(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_SHAPE)));
            Envelope observedArea = null;
            if (shape instanceof Multipoint) {
                Multipoint multipoint = (Multipoint) shape;
                if (!multipoint.isEmpty()) {
                    observedArea = (Envelope) multipoint.getEnvelope();
                }
            }

            // contact
            String individualName = (String) row.getValue(fields.findField(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_INDIVIDUAL_NAME)));
            String positionName = (String) row.getValue(fields.findField(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_POSITION_NAME)));
            String phone = (String) row.getValue(fields.findField(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_PHONE)));
            String facsimile = (String) row.getValue(fields.findField(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_FACSIMILE)));
            String deliveryPoint = (String) row.getValue(fields.findField(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_DELIVERY_POINT)));
            String city = (String) row.getValue(fields.findField(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_CITY)));
            String administrativeArea = (String) row.getValue(fields.findField(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_ADMINISTRATIVE_AREA)));
            String postalCode = (String) row.getValue(fields.findField(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_POSTAL_CODE)));
            String country = (String) row.getValue(fields.findField(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_COUNTRY)));
            String electronicMailAddress = (String) row.getValue(fields.findField(concatTableAndField(Table.CONTACT_DESCRIPTION, SubField.CONTACT_DESCRIPTION_ELECTRONIC_MAIL_ADDRESS)));
            ContactDescription contact = new ContactDescription(individualName, positionName, phone, facsimile, deliveryPoint, city, administrativeArea, postalCode, country, electronicMailAddress);

            procedures.add(new Procedure(objectId, id, name, description, intendedApplication, sensorType, observedArea, contact, null));
        }

        //
        // now find outputs (= observed properties) for each procedure
        //
        for (Iterator<Procedure> iterator = procedures.iterator(); iterator.hasNext();) {

            Procedure procedure = (Procedure) iterator.next();

            LOGGER.info("Request observed properties for the procedure " + procedure.getId());

            IQueryDef queryDefProp = workspace.createQueryDef();

            // set tables
            List<String> tablesProp = new ArrayList<String>();
            tablesProp.add(Table.PROC_PROP);
            tablesProp.add(Table.PROPERTY);
            queryDefProp.setTables(createCommaSeparatedList(tablesProp));

            // set sub fields
            List<String> subFieldsProp = new ArrayList<String>();
            subFieldsProp.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_DATA_TYPE));
            subFieldsProp.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION));
            subFieldsProp.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_UOM));
            queryDefProp.setSubFields(createCommaSeparatedList(subFieldsProp));

            // create the where clause with joins and constraints
            StringBuffer whereClauseProp = new StringBuffer();

            // join the tables
            whereClauseProp.append(concatTableAndField(Table.PROC_PROP, SubField.PROC_PROP_PROPERTY_ID) + " = " + concatTableAndField(Table.PROPERTY, SubField.PROPERTY_OBJECTID));
            whereClauseProp.append(" AND ");
            whereClauseProp.append(concatTableAndField(Table.PROC_PROP, SubField.PROC_PROP_PROCEDURE_ID) + " = " + procedure.getObjectId());

            LOGGER.info(whereClauseProp.toString());

            queryDefProp.setWhereClause(whereClauseProp.toString());

            // evaluate the database query
            ICursor cursorProp = queryDefProp.evaluate();

            fields = (Fields) cursorProp.getFields();

            List<ObservedProperty> obsProps = new ArrayList<ObservedProperty>();
            while ((row = cursorProp.nextRow()) != null) {
                String dataType = (String) row.getValue(fields.findField(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_DATA_TYPE)));
                String propDesc = (String) row.getValue(fields.findField(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION)));
                String uom = (String) row.getValue(fields.findField(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_UOM)));

                obsProps.add(new ObservedProperty(propDesc, dataType, uom));
            }
            ObservedProperty[] obsPropsArray = new ObservedProperty[obsProps.size()];
            for (int i = 0; i < obsProps.size(); i++) {
                obsPropsArray[i] = obsProps.get(i);
            }
            procedure.setOutputs(obsPropsArray);
        }

        return procedures;
    }

    /**
     * @return all observations with the specified identifiers.
     */
    public GenericObservationCollection getObservations(String[] observationIdentifiers) throws Exception
    {
        GenericObservationCollection observations = new GenericObservationCollection();
        IQueryDef queryDef = workspace.createQueryDef();

        // set tables
        List<String> tables = new ArrayList<String>();
        tables.add(Table.OBSERVATION);
        tables.add(Table.FEATURE);
        tables.add(Table.PROCEDURE);
        tables.add(Table.PROPERTY);
        tables.add(Table.OFFERING);
        queryDef.setTables(createCommaSeparatedList(tables));

        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_OBJECTID));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_TEXT_VALUE));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_NUMERIC_VALUE));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_START_TIME));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_END_TIME));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_VALIDITY));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_VERIFICATION));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_DATA_CAPTURE));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_TIME_COVERAGE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_NAME));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_OBJECTID));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_DESCRIPTION));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_SHAPE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_CODE_SPACE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_ID_VALUE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_SAMPLED_FEATURE_URL));
        subFields.add(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID));
        subFields.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION));
        subFields.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_UOM));
        subFields.add(concatTableAndField(Table.OFFERING, SubField.OFFERING_OFFERING_NAME));

        queryDef.setSubFields(createCommaSeparatedList(subFields));

        // create the where clause with joins and constraints
        StringBuffer whereClause = new StringBuffer();

        // joins
        whereClause.append(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FEATURE) + " = " + concatTableAndField(Table.FEATURE, SubField.FEATURE_OBJECTID));
        whereClause.append(" AND ");
        whereClause.append(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_PROCEDURE) + " = " + concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_OBJECTID));
        whereClause.append(" AND ");
        whereClause.append(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_PROPERTY) + " = " + concatTableAndField(Table.PROPERTY, SubField.PROPERTY_OBJECTID));
        whereClause.append(" AND ");
        whereClause.append(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_OFFERING) + " = " + concatTableAndField(Table.OFFERING, SubField.OFFERING_OBJECTID));

        whereClause.append(" AND ");
        whereClause.append(createOrClause(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_OBJECTID), observationIdentifiers));

        queryDef.setWhereClause(whereClause.toString());

        // Log the query clause
        LOGGER.info(queryDef.getWhereClause());

        // evaluate the database query
        ICursor cursor = queryDef.evaluate();

        // convert cursor entries to abstract observations
        Fields fields = (Fields) cursor.getFields();

        IRow row = cursor.nextRow();
        int count = 0;
        while (row != null && count < MAX_NUMBER_OF_RESULTS) {
            count++;
            observations.addObservation(createObservation(row, fields));
            row = cursor.nextRow();
        }

        return observations;
    }

    /**
     * This method can be used to retrieve all observations complying to the
     * filter as specified by the parameters. The method basically reflects the
     * SOS:GetObservation() operation on Java level.
     * 
     * If one of the method parameters is <b>null</b>, it shall not be
     * considered in the query.
     * 
     * @return all observations from the Geodatabase which comply to the
     *         specified parameters.
     * @throws Exception
     */
    public GenericObservationCollection getObservations(String[] offerings,
            String[] featuresOfInterest,
            String[] observedProperties,
            String[] procedures,
            String spatialFilter,
            String temporalFilter,
            String where) throws Exception
    {
        GenericObservationCollection observations = new GenericObservationCollection();
        IQueryDef queryDef = workspace.createQueryDef();

        // set tables
        List<String> tables = new ArrayList<String>();
        tables.add(Table.OBSERVATION);
        tables.add(Table.FEATURE);
        tables.add(Table.PROCEDURE);
        tables.add(Table.PROPERTY);
        tables.add(Table.OFFERING);
        queryDef.setTables(createCommaSeparatedList(tables));

        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_OBJECTID));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_TEXT_VALUE));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_NUMERIC_VALUE));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_END_TIME));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_START_TIME));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_VALIDITY));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_VERIFICATION));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_DATA_CAPTURE));
        subFields.add(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_TIME_COVERAGE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_NAME));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_OBJECTID));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_DESCRIPTION));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_SHAPE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_CODE_SPACE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_ID_VALUE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_SAMPLED_FEATURE_URL));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_LOCAL_ID));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_NAMESPACE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_INLET_HEIGHT));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_BUILDING_DISTANCE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_KERB_DISTANCE));
        subFields.add(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID));
        subFields.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION));
        subFields.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_UOM));
        subFields.add(concatTableAndField(Table.OFFERING, SubField.OFFERING_OFFERING_NAME));

        queryDef.setSubFields(createCommaSeparatedList(subFields));

        // create the where clause with joins and constraints
        StringBuffer whereClause = new StringBuffer();

        // joins
        whereClause.append(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FEATURE) + " = " + concatTableAndField(Table.FEATURE, SubField.FEATURE_OBJECTID));
        whereClause.append(" AND ");
        whereClause.append(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_PROCEDURE) + " = " + concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_OBJECTID));
        whereClause.append(" AND ");
        whereClause.append(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_PROPERTY) + " = " + concatTableAndField(Table.PROPERTY, SubField.PROPERTY_OBJECTID));
        whereClause.append(" AND ");
        whereClause.append(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_OFFERING) + " = " + concatTableAndField(Table.OFFERING, SubField.OFFERING_OBJECTID));

        // build query for offerings
        if (offerings != null) {
            whereClause.append(" AND ");
            whereClause.append(createOrClause(concatTableAndField(Table.OFFERING, SubField.OFFERING_OFFERING_NAME), offerings));
        }

        // build query for feature of interest
        if (featuresOfInterest != null) {
            whereClause.append(" AND ");
            whereClause.append(createOrClause(concatTableAndField(Table.FEATURE, SubField.FEATURE_ID_VALUE), featuresOfInterest));
        }

        // build query for observed property
        if (observedProperties != null) {
            whereClause.append(" AND ");
            whereClause.append(createOrClause(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION), observedProperties));
        }

        // build query for procedure
        if (procedures != null) {
            whereClause.append(" AND ");
            whereClause.append(createOrClause(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID), procedures));
        }

        // build query for spatial filter
        if (spatialFilter != null) {
            // get the IDs of all features which are within the specified
            // spatialFilter:
            Collection<Integer> featureList = queryFeatureIDsForSpatialFilter(spatialFilter);

            if (featureList.size() > 0) {
                // append the list of feature IDs:
                whereClause.append(" AND ");
                whereClause.append(createOrClause(concatTableAndField(Table.FEATURE, SubField.FEATURE_OBJECTID), featureList));
            } else {
                LOGGER.warning("The defined spatialFilter '" + spatialFilter + "' did not match any features in the database.");
            }
        }

        // build query for temporal filter
        if (temporalFilter != null) {
            whereClause.append(" AND ");
            whereClause.append(createTemporalClauseSDE(temporalFilter));
        }

        // build query for the where clause
        if (where != null) {
            whereClause.append(" AND ");
            whereClause.append(where);
        }

        queryDef.setWhereClause(whereClause.toString());

        // Log the query clause
        LOGGER.info(queryDef.getWhereClause());

        // evaluate the database query
        ICursor cursor = queryDef.evaluate();

        // convert cursor entries to abstract observations
        Fields fields = (Fields) cursor.getFields();

        IRow row = cursor.nextRow();
        int count = 0;
        while (row != null && count < MAX_NUMBER_OF_RESULTS) {
            count++;
            observations.addObservation(createObservation(row, fields));
            row = cursor.nextRow();
        }

        return observations;
    }

    /**
     * This method can be used to retrieve all features of interest complying to
     * the filter as specified by the parameters. The method basically reflects
     * the SOS:GetFeatureOfInterest() operation on Java level.
     * 
     * If one of the method parameters is <b>null</b>, it shall not be
     * considered in the query.
     * 
     * @param featuresOfInterest
     *            the names of requested features.
     * @param observedProperties
     *            the descriptions of observed properties.
     * @param procedures
     *            the unique IDs of procedures.
     * @param spatialfilter
     *            a spatial filter that shall be applied.
     * 
     * @return all features of interest from the Geodatabase which comply to the
     *         specified parameters.
     * @throws Exception
     */
    public Collection<Feature> getFeaturesOfInterest(String[] featuresOfInterest,
            String[] observedProperties,
            String[] procedures,
            String spatialFilter) throws Exception
    {
        List<Feature> features = new ArrayList<Feature>();
        IQueryDef queryDef = workspace.createQueryDef();

        // set tables
        List<String> tables = new ArrayList<String>();
        tables.add(Table.FEATURE);
        tables.add(Table.PROCEDURE);
        tables.add(Table.PROPERTY);
        tables.add(Table.OFFERING);
        tables.add(Table.FOI_OFF);
        tables.add(Table.PROP_OFF);
        queryDef.setTables(createCommaSeparatedList(tables));

        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_NAME));
        subFields.add(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID));
        subFields.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_NAME));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_DESCRIPTION));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_SHAPE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_ID_VALUE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_CODE_SPACE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_SAMPLED_FEATURE_URL));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_LOCAL_ID));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_NAMESPACE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_INLET_HEIGHT));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_BUILDING_DISTANCE));
        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_KERB_DISTANCE));
        queryDef.setSubFields(createCommaSeparatedList(subFields));

        // create the where clause with joins and constraints
        StringBuffer whereClause = new StringBuffer();

        // joins
        whereClause.append(concatTableAndField(Table.OFFERING, SubField.OFFERING_OBJECTID) + " = " + concatTableAndField(Table.FOI_OFF, SubField.FOI_OFF_OFFERING_ID));
        whereClause.append(" AND ");
        whereClause.append(concatTableAndField(Table.FOI_OFF, SubField.FOI_OFF_FOI_ID) + " = " + concatTableAndField(Table.FEATURE, SubField.FEATURE_OBJECTID));
        whereClause.append(" AND ");
        whereClause.append(concatTableAndField(Table.OFFERING, SubField.OFFERING_PROCEDURE) + " = " + concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_OBJECTID));
        whereClause.append(" AND ");
        whereClause.append(concatTableAndField(Table.OFFERING, SubField.OFFERING_OBJECTID) + " = " + concatTableAndField(Table.PROP_OFF, SubField.PROP_OFF_OFFERING_ID));
        whereClause.append(" AND ");
        whereClause.append(concatTableAndField(Table.PROP_OFF, SubField.PROP_OFF_PROPERTY_ID) + " = " + concatTableAndField(Table.PROPERTY, SubField.PROPERTY_OBJECTID));

        // build query for feature of interest
        if (featuresOfInterest != null) {
            whereClause.append(" AND ");
            whereClause.append(createOrClause(concatTableAndField(Table.FEATURE, SubField.FEATURE_ID_VALUE), featuresOfInterest));
        }

        // build query for observed properties
        if (observedProperties != null) {
            whereClause.append(" AND ");
            whereClause.append(createOrClause(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION), observedProperties));
        }

        // build query for procedures
        if (procedures != null) {
            whereClause.append(" AND ");
            whereClause.append(createOrClause(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID), procedures));
        }

        // build query for spatial filter
        if (spatialFilter != null) {
            // get the IDs of all features which are within the specified
            // spatialFilter:
            Collection<Integer> featureList = queryFeatureIDsForSpatialFilter(spatialFilter);

            if (featureList.size() > 0) {
                // append the list of feature IDs:
                whereClause.append(" AND ");
                whereClause.append(createOrClause(concatTableAndField(Table.FEATURE, SubField.FEATURE_OBJECTID), featureList));
            } else {
                LOGGER.warning("The defined spatialFilter '" + spatialFilter + "' did not match any features in the database.");
            }
        }

        queryDef.setWhereClause(whereClause.toString());

        // Log out the query clause
        LOGGER.info("GetFeatureOfInterest DB query WHERE clause: '" + queryDef.getWhereClause() + "'");

        // evaluate the database query
        ICursor cursor = queryDef.evaluate();

        // convert cursor entries to abstract observations
        Fields fields = (Fields) cursor.getFields();

        IRow row;
        int count = 0;
        while ((row = cursor.nextRow()) != null && count < MAX_NUMBER_OF_RESULTS) {
            count++;
            Feature feature = createFeature(row, fields);
            features.add(feature);
        }

        return features;
    }

    // //////////////////////////// Helper Methods:

    private AbstractObservation createObservation(IRow row,
            Fields fields) throws IOException, AutomationException, URISyntaxException, Exception
    {
        AbstractObservation observation = null;
        // Identifier
        String objectID = row.getValue(fields.findField(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_OBJECTID))).toString();
        Identifier obsIdentifier = new Identifier(null, objectID);

        // start time
        ITime startTime;
        Date startDate = (Date) row.getValue(fields.findField(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_START_TIME)));
        if (startDate != null) {
            ITimePosition startTimePos = createTimeFromDate(startDate, null);
            startTime = startTimePos;
        } else {
            startTime = null;
        }

        // end time
        Date endDate = (Date) row.getValue(fields.findField(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_END_TIME)));
        ITimePosition endTimePos = createTimeFromDate(endDate, null);
        ITime endTime = endTimePos;

        // result time (here, we set resultTime = endTime)
        ITime resultTime = endTimePos;

        // validity
        Short validity = (Short) row.getValue(fields.findField(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_VALIDITY)));
        if (validity == null) {
            validity = Constants.OBSERVATION_VALIDITY;
        }

        // verification
        Short verification = (Short) row.getValue(fields.findField(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_VERIFICATION)));
        if (verification == null) {
            verification = Constants.OBSERVATION_VERIFICATION;
        }

        // procedure
        String procURI = (String) row.getValue(fields.findField(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID)));
        URI procedure = new URI(procURI);

        // observed property
        String obsPropURI = (String) row.getValue(fields.findField(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION)));
        URI observedProperty = new URI(obsPropURI);

        // featureOfInterest
        Feature feature = createFeature(row, fields);

        // result
        Object numValue = row.getValue(fields.findField(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_NUMERIC_VALUE)));
        if (numValue != null) {
            double value = (Double) row.getValue(fields.findField(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_NUMERIC_VALUE)));

            // uom
            String uom = (String) row.getValue(fields.findField(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_UOM)));

            if (startTime == null) {
                MeasureResult result = new MeasureResult(validity, verification, value, uom);
                observation = new Measurement(obsIdentifier, endTime, resultTime, procedure, observedProperty, feature, result);
            } else {
                // data capture
                Short dataCapture = (Short) row.getValue(fields.findField(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_DATA_CAPTURE)));
                if (dataCapture == null) {
                    dataCapture = Constants.OBSERVATION_DATA_CAPTURE;
                }

                // time coverage
                Short timeCoverage = (Short) row.getValue(fields.findField(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_TIME_COVERAGE)));
                if (timeCoverage == null) {
                    timeCoverage = Constants.OBSERVATION_TIME_COVERAGE;
                }
                
                MultiMeasureResult result = new MultiMeasureResult(validity, verification, dataCapture, timeCoverage, value, uom);
                observation = new MultiMeasurement(obsIdentifier, startTime, endTime, resultTime, procedure, observedProperty, feature, result);
            }
        } else if (row.getValue(fields.findField(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_TEXT_VALUE))) != null) {
            String value = (String) row.getValue(fields.findField(concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_NUMERIC_VALUE)));
            TextResult result = new TextResult(value);
            observation = new TextObservation(obsIdentifier, endTime, resultTime, procedure, observedProperty, feature, result);
        }
        return observation;
    }

    /**
     * This method creates a SpatialSamplingFeature of a given row and it's
     * fields
     * 
     * @param row
     *            The row
     * @param fields
     *            The fields
     * @return A SpatialSamplingFeature
     * @throws AutomationException
     * @throws IOException
     * @throws URISyntaxException
     */
    private Feature createFeature(IRow row,
            Fields fields) throws AutomationException, IOException, URISyntaxException
    {
        // identifier (codeSpace URI and idValue)
        String idValue = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_ID_VALUE)));
        String uri = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_CODE_SPACE)));
        URI codeSpace;
        if (uri != null) {
            codeSpace = new URI(uri);
        } else {
            codeSpace = new URI("");
        }
        Identifier identifier = new Identifier(codeSpace, idValue);

        // name
        String name = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_NAME)));

        // description
        String description = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_DESCRIPTION)));

        // sampled Feature
        String sampledFeature = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_SAMPLED_FEATURE_URL)));

        // shape
        Point point = null;
        Object shape = row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_SHAPE)));
        if (shape instanceof Point) {
            point = (Point) shape;
        } else {
            LOGGER.log(Level.WARNING, "Shape of the feature is no point.");
        }
        
        // localID
        String localId = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_LOCAL_ID)));
        if (localId == null) {
            localId = Constants.FEATURE_LOCALID;
        }
        
        // namespace
        String namespace = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_NAMESPACE)));
        if (namespace == null) {
            namespace = Constants.FEATURE_NAMESPACE;
        }
        
        // inletHeight
        Double inletHeight = (Double) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_INLET_HEIGHT)));
        if (inletHeight == null) {
            inletHeight = Constants.FEATURE_INLET_HEIGHT;
        }
        
        // buildingDistance
        Double buildingDistance = (Double) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_BUILDING_DISTANCE)));
        if (buildingDistance == null) {
            buildingDistance = Constants.FEATURE_BUILDING_DISTANCE;
        }
        
        // kerbDistance
        Double kerbDistance = (Double) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_KERB_DISTANCE)));
        if (kerbDistance == null) {
            kerbDistance = Constants.FEATURE_KERB_DISTANCE;
        }

        return new AQDSample(identifier, name, description, sampledFeature, point, localId, namespace, inletHeight, buildingDistance, kerbDistance);
    }

    private ITimePosition createTimePosition(Object startValue)
    {
        ITimePosition timePosition = null;
        if (startValue != null && startValue instanceof Date) {
            Date startTimeAsDate = (Date) startValue;
            // Problem: java.util.Date always sets the time zone to the
            // local time
            // zone, where the SOS is installed.
            // Hence, we have to make it UTC:
            int year = startTimeAsDate.getYear() + 1900;
            int month = startTimeAsDate.getMonth() + 1;
            int day = startTimeAsDate.getDate();
            int hour = startTimeAsDate.getHours();
            int minute = startTimeAsDate.getMinutes();
            int second = startTimeAsDate.getSeconds();
            String startTimeAsISO8601 = TimeConverter.toISO8601(year, month, day, hour, minute, second, "+00:00");
            timePosition = (ITimePosition) TimeFactory.createTime(startTimeAsISO8601);
        }
        return timePosition;
    }

    /**
     * This method creates a comma separated string of a list of string entries
     * 
     * @param list
     *            List of entries
     * @return a comma separated string
     */
    private String createCommaSeparatedList(List<String> list)
    {
        StringBuilder sb = new StringBuilder();
        for (String entry : list) {
            sb.append(entry);
            sb.append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * This method creates an or-clause out of a given field and a list of
     * strings, where all entries of the list will be compared with the field.
     * 
     * @param field
     *            a database field
     * @param list
     *            a String array representing the list of values that are
     *            compared with the field.
     * @return the created or-clause
     */
    protected String createOrClause(String field,
            String[] list)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (String entry : list) {
            sb.append(field + " = '" + entry + "'");
            sb.append(" OR ");
        }
        sb.delete(sb.length() - 4, sb.length());
        sb.append(")");
        return sb.toString();
    }

    /**
     * This method creates an or-clause out of a given field and a Collection of
     * Integers, where all Integer values will be compared with the field.
     * 
     * @param field
     *            a database field
     * @param list
     *            a Collection of Integers representing the list of values that
     *            are compared with the field.
     * @return the created or-clause
     */
    protected String createOrClause(String field,
            Collection<Integer> list)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Integer entry : list) {
            sb.append(field + " = '" + entry + "'");
            sb.append(" OR ");
        }
        sb.delete(sb.length() - 4, sb.length());
        sb.append(")");
        return sb.toString();
    }

    /**
     * This method creates a temporal clause out of a given temporal filter as
     * String.
     * 
     * options of temporal filters:<br>
     * 1.) equals:yyyy-MM-ddTHH:mm:ss+HH:mm<br>
     * 2.) during:yyyy-MM-ddTHH:mm:ss+HH:mm,yyyy-MM-dd HH:mm:ss+HH:mm<br>
     * 3.) after:yyyy-MM-ddTHH:mm:ss+HH:mm<br>
     * 4.) before:yyyy-MM-ddTHH:mm:ss+HH:mm<br>
     * 5.) last:milliseconds,+HH:mm<br>
     * 
     * @param temporalFilter
     *            The temporal filter as specified.
     * @return A database conform request for the temporal filter.
     * @throws IllegalArgumentException
     */
    protected String createTemporalClauseSDE(String temporalFilter) throws IllegalArgumentException
    {
        // TODO JAN: refactor with the new start time
        String clause = null;
        if (temporalFilter.contains("equals:")) {
            String timeInstant = extractTemporalOperandAfterKeyWord(temporalFilter);
            timeInstant = TimeConverter.convertLocalToUTC(timeInstant);
            clause = SubField.OBSERVATION_END_TIME + " = '" + timeInstant + "'";
        } else if (temporalFilter.contains("during:")) {
            String tempOperand = extractTemporalOperandAfterKeyWord(temporalFilter);
            String timeStart = tempOperand.split(",")[0];
            String timeEnd = tempOperand.split(",")[1];
            timeStart = TimeConverter.convertLocalToUTC(timeStart);
            timeEnd = TimeConverter.convertLocalToUTC(timeEnd);
            clause = SubField.OBSERVATION_END_TIME + " BETWEEN '" + timeStart + "' AND '" + timeEnd + "'";
        } else if (temporalFilter.contains("after:")) {
            String timeInstant = extractTemporalOperandAfterKeyWord(temporalFilter);
            timeInstant = TimeConverter.convertLocalToUTC(timeInstant);
            clause = SubField.OBSERVATION_END_TIME + " > '" + timeInstant + "'";
        } else if (temporalFilter.contains("before:")) {
            String timeInstant = extractTemporalOperandAfterKeyWord(temporalFilter);
            timeInstant = TimeConverter.convertLocalToUTC(timeInstant);
            clause = SubField.OBSERVATION_END_TIME + " < '" + timeInstant + "'";
        } else if (temporalFilter.contains("last:")) {
            String durationString = extractTemporalOperandAfterKeyWord(temporalFilter).split(",")[0];
            long duration = Long.parseLong(durationString);

            // convert to UTC, since database is in UTC:
            Calendar utcTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            utcTime.setTimeInMillis(System.currentTimeMillis() - duration);
            int year = utcTime.get(Calendar.YEAR);
            int month = utcTime.get(Calendar.MONTH) + 1;
            int day = utcTime.get(Calendar.DAY_OF_MONTH);
            int hour = utcTime.get(Calendar.HOUR_OF_DAY);
            int minute = utcTime.get(Calendar.MINUTE);
            int second = utcTime.get(Calendar.SECOND);
            String timeInstant = TimeConverter.toISO8601(false, year, month, day, hour, minute, second);

            clause = SubField.OBSERVATION_END_TIME + " > '" + timeInstant + "'";
        } else {
            throw new IllegalArgumentException("Error while parsing the temporal filter.");
        }
        return clause;
    }

    /**
     * This helper method creates an {@link ITimePosition} from a
     * {@link java.util.Date} object (which is in the time zone of the SOS
     * server), and converts it to the requested time zone as defined in the
     * temporalFilter parameter.
     * 
     * @param date
     *            the time stamp of an observation but in the local time zone of
     *            the SOS server.
     * @param temporalFilter
     *            as accepted by the SOS server.
     * @return
     */
    @SuppressWarnings("deprecation")
    protected ITimePosition createTimeFromDate(Date date,
            String temporalFilter)
    {
        // Problem: java.util.Date always sets the time zone to the local time
        // zone, where the SOS is installed.
        // Hence, we have to make it UTC:
        int year = date.getYear() + 1900;
        int month = date.getMonth() + 1;
        int day = date.getDate();
        int hour = date.getHours();
        int minute = date.getMinutes();
        int second = date.getSeconds();
        String TimeAsString = TimeConverter.toISO8601(true, year, month, day, hour, minute, second);

        if (temporalFilter != null) {
            // if a temporalFilter was specified by the client, find out the
            // requested time zone, so that we can convert the UTC time to that
            // one:
            String queriedTimeZoneOffset;
            if (temporalFilter.contains("last:")) {
                queriedTimeZoneOffset = extractTemporalOperandAfterKeyWord(temporalFilter).split(",")[1];
            } else {
                String queriedTimeAsISO8601 = extractTemporalOperandAfterKeyWord(temporalFilter);
                if (queriedTimeAsISO8601.contains(",")) {
                    // time period has been requested; it's fine to only
                    // consider
                    // begin time to find out time zone:
                    queriedTimeAsISO8601 = queriedTimeAsISO8601.split(",")[0];
                }
                queriedTimeZoneOffset = TimeConverter.getTimeZoneOffset(queriedTimeAsISO8601);
            }

            // now, convert to the local time used in the query by the client:
            TimeAsString = TimeConverter.convertUTCToLocal(TimeAsString, queriedTimeZoneOffset);
        }

        ITimePosition time = new TimePosition(TimeAsString);

        return time;
    }

    /**
     * @param temporalFilter
     *            the temporalFilter as accepted by the SOS server, e.g.,
     *            'equals:2010-12-31T15:00:00+02:00'
     * @return the String after the keyword, e.g., '2010-12-31T15:00:00+02:00'.
     */
    protected String extractTemporalOperandAfterKeyWord(String temporalFilter)
    {
        if (temporalFilter.contains("equals:")) {
            return temporalFilter.substring(temporalFilter.indexOf("equals:") + 7);
        } else if (temporalFilter.contains("during:")) {
            return temporalFilter.substring(temporalFilter.indexOf("during:") + 7);
        } else if (temporalFilter.contains("after:")) {
            return temporalFilter.substring(temporalFilter.indexOf("after:") + 6);
        } else if (temporalFilter.contains("before:")) {
            return temporalFilter.substring(temporalFilter.indexOf("before:") + 7);
        } else if (temporalFilter.contains("last:")) {
            return temporalFilter.substring(temporalFilter.indexOf("last:") + 5);
        } else {
            throw new IllegalArgumentException("Error while parsing the temporal filter.");
        }
    }

    /**
     * 
     * @param spatialFilter
     * @return
     * @throws Exception
     */
    protected Collection<Integer> queryFeatureIDsForSpatialFilter(String spatialFilter) throws Exception
    {
        Collection<Integer> featureList = new ArrayList<Integer>();

        IGeometry geometry = ServerUtilities.getGeometryFromJSON(new JSONObject(spatialFilter));
        IFeatureClass features = workspace.openFeatureClass(Table.FEATURE);
        ISpatialFilter spatialQuery = new SpatialFilter();
        spatialQuery.setGeometryByRef(geometry);
        spatialQuery.setGeometryField(features.getShapeFieldName());
        spatialQuery.setSpatialRel(esriSpatialRelEnum.esriSpatialRelIntersects);
        spatialQuery.setSubFields(SubField.FEATURE_OBJECTID);
        IFeatureCursor featureCursor = features.search(spatialQuery, true);

        IFeature feature = featureCursor.nextFeature();
        Fields fields = (Fields) featureCursor.getFields();
        while (feature != null) {
            featureList.add((Integer) feature.getValue(fields.findField(SubField.FEATURE_OBJECTID)));
            feature = featureCursor.nextFeature();
        }

        return featureList;
    }

    /**
     * Concatenates names of a database table and field with a '.'.
     */
    protected String concatTableAndField(String table,
            String field)
    {
        return table + "." + field;
    }

    /**
     * 
     * @param targetTable
     * @param joinTable
     * @param fromField
     * @param toField
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private static ITable createRelQueryTable(ITable targetTable,
            ITable joinTable,
            String fromField,
            String toField) throws Exception
    {
        // Build a memory relationship class.
        IMemoryRelationshipClassFactory memRelClassFactory = new MemoryRelationshipClassFactory();
        // Create object class for the tables since table cannot be cast to
        // object class
        IObjectClass targetClass = new IObjectClassProxy(targetTable);
        IObjectClass joinClass = new IObjectClassProxy(joinTable);
        IRelationshipClass relationshipClass = memRelClassFactory.open("MemRelClass", targetClass, fromField, joinClass, toField, "forward", "backward", esriRelCardinality.esriRelCardinalityOneToOne);
        // Open the RelQueryTable as a feature class.
        IRelQueryTableFactory rqtFactory = new RelQueryTableFactory();
        ITable relQueryTable = (ITable) rqtFactory.open(relationshipClass, true, null, null, "", false, false);
        return relQueryTable;
    }

    /**
     * 
     * @param cursor
     * @throws AutomationException
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private void printOut(ICursor cursor) throws AutomationException, IOException
    {
        // Get the number of fields in the feature class
        Fields fields = (Fields) cursor.getFields();
        int fieldCount = fields.getFieldCount();

        // Use the feature cursor to iterate over all elements in the feature
        // class,
        // printing the values of the fields. All field types are shown here.
        // Simple
        // values are printed as Strings. Complex elements are shown as the type
        // name.
        IRow feature = (IRow) cursor.nextRow();
        while (feature != null) {
            LOGGER.info("");
            LOGGER.info("#################################");

            for (int index = 0; index < fieldCount; index++) {
                String tmp = fields.getField(index).getName();
                int fieldType = feature.getFields().getField(index).getType();

                switch (fieldType) {
                case esriFieldType.esriFieldTypeDate:
                case esriFieldType.esriFieldTypeDouble:
                case esriFieldType.esriFieldTypeGlobalID:
                case esriFieldType.esriFieldTypeGUID:
                case esriFieldType.esriFieldTypeInteger:
                case esriFieldType.esriFieldTypeOID:
                case esriFieldType.esriFieldTypeSingle:
                case esriFieldType.esriFieldTypeSmallInteger:
                case esriFieldType.esriFieldTypeString:
                    tmp = tmp + " = " + feature.getValue(index);
                    break;

                case esriFieldType.esriFieldTypeBlob:
                    tmp = tmp + " = (blob)";
                    break;

                case esriFieldType.esriFieldTypeGeometry:
                    tmp = tmp + " = (geometry)";
                    break;

                case esriFieldType.esriFieldTypeRaster:
                    tmp = tmp + " = (raster)";
                    break;
                }
                LOGGER.info(tmp);
            }
            feature = (IRow) cursor.nextRow();
        }
    }

    //
    //
    //
    // /////////////////////////////////////////////////////////////////////
    //
    //
    //

    /**
     * 
     */
    public int insertObservation(int offeringID,
            Date phenomenonTime,
            int featureID,
            int observedPropertyID,
            int procedureID,
            float result) throws Exception
    {
        LOGGER.info("Starting to add new observation");

        try {
            workspace.startEditing(true);
            workspace.startEditOperation();
            LOGGER.info("Started editing workspace.");

            IRow newObservation = fc.createRow();
            LOGGER.info("New row created.");

            IFields fields = fc.getFields();
            for (int i = 0; i < fields.getFieldCount(); i++) {
                if (fields.getField(i).getName().equals(SubField.OBSERVATION_OFFERING)) {
                    newObservation.setValue(i, offeringID);
                } else if (fields.getField(i).getName().equals(SubField.OBSERVATION_END_TIME)) {

                    // Date phenomenonTimeStatic = new Date();
                    // short year = 2012;
                    // short month = 8;
                    // short day = 16;
                    // short hour = 15;
                    // short minute = 45;
                    // short second = 0;
                    // phenomenonTimeStatic.setYear(year);
                    // phenomenonTimeStatic.setMonth(month);
                    // phenomenonTimeStatic.setDate(day);
                    // phenomenonTimeStatic.setHours(hour);
                    // phenomenonTimeStatic.setMinutes(minute);
                    // phenomenonTimeStatic.setSeconds(second);
                    // LOGGER.info("phenomenonTimeStatic to set: '" +
                    // phenomenonTimeStatic + "'");
                    // newObservation.setValue(i, phenomenonTimeStatic);

                    LOGGER.info("phenomenonTime to set: '" + phenomenonTime + "'");
                    newObservation.setValue(i, phenomenonTime);
                } else if (fields.getField(i).getName().equals(SubField.OBSERVATION_PROPERTY)) {
                    newObservation.setValue(i, observedPropertyID);
                } else if (fields.getField(i).getName().equals(SubField.OBSERVATION_PROCEDURE)) {
                    newObservation.setValue(i, procedureID);
                } else if (fields.getField(i).getName().equals(SubField.OBSERVATION_FEATURE)) {
                    newObservation.setValue(i, featureID);
                } else if (fields.getField(i).getName().equals(SubField.OBSERVATION_NUMERIC_VALUE)) {
                    newObservation.setValue(i, result);
                }
            }
            LOGGER.info("New observation created.");

            newObservation.store();
            LOGGER.info("New observation stored.");

            int observationID = newObservation.getOID();
            LOGGER.info("New observation successfully added to DB: " + observationID);

            workspace.stopEditOperation();
            workspace.stopEditing(true);

            return observationID;

        } catch (Exception e) {
            workspace.stopEditOperation();
            workspace.stopEditing(false);

            LOGGER.severe("There was a problem while trying to insert new observation: \n" + ExceptionSupporter.createStringFromStackTrace(e));
            throw e;
        }
    }

    /**
     * @return the maximum ObjectID of all observations.
     * @throws IOException
     * @throws AutomationException
     */
    protected int getObservationMaxID() throws AutomationException, IOException
    {
        IQueryDef queryDef = workspace.createQueryDef();

        queryDef.setTables(Table.OBSERVATION);

        queryDef.setSubFields("MAX(" + SubField.OBSERVATION_OBJECTID + ")");

        // evaluate the database query
        ICursor cursor = queryDef.evaluate();

        IRow row = cursor.nextRow();

        return (Integer) row.getValue(0);
    }

    /**
     * Support method.
     */
//    protected String featureToString(Feature f) throws AutomationException, IOException
//    {
//        String featureAsString = "";
//        IFields fields = f.getFields();
//        for (int i = 0; i < fields.getFieldCount(); i++) {
//            String fieldName = fields.getField(i).getName();
//            String value = (String) f.getValue(i);
//            featureAsString += "[" + fieldName + ":->" + value + "]";
//        }
//        return featureAsString;
//    }
}
