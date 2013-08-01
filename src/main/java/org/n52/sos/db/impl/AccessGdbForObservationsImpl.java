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
import java.util.Iterator;
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
    @Override
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
                LOGGER.warn("The defined spatialFilter '" + spatialFilter + "' did not match any features in the database.");
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
        List<String> tables = createTablesForQuery();
        
        String whereClause = createWhereClauseForQuery(whereClauseParameterAppend);

        List<String> subFields = createSubFieldsForQuery();
        
        assertMaximumRecordCount(tables, whereClause);
        
        ICursor cursor = evaluateQuery(tables, whereClause, subFields);

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

	private void assertMaximumRecordCount(List<String> tables, String whereClause) throws ResponseExceedsSizeLimitException {
		try {
			ICursor countCursor = evaluateQuery(tables, whereClause, Collections.singletonList("count(*)"));
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

	private ICursor evaluateQuery(List<String> tables,
			String whereClause, List<String> subFields) throws IOException,
			AutomationException {
		IQueryDef queryDef = gdb.getWorkspace().createQueryDef();
        queryDef.setTables(gdb.createCommaSeparatedList(tables));
       	LOGGER.debug("Table clause := " + queryDef.getTables());

        queryDef.setSubFields(gdb.createCommaSeparatedList(subFields));
       	LOGGER.debug("Subfields clause := " + queryDef.getSubFields());

        queryDef.setWhereClause(whereClause);
       	LOGGER.debug("Where clause := " + queryDef.getWhereClause());

        // evaluate the database query
        ICursor cursor = queryDef.evaluate();
		return cursor;
	}

	private String createWhereClauseForQuery(
			String whereClauseParameterAppend) {
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

        whereClause.append(whereClauseParameterAppend);
		return whereClause.toString();
	}

	private List<String> createSubFieldsForQuery() {
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
		return subFields;
	}

	private List<String> createTablesForQuery() {
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
        return tables;
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
        ITimePosition resultTimePos = TimeConverter.createTimeFromDate(resultDate, null);

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
