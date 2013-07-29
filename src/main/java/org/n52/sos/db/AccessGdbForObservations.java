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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.n52.gml.Identifier;
import org.n52.om.observation.MultiValueObservation;
import org.n52.om.result.MeasureResult;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimeConverter;
import org.n52.oxf.valueDomains.time.TimePosition;
import org.n52.sos.Constants;

import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IQueryDef;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class AccessGdbForObservations {

    static Logger LOGGER = Logger.getLogger(AccessGdbForObservations.class.getName());

    private AccessGDB gdb;

    public AccessGdbForObservations(AccessGDB accessGDB) {
        this.gdb = accessGDB;
    }
    
    /**
     * @return all observations with the specified identifiers.
     */
    public Map<String, MultiValueObservation> getObservations(String[] observationIdentifiers) throws Exception
    {
        // create the where clause with joins and constraints
        StringBuilder whereClauseParameterAppend = new StringBuilder();

        // joins from OBSERVATION
        whereClauseParameterAppend.append(" AND ");
        whereClauseParameterAppend.append(gdb.join(Table.OBSERVATION, SubField.OBSERVATION_FK_FEATUREOFINTEREST, Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST));
        whereClauseParameterAppend.append(" AND ");
        whereClauseParameterAppend.append(gdb.join(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE, Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE));
        whereClauseParameterAppend.append(" AND ");
        whereClauseParameterAppend.append(gdb.join(Table.OBSERVATION, SubField.OBSERVATION_FK_PROPERTY, Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY));
        whereClauseParameterAppend.append(" AND ");
        whereClauseParameterAppend.append(gdb.join(Table.OBSERVATION, SubField.OBSERVATION_FK_SAMPLINGPOINT, Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT));

        // joins FROM VALUE
        whereClauseParameterAppend.append(" AND ");
        whereClauseParameterAppend.append(gdb.join(Table.VALUE, SubField.VALUE_FK_OBSERVATION, Table.OBSERVATION, SubField.OBSERVATION_PK_OBSERVATION));
        whereClauseParameterAppend.append(" AND ");
        whereClauseParameterAppend.append(gdb.join(Table.VALUE, SubField.VALUE_FK_VALIDITY, Table.VALIDITY, SubField.VALIDITY_PK_VALIDITY));
        whereClauseParameterAppend.append(" AND ");
        whereClauseParameterAppend.append(gdb.join(Table.VALUE, SubField.VALUE_FK_VERIFICATION, Table.VERIFICATION, SubField.VERIFICATION_PK_VERIFICATION));
        whereClauseParameterAppend.append(" AND ");
        whereClauseParameterAppend.append(gdb.join(Table.VALUE, SubField.VALUE_FK_AGGREGATIONTYPE, Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_PK_AGGREGATIONTYPE));

        // where query:
        whereClauseParameterAppend.append(" AND ");
        whereClauseParameterAppend.append(gdb.createOrClause(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_ID), observationIdentifiers));

        return getObservations(whereClauseParameterAppend.toString());
    }

    /**
     * This method can be used to retrieve all observations complying to the
     * filter as specified by the parameters. The method basically reflects the
     * SOS:GetObservation() operation on Java level.
     * 
     * If one of the method parameters is <b>null</b>, it shall not be
     * considered in the query.
     * 
     * @return all observations from the database which comply to the
     *         specified parameters.
     * @throws Exception
     */
    public Map<String, MultiValueObservation> getObservations(
            String[] offerings,
            String[] featuresOfInterest,
            String[] observedProperties,
            String[] procedures,
            String spatialFilter,
            String temporalFilter,
            String where) throws Exception
    {
        
        StringBuilder whereClauseParameterAppend = new StringBuilder();
        
        // build query for offerings
        if (offerings != null) {
            throw new UnsupportedOperationException("Parameter 'offering' not yet supported.");
//            whereClause.append(" AND ");
//            whereClause.append(gdb.createOrClause(gdb.concatTableAndField(Table.OFFERING, SubField.OFFERING_OFFERING_NAME), offerings));
        }
        
        // build query for feature of interest
        if (featuresOfInterest != null) {
            whereClauseParameterAppend.append(" AND ");
            whereClauseParameterAppend.append(gdb.createOrClause(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID), featuresOfInterest));
        }

        // build query for observed property
        if (observedProperties != null) {
            whereClauseParameterAppend.append(" AND ");
            whereClauseParameterAppend.append(gdb.createOrClause(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID), observedProperties));
        }

        // build query for procedure
        if (procedures != null) {
            whereClauseParameterAppend.append(" AND ");
            whereClauseParameterAppend.append(gdb.createOrClause(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID), procedures));
        }

        // build query for spatial filter
        if (spatialFilter != null) {
            // get the IDs of all features which are within the specified
            // spatialFilter:
            Collection<String> featureList = gdb.queryFeatureIDsForSpatialFilter(spatialFilter);
            String[] featureArray = toArray(featureList);
            
            if (featureList.size() > 0) {
                // append the list of feature IDs:
                whereClauseParameterAppend.append(" AND ");
                whereClauseParameterAppend.append(gdb.createOrClause(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID), featureArray));
            } else {
                LOGGER.warning("The defined spatialFilter '" + spatialFilter + "' did not match any features in the database.");
            }
        }

        // build query for temporal filter
        if (temporalFilter != null) {
            whereClauseParameterAppend.append(" AND ");
            whereClauseParameterAppend.append(createTemporalClauseSDE(temporalFilter));
        }

        // build query for the where clause
        if (where != null) {
            whereClauseParameterAppend.append(" AND ");
            whereClauseParameterAppend.append(where);
        }

        return getObservations(whereClauseParameterAppend.toString());
    }

    /**
     * This method serves as a skeleton for the other 2 methods above and
     * expects a WHERE clause append that parameterizes the database query.
     */
    private Map<String, MultiValueObservation> getObservations(String whereClauseParameterAppend) throws Exception
    {
        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();

        // set tables
        List<String> tables = new ArrayList<String>();
        tables.add(Table.OBSERVATION);
        tables.add(Table.PROCEDURE);
        tables.add(Table.SAMPLINGPOINT);
        tables.add(Table.FEATUREOFINTEREST);
        tables.add(Table.PROPERTY);
        tables.add(Table.UNIT);
        tables.add(Table.VALUE);
        tables.add(Table.AGGREGATIONTYPE);
        tables.add(Table.VALIDITY);
        tables.add(Table.VERIFICATION);
        
        queryDef.setTables(gdb.createCommaSeparatedList(tables));
        LOGGER.info("Table clause := " + queryDef.getTables());
        
        // set sub fields
        List<String> subFields = new ArrayList<String>();
        subFields.add(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_ID));
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
        subFields.add(gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_ID));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID));
        subFields.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
        subFields.add(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_NOTATION));
        subFields.add(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_ID));
        subFields.add(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_BEGIN));
        subFields.add(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END));
        subFields.add(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_VALUE_NUMERIC));
        subFields.add(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_RESULTTIME));
        subFields.add(gdb.concatTableAndField(Table.VALIDITY, SubField.VALIDITY_NOTATION)); 
        subFields.add(gdb.concatTableAndField(Table.VERIFICATION, SubField.VERIFICATION_NOTATION));
        subFields.add(gdb.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_DEFINITION));

        queryDef.setSubFields(gdb.createCommaSeparatedList(subFields));
        LOGGER.info("Subfields clause := " + queryDef.getSubFields());
        
        // create the where clause with joins and constraints
        StringBuilder whereClause = new StringBuilder();

        // joins from OBSERVATION
        whereClause.append(gdb.join(Table.OBSERVATION, SubField.OBSERVATION_FK_FEATUREOFINTEREST, Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST));
        whereClause.append(" AND ");
        whereClause.append(gdb.join(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE, Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE));
        whereClause.append(" AND ");
        whereClause.append(gdb.join(Table.OBSERVATION, SubField.OBSERVATION_FK_PROPERTY, Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY));
        whereClause.append(" AND ");
        whereClause.append(gdb.join(Table.OBSERVATION, SubField.OBSERVATION_FK_SAMPLINGPOINT, Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT));

        // joins from VALUE
        whereClause.append(" AND ");
        whereClause.append(gdb.join(Table.VALUE, SubField.VALUE_FK_OBSERVATION, Table.OBSERVATION, SubField.OBSERVATION_PK_OBSERVATION));
        whereClause.append(" AND ");
        whereClause.append(gdb.join(Table.VALUE, SubField.VALUE_FK_VALIDITY, Table.VALIDITY, SubField.VALIDITY_PK_VALIDITY));
        whereClause.append(" AND ");
        whereClause.append(gdb.join(Table.VALUE, SubField.VALUE_FK_VERIFICATION, Table.VERIFICATION, SubField.VERIFICATION_PK_VERIFICATION));
        whereClause.append(" AND ");
        whereClause.append(gdb.join(Table.VALUE, SubField.VALUE_FK_AGGREGATIONTYPE, Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_PK_AGGREGATIONTYPE));

        // where query:
        queryDef.setWhereClause(whereClause.append(whereClauseParameterAppend).toString());

        // Log the query clause
        if (LOGGER.isLoggable(Level.INFO))
        	LOGGER.info("Where clause := " + queryDef.getWhereClause());

        // evaluate the database query
        ICursor cursor = queryDef.evaluate();

        // convert cursor entries to abstract observations
        Fields fields = (Fields) cursor.getFields();

        IRow row = cursor.nextRow();
        int count = 0;
        
        // map that associates an observation-ID with an observation:
        Map<String, MultiValueObservation> idObsMap = new HashMap<String, MultiValueObservation>();
        
        while (row != null && count < gdb.getMaxNumberOfResults()) {
            String obsID = row.getValue(fields.findField(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_ID))).toString();

            MultiValueObservation multiValObs = idObsMap.get(obsID);
            
            if (multiValObs == null) {
                multiValObs = createMultiValueObservation(row, fields);
                multiValObs.getResult().addResultValue(createResultValue(row, fields));
                idObsMap.put(obsID, multiValObs);
            } else {
                multiValObs.getResult().addResultValue(createResultValue(row, fields));
            }

            count++;
            row = cursor.nextRow();
        }

        return idObsMap;
    }
    
    // /////////////////////////////
    // //////////////////////////// Helper Methods:
    // /////////////////////////////

    protected MultiValueObservation createMultiValueObservation(IRow row,
            Fields fields) throws IOException, AutomationException, Exception
    {
        // Identifier
        String obsID = row.getValue(fields.findField(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_ID))).toString();
        Identifier obsIdentifier = new Identifier(null, obsID);

        // procedure
        String procID = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE)));
        if (procID == null) {
            procID = Constants.NULL_VALUE;
        }

        // observed property
        String obsPropID = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID)));
        if (obsPropID == null) {
            obsPropID = Constants.NULL_VALUE;
        }

        // featureOfInterest
        String featureID = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE)));
        if (featureID == null) {
            featureID = Constants.NULL_VALUE;
        }
        
        // samplingFeature
        String samplingPointID = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_RESOURCE)));
        // in case "resource" field is null, "id" field is used:
        if (samplingPointID == null || samplingPointID.equals("")) {
            samplingPointID = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_ID)));
        }

        // unit ID
        String unitID = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_ID)));
        if (unitID == null) {
            unitID = Constants.NULL_VALUE;
        }
        
        // unit notation
        String unitNotation = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_NOTATION)));
        if (unitNotation == null) {
            unitNotation = Constants.NULL_VALUE;
        }
        
        // aggregation type
        String aggregationType = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_DEFINITION)));
        if (aggregationType == null) {
            aggregationType = Constants.NULL_VALUE;
        }
        
        // result time
        Date resultDate = (Date) row.getValue(fields.findField(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_RESULTTIME)));
        ITimePosition resultTimePos = createTimeFromDate(resultDate, null);

        return new MultiValueObservation(obsIdentifier, procID, obsPropID, featureID, samplingPointID, unitID, unitNotation, aggregationType, resultTimePos);
    }

    /**
     * 
     * @param row
     * @param fields
     * @return
     * @throws IOException
     * @throws AutomationException
     */
    protected MeasureResult createResultValue(IRow row,
            Fields fields) throws AutomationException, IOException
    {
        // start time
        Date startDate = (Date) row.getValue(fields.findField(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_BEGIN)));
        ITimePosition startTimePos = createTimeFromDate(startDate, null);

        // end time
        Date endDate = (Date) row.getValue(fields.findField(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END)));
        ITimePosition endTimePos = createTimeFromDate(endDate, null);

        // validity
        String validity = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.VALIDITY, SubField.VALIDITY_NOTATION)));
        if (validity == null) {
            validity = Constants.NULL_VALUE;
        }

        // verification
        String verification = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.VERIFICATION, SubField.VERIFICATION_NOTATION)));
        if (verification == null) {
            verification = Constants.NULL_VALUE;
        }

        // result
        Object numValue = row.getValue(fields.findField(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_VALUE_NUMERIC)));
        double value = (Double) numValue;

        return new MeasureResult(startTimePos, endTimePos, validity, verification, value);
    }
 
    /**
     * This method creates a temporal database clause out of a given temporal filter as
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
        String clause = null;
        
        String tempOperand = extractTemporalOperandAfterKeyWord(temporalFilter);
        
        if (temporalFilter.contains("during:")) {
            String timeStart = TimeConverter.convertLocalToUTC(tempOperand.split(",")[0]);
            String timeEnd = TimeConverter.convertLocalToUTC(tempOperand.split(",")[1]);
            clause = SubField.VALUE_DATETIME_END + " BETWEEN '" + timeStart + "' AND '" + timeEnd + "'";
        } 
        else if (temporalFilter.contains("equals:")) {
            String timeInstant = TimeConverter.convertLocalToUTC(tempOperand);
            clause = SubField.VALUE_DATETIME_END + " = '" + timeInstant + "'";
        } 
        else if (temporalFilter.contains("after:")) {
            String timeInstant = TimeConverter.convertLocalToUTC(tempOperand);
            clause = SubField.VALUE_DATETIME_END + " > '" + timeInstant + "'";
        } 
        else if (temporalFilter.contains("before:")) {
            String timeInstant = TimeConverter.convertLocalToUTC(tempOperand);
            clause = SubField.VALUE_DATETIME_END + " < '" + timeInstant + "'";
        } 
        else if (temporalFilter.contains("last:")) {
            long duration = Long.parseLong(tempOperand);

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

            clause = SubField.VALUE_DATETIME_END + " > '" + timeInstant + "'";
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
    
    private static String[] toArray(Collection<String> stringCollection) {
        String[] sArray = new String[stringCollection.size()];
        int i=0;
        for (Iterator<String> iterator = stringCollection.iterator(); iterator.hasNext();) {
            sArray[i] = (String) iterator.next();
            i++;
        }
        return sArray;
    }
}
