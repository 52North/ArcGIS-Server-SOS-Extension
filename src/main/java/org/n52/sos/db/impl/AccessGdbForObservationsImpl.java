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

package org.n52.sos.db.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.n52.gml.Identifier;
import org.n52.om.observation.MultiValueObservation;
import org.n52.om.result.MeasureResult;
import org.n52.ows.ResponseExceedsSizeLimitException;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimeConverter;
import org.n52.sos.Constants;
import org.n52.sos.db.AccessGdbForObservations;
import org.n52.util.CommonUtilities;
import org.n52.util.logging.Logger;

import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IQueryDef;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class AccessGdbForObservationsImpl implements AccessGdbForObservations {

    static Logger LOGGER = Logger.getLogger(AccessGdbForObservationsImpl.class.getName());

    private AccessGDBImpl gdb;

    public AccessGdbForObservationsImpl(AccessGDBImpl accessGDB) {
        this.gdb = accessGDB;
    }
    
    /**
     * @return all observations with the specified identifiers.
     */
    public Map<String, MultiValueObservation> getObservations(String[] observationIdentifiers) throws Exception
    {
        return getObservations(gdb.createOrClause(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_ID), observationIdentifiers));
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
    @Override
    public Map<String, MultiValueObservation> getObservations(
            String[] offerings,
            String[] featuresOfInterest,
            String[] observedProperties,
            String[] procedures,
            String spatialFilter,
            String temporalFilter,
            String[] aggregationTypes,
            String where) throws Exception
    {
        StringBuilder whereClauseParameterAppend = new StringBuilder();
        
        boolean isFirst = true;
        
        // build query for offerings
        if (offerings != null) {
        	isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);
        	whereClauseParameterAppend.append(gdb.createOrClause(gdb.concatTableAndField(Table.NETWORK, SubField.NETWORK_ID), offerings));
        }
        
        // build query for feature of interest
        if (featuresOfInterest != null) {
        	isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);
            whereClauseParameterAppend.append(gdb.createOrClause(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID), featuresOfInterest));
        }

        // build query for observed property
        if (observedProperties != null) {
        	isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);
            whereClauseParameterAppend.append(gdb.createOrClause(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID), observedProperties));
        }

        // build query for procedure
        if (procedures != null) {
        	isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);
            whereClauseParameterAppend.append(gdb.createOrClause(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID), procedures));
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
                whereClauseParameterAppend.append(gdb.createOrClause(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID), featureArray));
            } else {
                LOGGER.warn("The defined spatialFilter '" + spatialFilter + "' did not match any features in the database.");
            }
        }

        // build query for temporal filter
        if (temporalFilter != null) {
        	isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);
            whereClauseParameterAppend.append(createTemporalClauseSDE(temporalFilter));
        }
        
        // build query for aggregation type
        if (aggregationTypes != null) {
        	isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);
            whereClauseParameterAppend.append(gdb.createOrClause(gdb.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_ID), aggregationTypes));
        }
        
        // build query for the where clause
        if (where != null) {
        	isFirst = ifIsFirstAppendAND (whereClauseParameterAppend, isFirst);
            whereClauseParameterAppend.append(where);
        }

        return getObservations(whereClauseParameterAppend.toString());
    }

    
    /**
     * This method serves as a skeleton for the other 2 methods above and
     * expects a WHERE clause append that parameterizes the database query.
     */
    private Map<String, MultiValueObservation> getObservations(String whereClause) throws Exception
    {
        String tables = createFromClause();

        List<String> subFields = createSubFieldsForQuery();
        
        assertMaximumRecordCount(tables, whereClause);
        
        ICursor cursor = evaluateQuery(tables, whereClause, gdb.createCommaSeparatedList(subFields));

        // convert cursor entries to abstract observations
        Fields fields = (Fields) cursor.getFields();

        // map that associates an observation-ID with an observation:
        Map<String, MultiValueObservation> idObsMap = new HashMap<String, MultiValueObservation>();
        IRow row;
		while ((row = cursor.nextRow()) != null) {
            String obsID = row.getValue(fields.findField(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_ID))).toString();

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

	private void assertMaximumRecordCount(String tables, String whereClause) throws ResponseExceedsSizeLimitException {
		try {
			ICursor countCursor = evaluateQuery(tables, whereClause, "count(*)");
			IRow row;
			if ((row = countCursor.nextRow()) != null) {
				Object value = row.getValue(0);
				if (value != null && value instanceof Integer) {
					if ((int) value > gdb.getMaxNumberOfResults()) {
						throw new ResponseExceedsSizeLimitException(gdb.getMaxNumberOfResults());
					}
				}
			}
		} catch (AutomationException e) {
			LOGGER.warn(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.warn(e.getMessage(), e);
		}		
	}
	
	private ICursor evaluateQuery(String tables, String whereClause, String subFields) 
			throws IOException, AutomationException
	{
		IQueryDef queryDef = gdb.getWorkspace().createQueryDef();
        queryDef.setTables(tables);
       	LOGGER.debug("Table clause := " + queryDef.getTables());

        queryDef.setSubFields(subFields);
       	LOGGER.debug("Subfields clause := " + queryDef.getSubFields());

        queryDef.setWhereClause(whereClause);
       	LOGGER.debug("Where clause := " + queryDef.getWhereClause());

        // evaluate the database query
        ICursor cursor = queryDef.evaluate();
		return cursor;
	}

	private List<String> createSubFieldsForQuery() {
		List<String> subFields = new ArrayList<String>();
        subFields.add(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_ID));
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID));
        subFields.add(gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_ID));
        subFields.add(gdb.concatTableAndField(Table.SAMPLINGPOINT, SubField.SAMPLINGPOINT_FK_STATION));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID));
        subFields.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID));
        subFields.add(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_NOTATION));
        subFields.add(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_ID));
        subFields.add(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_DEFINITION));
        subFields.add(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_LABEL));
        subFields.add(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_BEGIN));
        subFields.add(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END));
        subFields.add(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_VALUE_NUMERIC));
        subFields.add(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_RESULTTIME));
        subFields.add(gdb.concatTableAndField(Table.VALIDITY, SubField.VALIDITY_NOTATION)); 
        subFields.add(gdb.concatTableAndField(Table.VERIFICATION, SubField.VERIFICATION_NOTATION));
        subFields.add(gdb.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_DEFINITION));
        subFields.add(gdb.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_NOTATION));
        subFields.add(gdb.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_ID));
        subFields.add(gdb.concatTableAndField(Table.STATION, SubField.STATION_PK_STATION));
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
		" LEFT JOIN " + Table.UNIT 				+ " ON " + Table.UNIT + "." + SubField.UNIT_PK_UNIT 							+ " = " + Table.VALUE + "." + SubField.VALUE_PK_VALUE + 
		" LEFT JOIN " + Table.NETWORK 			+ " ON " + Table.NETWORK + "." + SubField.NETWORK_PK_NETWOK 					+ " = " + Table.STATION + "." + SubField.STATION_FK_NETWORK_GID;
		
		return fromClause;
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
        
        // unit notation
        String unitLabel = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.UNIT, SubField.UNIT_LABEL)));
        if (unitLabel == null) {
        	unitLabel = Constants.NULL_VALUE;
        }
        
        // aggregation type
        String aggregationType = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_DEFINITION)));
        if (aggregationType == null) {
            aggregationType = Constants.NULL_VALUE;
        }
        
        // result time
        Date resultDate = (Date) row.getValue(fields.findField(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_RESULTTIME)));
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
            Fields fields) throws AutomationException, IOException
    {
        // start time
        Date startDate = (Date) row.getValue(fields.findField(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_BEGIN)));
        ITimePosition startTimePos = TimeConverter.createTimeFromDate(startDate, null);

        // end time
        Date endDate = (Date) row.getValue(fields.findField(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_DATETIME_END)));
        ITimePosition endTimePos = TimeConverter.createTimeFromDate(endDate, null);

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
        
        //aggregationType
        String aggregationType = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.AGGREGATIONTYPE, SubField.AGGREGATIONTYPE_NOTATION)));
        if (aggregationType == null) {
        	aggregationType = Constants.NULL_VALUE;
        }

        // result
        Object numValue = row.getValue(fields.findField(gdb.concatTableAndField(Table.VALUE, SubField.VALUE_VALUE_NUMERIC)));
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
