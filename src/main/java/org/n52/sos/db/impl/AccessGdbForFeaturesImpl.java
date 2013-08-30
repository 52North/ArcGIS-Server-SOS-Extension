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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.activation.UnsupportedDataTypeException;

import org.n52.om.sampling.AQDSample;
import org.n52.om.sampling.Feature;
import org.n52.sos.Constants;
import org.n52.sos.db.AccessGdbForFeatures;
import org.n52.util.CommonUtilities;
import org.n52.util.logging.Logger;

import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IQueryDef;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class AccessGdbForFeaturesImpl implements AccessGdbForFeatures {
    
    static Logger LOGGER = Logger.getLogger(AccessGdbForFeaturesImpl.class.getName());

    private AccessGDBImpl gdb;

    public AccessGdbForFeaturesImpl(AccessGDBImpl accessGDB) {

        this.gdb = accessGDB;
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
    public Collection<Feature> getFeaturesOfInterest(
    		String[] featuresOfInterest,
            String[] observedProperties,
            String[] procedures,
            String spatialFilter) throws Exception
    {
    	
        List<Feature> features = new ArrayList<Feature>();
        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();

        // set tables
        List<String> tables = new ArrayList<String>();
        tables.add(Table.FEATUREOFINTEREST);
        if (observedProperties != null || procedures != null){
	        tables.add(Table.OBSERVATION);
	        tables.add(Table.PROPERTY);
	        tables.add(Table.PROCEDURE);
	    }
        
        queryDef.setTables(gdb.createCommaSeparatedList(tables));
        
        // Log out the query clause
        LOGGER.info("GetFeatureOfInterest DB query TABLES clause: '" + queryDef.getTables() + "'");
        
        
        // set sub fields
        List<String> subFields = new ArrayList<String>();
        
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_SHAPE));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_INLETHEIGHT));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_BUILDINGDISTANCE));
        subFields.add(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_KERBDISTANCE));
        if (observedProperties != null || procedures != null){
	        subFields.add(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY));
	        subFields.add(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE));
	        subFields.add(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_FEATUREOFINTEREST));
	        subFields.add(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROPERTY));
	        subFields.add(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE));
        }
        
        queryDef.setSubFields(gdb.createCommaSeparatedList(subFields));
        
		// Log out the query clause
        LOGGER.info("GetFeatureOfInterest DB query SUBFIELDS clause: '" + queryDef.getSubFields() + "'");
        
        
        // create the where clause with joins and constraints
        StringBuilder whereClause = new StringBuilder();

        // joins
        boolean firstWhere = true;
        if (observedProperties != null || procedures != null) {
	        whereClause.append(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST) + " = " + 
	        				   gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_FEATUREOFINTEREST));
	        firstWhere = false;
        }
        
        if (observedProperties != null) {
        	if (firstWhere == false) {
        		whereClause.append(" AND ");
        	}
        	firstWhere = false;
	        whereClause.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROPERTY) + " = " + 
	        				   gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PK_PROPERTY));
        }

        if (procedures != null) {
        	if (firstWhere == false) {
        		whereClause.append(" AND ");
        	}
        	firstWhere = false;
	        whereClause.append(gdb.concatTableAndField(Table.OBSERVATION, SubField.OBSERVATION_FK_PROCEDURE) + " = " + 
	        				   gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_PK_PROCEDURE));
        }
        
        // build query for feature of interest
        if (featuresOfInterest != null) {
            if (firstWhere == false) {
        		whereClause.append(" AND ");
        	}
            firstWhere = false;
            whereClause.append(gdb.createOrClause(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID), featuresOfInterest));
        }

        // build query for observed properties
        if (observedProperties != null) {
        	if (firstWhere == false) {
        		whereClause.append(" AND ");
        	}
        	firstWhere = false;
            whereClause.append(gdb.createOrClause(gdb.concatTableAndField(Table.PROPERTY, SubField.PROPERTY_ID), observedProperties));
        }

        // build query for procedures
        if (procedures != null) {
        	if (firstWhere == false) {
        		whereClause.append(" AND ");
        	}
        	firstWhere = false;
            whereClause.append(gdb.createOrClause(gdb.concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_ID), procedures));
        }

        // build query for spatial filter
        if (spatialFilter != null) {
        	Collection<String> featureList = gdb.queryFeatureIDsForSpatialFilter(spatialFilter);
            String[] featureArray = CommonUtilities.toArray(featureList);
            
            if (featureList.size() > 0) {
                // append the list of feature IDs:
            	if (firstWhere == false) {
            		whereClause.append(" AND ");
            	}
            	firstWhere = false;
            	whereClause.append(gdb.createOrClause(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID), featureArray));
            } else {
                LOGGER.warn("The defined spatialFilter '" + spatialFilter + "' did not match any features in the database.");
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
        while ((row = cursor.nextRow()) != null && count < gdb.getMaxNumberOfResults()) {
            count++;
            Feature feature = createFeature(row, fields);
            features.add(feature);
        }

        return features;
    }
    
    ///////////////////////////////
    /////////////////////////////// Helper Methods:
    ///////////////////////////////     
    
    /**
     * This method creates a {@link AQDSample} of a given {@link IRow} and it's {@link Fields}
     */
    protected AQDSample createFeature(IRow row, Fields fields) throws AutomationException, IOException, URISyntaxException
    {	
        // gml identifier
        String gmlId = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_ID)));
        
        // resource URI
        URI resourceUri = null;
        String resource = (String) row.getValue(fields.findField(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_RESOURCE)));
        if (resource != null) {
        	resourceUri = new URI(resource);
        }

        // local ID
        int localId = (Integer) row.getValue(fields.findField(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_PK_FEATUREOFINTEREST)));
        
        // shape
        Point point = null;
        Object shape = row.getValue(fields.findField(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_SHAPE)));
        if (shape instanceof Point) {
            point = (Point) shape;
        } else {
            LOGGER.warn("Shape of the feature '" + gmlId + "' is no point.");
        }
        
        // inletHeight
        Double inletHeight = (Double) row.getValue(fields.findField(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_INLETHEIGHT)));
        if (inletHeight == null) {
            inletHeight = Constants.FEATURE_INLET_HEIGHT;
        }
        
        // buildingDistance
        Double buildingDistance = (Double) row.getValue(fields.findField(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_BUILDINGDISTANCE)));
        if (buildingDistance == null) {
            buildingDistance = Constants.FEATURE_BUILDING_DISTANCE;
        }
        
        // kerbDistance
        Double kerbDistance = (Double) row.getValue(fields.findField(gdb.concatTableAndField(Table.FEATUREOFINTEREST, SubField.FEATUREOFINTEREST_KERBDISTANCE)));
        if (kerbDistance == null) {
            kerbDistance = Constants.FEATURE_KERB_DISTANCE;
        }

        return new AQDSample(resourceUri, gmlId, localId, null, null, null, point, null, inletHeight, buildingDistance, kerbDistance);
    }
}
