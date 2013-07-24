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
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Logger;

import org.n52.om.sampling.Feature;
import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class AccessGdbForFeatures {
    
    static Logger LOGGER = Logger.getLogger(AccessGdbForFeatures.class.getName());


    public AccessGdbForFeatures(AccessGDB accessGDB) {
    	//TODO: never used?
//        this.gdb = accessGDB;
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
    	// TODO: feature encoding needs to be adjusted to new AQ e-Reporting data model
    	
    	throw new UnsupportedOperationException();
    	
//        List<Feature> features = new ArrayList<Feature>();
//        IQueryDef queryDef = gdb.getWorkspace().createQueryDef();
//
//        // set tables
//        List<String> tables = new ArrayList<String>();
//        tables.add(Table.FEATURE);
//        tables.add(Table.PROCEDURE);
//        tables.add(Table.PROPERTY);
//        tables.add(Table.OFFERING);
//        tables.add(Table.FOI_OFF);
//        tables.add(Table.PROP_OFF);
//        queryDef.setTables(gdb.createCommaSeparatedList(tables));
//
//        // set sub fields
//        List<String> subFields = new ArrayList<String>();
//        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_NAME));
//        subFields.add(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID));
//        subFields.add(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION));
//        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_NAME));
//        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_DESCRIPTION));
//        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_SHAPE));
//        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_ID_VALUE));
//        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_CODE_SPACE));
//        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_SAMPLED_FEATURE_URL));
//        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_LOCAL_ID));
//        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_NAMESPACE));
//        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_INLET_HEIGHT));
//        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_BUILDING_DISTANCE));
//        subFields.add(concatTableAndField(Table.FEATURE, SubField.FEATURE_KERB_DISTANCE));
//        queryDef.setSubFields(gdb.createCommaSeparatedList(subFields));
//
//        // create the where clause with joins and constraints
//        StringBuilder whereClause = new StringBuilder();
//
//        // joins
//        whereClause.append(concatTableAndField(Table.OFFERING, SubField.OFFERING_OBJECTID) + " = " + concatTableAndField(Table.FOI_OFF, SubField.FOI_OFF_OFFERING_ID));
//        whereClause.append(" AND ");
//        whereClause.append(concatTableAndField(Table.FOI_OFF, SubField.FOI_OFF_FOI_ID) + " = " + concatTableAndField(Table.FEATURE, SubField.FEATURE_OBJECTID));
//        whereClause.append(" AND ");
//        whereClause.append(concatTableAndField(Table.OFFERING, SubField.OFFERING_PROCEDURE) + " = " + concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_OBJECTID));
//        whereClause.append(" AND ");
//        whereClause.append(concatTableAndField(Table.OFFERING, SubField.OFFERING_OBJECTID) + " = " + concatTableAndField(Table.PROP_OFF, SubField.PROP_OFF_OFFERING_ID));
//        whereClause.append(" AND ");
//        whereClause.append(concatTableAndField(Table.PROP_OFF, SubField.PROP_OFF_PROPERTY_ID) + " = " + concatTableAndField(Table.PROPERTY, SubField.PROPERTY_OBJECTID));
//
//        // build query for feature of interest
//        if (featuresOfInterest != null) {
//            whereClause.append(" AND ");
//            whereClause.append(createOrClause(concatTableAndField(Table.FEATURE, SubField.FEATURE_ID_VALUE), featuresOfInterest));
//        }
//
//        // build query for observed properties
//        if (observedProperties != null) {
//            whereClause.append(" AND ");
//            whereClause.append(createOrClause(concatTableAndField(Table.PROPERTY, SubField.PROPERTY_PROPERTY_DESCRIPTION), observedProperties));
//        }
//
//        // build query for procedures
//        if (procedures != null) {
//            whereClause.append(" AND ");
//            whereClause.append(createOrClause(concatTableAndField(Table.PROCEDURE, SubField.PROCEDURE_UNIQUE_ID), procedures));
//        }
//
//        // build query for spatial filter
//        if (spatialFilter != null) {
//            // get the IDs of all features which are within the specified
//            // spatialFilter:
//            Collection<Integer> featureList = gdb.queryFeatureIDsForSpatialFilter(spatialFilter);
//
//            if (featureList.size() > 0) {
//                // append the list of feature IDs:
//                whereClause.append(" AND ");
//                whereClause.append(createOrClause(concatTableAndField(Table.FEATURE, SubField.FEATURE_OBJECTID), featureList));
//            } else {
//                LOGGER.warning("The defined spatialFilter '" + spatialFilter + "' did not match any features in the database.");
//            }
//        }
//
//        queryDef.setWhereClause(whereClause.toString());
//
//        // Log out the query clause
//        LOGGER.info("GetFeatureOfInterest DB query WHERE clause: '" + queryDef.getWhereClause() + "'");
//
//        // evaluate the database query
//        ICursor cursor = queryDef.evaluate();
//
//        // convert cursor entries to abstract observations
//        Fields fields = (Fields) cursor.getFields();
//
//        IRow row;
//        int count = 0;
//        while ((row = cursor.nextRow()) != null && count < gdb.getMaxNumberOfResults()) {
//            count++;
//            Feature feature = createFeature(row, fields);
//            features.add(feature);
//        }
//
//        return features;
    }
    
    ///////////////////////////////
    // //////////////////////////// Helper Methods:
    ///////////////////////////////     
    
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
    protected Feature createFeature(IRow row,
            Fields fields) throws AutomationException, IOException, URISyntaxException
    {
    	// TODO: feature encoding needs to be adjusted to new AQ e-Reporting data model
    	
    	throw new UnsupportedOperationException();
    	
//        // identifier (codeSpace URI and idValue)
//        String idValue = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_ID_VALUE)));
//        String uri = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_CODE_SPACE)));
//        URI codeSpace;
//        if (uri != null) {
//            codeSpace = new URI(uri);
//        } else {
//            codeSpace = new URI("");
//        }
//        Identifier identifier = new Identifier(codeSpace, idValue);
//
//        // name
//        String name = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_NAME)));
//
//        // description
//        String description = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_DESCRIPTION)));
//
//        // sampled Feature
//        String sampledFeature = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_SAMPLED_FEATURE_URL)));
//
//        // shape
//        Point point = null;
//        Object shape = row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_SHAPE)));
//        if (shape instanceof Point) {
//            point = (Point) shape;
//        } else {
//            LOGGER.log(Level.WARNING, "Shape of the feature is no point.");
//        }
//        
//        // localID
//        String localId = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_LOCAL_ID)));
//        if (localId == null) {
//            localId = Constants.FEATURE_LOCALID;
//        }
//        
//        // namespace
//        String namespace = (String) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_NAMESPACE)));
//        if (namespace == null) {
//            namespace = Constants.FEATURE_NAMESPACE;
//        }
//        
//        // inletHeight
//        Double inletHeight = (Double) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_INLET_HEIGHT)));
//        if (inletHeight == null) {
//            inletHeight = Constants.FEATURE_INLET_HEIGHT;
//        }
//        
//        // buildingDistance
//        Double buildingDistance = (Double) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_BUILDING_DISTANCE)));
//        if (buildingDistance == null) {
//            buildingDistance = Constants.FEATURE_BUILDING_DISTANCE;
//        }
//        
//        // kerbDistance
//        Double kerbDistance = (Double) row.getValue(fields.findField(concatTableAndField(Table.FEATURE, SubField.FEATURE_KERB_DISTANCE)));
//        if (kerbDistance == null) {
//            kerbDistance = Constants.FEATURE_KERB_DISTANCE;
//        }
//
//        return new AQDSample(identifier, name, description, sampledFeature, point, localId, namespace, inletHeight, buildingDistance, kerbDistance);
    }
}
