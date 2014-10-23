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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.n52.gml.Identifier;
import org.n52.om.observation.MultiValueObservation;
import org.n52.om.result.MeasureResult;
import org.n52.ows.InvalidRequestException;
import org.n52.ows.ResponseExceedsSizeLimitException;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimeConverter;
import org.n52.sos.Constants;
import org.n52.sos.db.AccessGdbForObservations;
import org.n52.sos.handler.GetObservationOperationHandler;
import org.n52.util.CommonUtilities;
import org.n52.util.logging.Logger;

import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class AccessGdbForObservationsImpl implements AccessGdbForObservations {

	static Logger LOGGER = Logger.getLogger(AccessGdbForObservationsImpl.class.getName());

    static String[][] aggregationTypesCandidates = new String[][] {
			new String[] {Constants.GETOBSERVATION_DEFAULT_AGGREGATIONTYPE, Constants.GETOBSERVATION_DEFAULT_AGGREGATIONTYPE_ALT},
			new String[] {Constants.GETOBSERVATION_DEFAULT_AGGREGATIONTYPE_SECOND, Constants.GETOBSERVATION_DEFAULT_AGGREGATIONTYPE_SECOND_ALT}};
    
    private AccessGDBImpl gdb;

    public AccessGdbForObservationsImpl(AccessGDBImpl accessGDB) {
        this.gdb = accessGDB;
    }
    
    /**
     * @return all observations with the specified identifiers.
     * @throws IOException 
     * @throws AutomationException 
     * @throws ResponseExceedsSizeLimitException 
     */
    public Map<String, MultiValueObservation> getObservations(String[] observationIdentifiers) throws ResponseExceedsSizeLimitException, AutomationException, IOException
    {
        return getObservations(new StringBuilder(AccessGDBImpl.createOrClause(AccessGDBImpl.concatTableAndField(Table.OBSERVATION,
        		SubField.OBSERVATION_ID), observationIdentifiers)), null,
        		true);
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
     * @throws IOException 
     * @throws ResponseExceedsSizeLimitException 
     * @throws InvalidRequestException 
     * @throws Exception
     */
    @Override
    public Map<String, MultiValueObservation> getObservations(
            String[] offerings,
            String[] featuresOfInterest,
            String[] observedProperties,
            String[] procedures,
            String spatialFilter,
            String temporalFilter,
            String[] aggregationTypes,
            String where) throws IOException, ResponseExceedsSizeLimitException, InvalidRequestException
    {
        StringBuilder whereClauseParameterAppend = new StringBuilder();
        
        boolean isFirst = true;
        
        // build query for offerings
        if (offerings != null) {
        	isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);
        	whereClauseParameterAppend.append(AccessGDBImpl.createOrClause(AccessGDBImpl.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID), offerings));
        }
        
        // build query for feature of interest
        if (featuresOfInterest != null) {
        	isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);
            whereClauseParameterAppend.append(AccessGDBImpl.createOrClause(AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE), featuresOfInterest));
        }

        // build query for observed property
        if (observedProperties != null) {
        	isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);
            whereClauseParameterAppend.append(AccessGDBImpl.createOrClause(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID), observedProperties));
        }

        // build query for procedure
        if (procedures != null) {
        	isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);
            whereClauseParameterAppend.append(AccessGDBImpl.createOrClause(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE), procedures));
        }

        // build query for spatial filter
        if (spatialFilter != null) {
            // get the IDs of all features which are within the specified
            // spatialFilter:
            Collection<String> featureList = gdb.queryFeatureIDsForSpatialFilter(spatialFilter);
            String[] featureArray = CommonUtilities.toArray(featureList);
            
            if (featureList.size() > 0) {
            	isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);          
            	// append the list of feature IDs:
                whereClauseParameterAppend.append(AccessGDBImpl.createOrClause(AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE), featureArray));
            } else {
                LOGGER.warn("The defined spatialFilter '" + spatialFilter + "' did not match any features in the database.");
            }
        }

        // build query for temporal filter
        boolean firstOrLatest = false;
        if (temporalFilter != null) {
        	if (temporalFilter.equals(GetObservationOperationHandler.OM_PHENOMENON_TIME_FIRST) ||
        			temporalFilter.equals(GetObservationOperationHandler.OM_PHENOMENON_TIME_LATEST)) {
        		LOGGER.debug("Temporal filter special case: ".concat(temporalFilter));
        		firstOrLatest = true;
        	} else {
        		isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);
                whereClauseParameterAppend.append(createTemporalClauseSDE(temporalFilter));	
        	}
        }
        
        // build query for the where clause
        if (where != null) {
        	isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);
            whereClauseParameterAppend.append(where);
        }

        if (firstOrLatest) {
        	return getFirstOrLatestObservation(whereClauseParameterAppend, temporalFilter.equals(GetObservationOperationHandler.OM_PHENOMENON_TIME_FIRST), aggregationTypes);
        }
        return getObservations(whereClauseParameterAppend, aggregationTypes, true);
    }

    
    private Map<String, MultiValueObservation> getFirstOrLatestObservation(
			StringBuilder whereClauseParameterAppend, boolean first, String[] aggregationTypes) throws InvalidRequestException, IOException {
        if (whereClauseParameterAppend.toString().trim().isEmpty()) {
        	throw new InvalidRequestException("No filter of any kind was defined. Rejecting request.");
        }
    	
    	String tables = createFromClause();
        List<String> subFields = createSubFieldsForQuery();
        
        /*
         * if no aggregation type was defined in the query use
         * hourly as this should be there always
         */
        if (aggregationTypes == null) {
        	aggregationTypes = new String[] {
        			Constants.GETOBSERVATION_DEFAULT_AGGREGATIONTYPE,
        			Constants.GETOBSERVATION_DEFAULT_AGGREGATIONTYPE_ALT
        	};
        }
        
        ifIsFirstAppendAND(whereClauseParameterAppend, false);
        whereClauseParameterAppend.append(AccessGDBImpl.createOrClause(AccessGDBImpl.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_ID), aggregationTypes));
        
        String originalWhereClause = whereClauseParameterAppend.toString();
        
        /*
         * SubQuery using MIN/MAX as ArcObject does not support ORDER BY
         */
        whereClauseParameterAppend.append(" AND ");
        whereClauseParameterAppend.append(AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END));
        whereClauseParameterAppend.append(" = (");
        
        if (first) {
        	whereClauseParameterAppend.append("SELECT MIN(");
        }
        else {
        	whereClauseParameterAppend.append("SELECT MAX(");
        }
        whereClauseParameterAppend.append(AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END));
        whereClauseParameterAppend.append(")");
        whereClauseParameterAppend.append(" FROM ");
        whereClauseParameterAppend.append(tables);
        whereClauseParameterAppend.append(" WHERE ");
        whereClauseParameterAppend.append(originalWhereClause);
        
        whereClauseParameterAppend.append(" )");
        
        String whereClause = whereClauseParameterAppend.toString();
        LOGGER.debug("WHERE "+ whereClause);
        
        ICursor cursor = DatabaseUtils.evaluateQuery(tables, whereClause,
        		" DISTINCT TOP 1 ".concat(AccessGDBImpl.createCommaSeparatedList(subFields)), gdb);

        Map<String, MultiValueObservation> idObsMap = createObservationsFromCursor(cursor, subFields);

        return idObsMap;
	}

	/**
     * This method serves as a skeleton for the other 2 methods above and
     * expects a WHERE clause that parameterizes the database query.
     * @param aggregationTypes 
	 * @throws ResponseExceedsSizeLimitException 
	 * @throws IOException 
	 * @throws AutomationException 
     */
    private Map<String, MultiValueObservation> getObservations(StringBuilder whereClauseParameterAppend, String[] aggregationTypes, boolean checkForMaxRecords) throws ResponseExceedsSizeLimitException, AutomationException, IOException
    {
        String tables = createFromClause();

        List<String> subFields = createSubFieldsForQuery();
        
        /*
         * if there are values for aggregationTypes, then it
         * is defined via the request. otherwise try the default
         * values
         */
        boolean alreadyAssertedMaxRecords = false;
        if (aggregationTypes != null) {
        	ifIsFirstAppendAND(whereClauseParameterAppend, whereClauseParameterAppend.toString().trim().isEmpty());
            whereClauseParameterAppend.append(AccessGDBImpl.createOrClause(AccessGDBImpl.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_ID), aggregationTypes));
        }
        else {
        	alreadyAssertedMaxRecords = determineBestAggregationType(whereClauseParameterAppend, tables, checkForMaxRecords);
        }
        
        String whereClause = whereClauseParameterAppend.toString();
        if (checkForMaxRecords && !alreadyAssertedMaxRecords) {
        	DatabaseUtils.assertMaximumRecordCount(tables, whereClause, gdb);
        }
        
        ICursor cursor = DatabaseUtils.evaluateQuery(tables, whereClause,
        		" DISTINCT " + AccessGDBImpl.createCommaSeparatedList(subFields), gdb);

        Map<String, MultiValueObservation> idObsMap = createObservationsFromCursor(cursor, subFields);

        return idObsMap;
    }

	private Map<String, MultiValueObservation> createObservationsFromCursor(
			ICursor cursor, List<String> fields) throws IOException {
		// convert cursor entries to abstract observations
        // map that associates an observation-ID with an observation:
        Map<String, MultiValueObservation> idObsMap = new HashMap<String, MultiValueObservation>();
        IRow row;
		while ((row = cursor.nextRow()) != null) {
            String obsID = row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_ID))).toString();

            if (!idObsMap.containsKey(obsID)) {
                MultiValueObservation multiValObs = createMultiValueObservation(row, fields);
                multiValObs.getResult().addResultValue(createResultValue(row, fields));
                idObsMap.put(obsID, multiValObs);
            } else {
            	idObsMap.get(obsID).getResult().addResultValue(createResultValue(row, fields));
            }
        }
		return idObsMap;
	}


	private boolean determineBestAggregationType(
			StringBuilder whereClauseParameterAppend, String tables, boolean checkForMaxRecords) {
		int lengthBefore = whereClauseParameterAppend.length();
        
		int c;
		for (String[] aggregationTypes : aggregationTypesCandidates) {
        	ifIsFirstAppendAND(whereClauseParameterAppend, whereClauseParameterAppend.toString().trim().isEmpty());
            whereClauseParameterAppend.append(AccessGDBImpl.createOrClause(AccessGDBImpl.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_ID), aggregationTypes));
            
            c = DatabaseUtils.resolveRecordCount(tables, whereClauseParameterAppend.toString(), gdb);
            if (c > 0 && (!checkForMaxRecords || c < gdb.getMaxNumberOfResults())) {
            	return true;
            }
            
            whereClauseParameterAppend.setLength(lengthBefore);
        }
		
		return false;
	}

	private List<String> createSubFieldsForQuery() {
		List<String> subFields = new ArrayList<String>();
		
		subFields.add(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_PK_OBSERVATION)); //this field is only needed so that DISTINCT works
		subFields.add(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_ID));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_RESOURCE));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_ID));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.UNIT, SubField.UNIT_NOTATION));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.UNIT, SubField.UNIT_ID));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.UNIT, SubField.UNIT_LABEL));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_BEGIN));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_VALUE_NUMERIC));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_RESULTTIME));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.VALIDITY, SubField.VALIDITY_NOTATION)); 
        subFields.add(AccessGDBImpl.concatTableAndField(Table.VERIFICATION, SubField.VERIFICATION_NOTATION));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_DEFINITION));
        subFields.add(AccessGDBImpl.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_NOTATION));
		return subFields;
	}

	private String createFromClause() {
		String fromClause = 
		Table.OBSERVATION +
		" LEFT JOIN " + Table.FEATUREOFINTEREST	+ " ON " + Table.OBSERVATION + "." + SubField.OBSERVATION_FK_FEATUREOFINTEREST	+ " = " + Table.FEATUREOFINTEREST + "." + SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST +
		" LEFT JOIN " + Table.PROCEDURE 		+ " ON " + Table.OBSERVATION + "." + SubField.OBSERVATION_FK_PROCEDURE 			+ " = " + Table.PROCEDURE + "." + SubField.PROCEDURE_PK_PROCEDURE +
		" LEFT JOIN " + Table.PROPERTY 			+ " ON " + Table.OBSERVATION + "." + SubField.OBSERVATION_FK_PROPERTY 			+ " = " + Table.PROPERTY + "." + SubField.PROPERTY_PK_PROPERTY +
		" LEFT JOIN " + Table.SAMPLINGPOINT 	+ " ON " + Table.OBSERVATION + "." + SubField.OBSERVATION_FK_SAMPLINGPOINT 		+ " = " + Table.SAMPLINGPOINT + "." + SubField.SAMPLINGPOINT_PK_SAMPLINGPOINT + 
		" LEFT JOIN " + Table.VALUE 			+ " ON " + Table.VALUE + "." + SubField.VALUE_FK_OBSERVATION 					+ " = " + Table.OBSERVATION + "." + SubField.OBSERVATION_PK_OBSERVATION +
		
		" LEFT JOIN " + Table.VALIDITY 			+ " ON " + Table.VALUE + "." + SubField.VALUE_FK_VALIDITY 						+ " = " + Table.VALIDITY + "." + SubField.VALIDITY_PK_VALIDITY +
		" LEFT JOIN " + Table.VERIFICATION 		+ " ON " + Table.VALUE + "." + SubField.VALUE_FK_VERIFICATION 					+ " = " + Table.VERIFICATION + "." + SubField.VERIFICATION_PK_VERIFICATION +
		" LEFT JOIN " + Table.AGGREGATIONTYPE 	+ " ON " + Table.VALUE + "." + SubField.VALUE_FK_AGGREGATIONTYPE 				+ " = " + Table.AGGREGATIONTYPE + "." + SubField.AGGREGATIONTYPE_PK_AGGREGATIONTYPE +
		
		" LEFT JOIN " + Table.STATION 			+ " ON " + Table.SAMPLINGPOINT + "." + SubField.SAMPLINGPOINT_FK_STATION 		+ " = " + Table.STATION + "." + SubField.STATION_PK_STATION +
		" LEFT JOIN " + Table.UNIT 				+ " ON " + Table.UNIT + "." + SubField.UNIT_PK_UNIT 							+ " = " + Table.VALUE + "." + SubField.VALUE_FK_UNIT + 
		" LEFT JOIN " + Table.NETWORK 			+ " ON " + Table.NETWORK + "." + SubField.NETWORK_PK_NETWOK 					+ " = " + Table.STATION + "." + SubField.STATION_FK_NETWORK_GID;
		
		return fromClause;
	}
    
    // /////////////////////////////
    // //////////////////////////// Helper Methods:
    // /////////////////////////////

    protected MultiValueObservation createMultiValueObservation(IRow row,
            List<String> fields) throws IOException
    {
        // Identifier
        String obsID = row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_ID))).toString();
        Identifier obsIdentifier = new Identifier(null, obsID);

        // procedure
        String procID = (String) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE)));
        if (procID == null) {
            procID = Constants.NULL_VALUE;
        }

        // observed property
        String obsPropID = (String) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID)));
        if (obsPropID == null) {
            obsPropID = Constants.NULL_VALUE;
        }

        // featureOfInterest
        String featureID = (String) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE)));
        if (featureID == null) {
            featureID = Constants.NULL_VALUE;
        }
        
        // samplingFeature
        String samplingPointID = (String) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_RESOURCE)));
        // in case "resource" field is null, "id" field is used:
        if (samplingPointID == null || samplingPointID.equals("")) {
            samplingPointID = (String) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_ID)));
        }

        // unit ID
        String unitID = (String) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.UNIT, SubField.UNIT_ID)));
        if (unitID == null) {
            unitID = Constants.NULL_VALUE;
        }
        
        // unit notation
        String unitNotation = (String) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.UNIT, SubField.UNIT_NOTATION)));
        if (unitNotation == null) {
            unitNotation = Constants.NULL_VALUE;
        }
        
        // unit notation
        String unitLabel = (String) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.UNIT, SubField.UNIT_LABEL)));
        if (unitLabel == null) {
        	unitLabel = Constants.NULL_VALUE;
        }
        
        // aggregation type
        String aggregationType = (String) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_DEFINITION)));
        if (aggregationType == null) {
            aggregationType = Constants.NULL_VALUE;
        }
        
        // result time
        Date resultDate = (Date) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_RESULTTIME)));
        ITimePosition resultTimePos = TimeConverter.createTimeFromDate(resultDate, null);

        return new MultiValueObservation(obsIdentifier, procID, obsPropID, featureID, samplingPointID, unitID, unitNotation, unitLabel, aggregationType, resultTimePos);
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
            List<String> fields) throws AutomationException, IOException
    {
        // start time
        Date startDate = (Date) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_BEGIN)));
        ITimePosition startTimePos = TimeConverter.createTimeFromDate(startDate, null);

        // end time
        Date endDate = (Date) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END)));
        ITimePosition endTimePos = TimeConverter.createTimeFromDate(endDate, null);

        // validity
        String validity = (String) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.VALIDITY, SubField.VALIDITY_NOTATION)));
        if (validity == null) {
            validity = Constants.NULL_VALUE;
        }

        // verification
        String verification = (String) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.VERIFICATION, SubField.VERIFICATION_NOTATION)));
        if (verification == null) {
            verification = Constants.NULL_VALUE;
        }
        
        //aggregationType
        String aggregationType = (String) row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_NOTATION)));
        if (aggregationType == null) {
        	aggregationType = Constants.NULL_VALUE;
        }

        // result
        Object numValue = row.getValue(fields.indexOf(AccessGDBImpl.concatTableAndField(Table.VALUE, SubField.VALUE_VALUE_NUMERIC)));
        Double value = (Double) numValue;

        return new MeasureResult(startTimePos, endTimePos, validity, verification, aggregationType, value);
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
    public String createTemporalClauseSDE(String temporalFilter) throws IllegalArgumentException
    {
        String clause = null;
        
        String tempOperand = TimeConverter.extractTemporalOperandAfterKeyWord(temporalFilter);
        
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
     * helper method to reduce code length. Appends "AND" to WHERE clause if 'isFirst == false'.
     */
    private boolean ifIsFirstAppendAND (StringBuilder whereClauseParameterAppend, boolean isFirst) {
    	if (isFirst == false) {
    		whereClauseParameterAppend.append(" AND ");
    	}
    	else {
    		isFirst = false;
    	}
    	return isFirst;
    }
}
