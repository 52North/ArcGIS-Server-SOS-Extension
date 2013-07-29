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

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Assert;
import org.n52.om.observation.MultiValueObservation;
import org.n52.om.sampling.Feature;
import org.n52.sos.JSONEncoder;
import org.n52.sos.JSONObservationEncoder;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.dataTypes.Procedure;
import org.n52.sos.dataTypes.ServiceDescription;
import org.n52.sos.it.EsriTestBase;

import com.esri.arcgis.server.json.JSONObject;


public class AccessObservationGDBIT extends EsriTestBase {

    static Logger LOGGER = Logger.getLogger(AccessObservationGDBIT.class.getName());
    
    /**
     * Test method for {@link
     * org.n52.sos.db.AccessObservationGDB#getObservation(...)} .
     */
    public void testGetObservations()
    {
        try {

            Map<String, MultiValueObservation> observations = null;

            long millis;

            LOGGER.info("###################################### no parameter geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("without Entries ", observations);

            LOGGER.info("###################################### offering geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(new String[] { "Observations of my thermometer" }, null, null, null, null, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("with Offerings", observations);

            LOGGER.info("###################################### featuresOfInterest geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, new String[] { "WXT500" }, null, null, null, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("with feature of interest '' ", observations);

            LOGGER.info("###################################### spatial filter 1 geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, "{xmin:0.0,ymin:40.0,xmax:2.0,ymax:43.0,spatialReference:{wkid:4326}}", null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("with feature of interest '' ", observations);

            LOGGER.info("###################################### spatial filter 2 geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, "{x:1.121389,y:41.152222,spatialReference:{wkid:4326}}", null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("with feature of interest '' ", observations);

            LOGGER.info("###################################### temporal filter 'equals' geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, "equals:2011-07-28T10:00:00+12:00", null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("equals:<time instant>", observations);

            LOGGER.info("###################################### temporal filter 'during' geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, "during:2011-08-13T10:00:00,2011-08-13T11:00:00", null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("during:<time start>,<time end>", observations);

            LOGGER.info("###################################### temporal filter 'after' geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, "after:2011-08-13T10:00:00", null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("after:<time instant>", observations);

            LOGGER.info("###################################### temporal filter 'before' geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, "before:2011-08-13T10:00:00", null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("before:<time instant>", observations);

            LOGGER.info("###################################### temporal filter 'last' geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, "last:3600000000,+02:00", null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("last:<time duration>", observations);

            LOGGER.info("###################################### where geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, null, "NUMERIC_VALUE > 2500");
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("last:<time duration>", observations);

            LOGGER.info(JSONObservationEncoder.encodeObservations(observations).toString(2));

            // // TODO Problem with ' in the entry, check later
            // observations = geoDBQuerier.getObservations(new String[] {
            // "Observations of Paul's thermometer" }, null, null, null, null,
            // null,
            // null);
            // assertEquals("mit Offerings '' ", 3, observations.length);

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testGetFeaturesOfInterest()
    {
        try {

            Collection<Feature> featuresOfInterest = null;

            long millis;

            LOGGER.info("######################################");
            millis = System.currentTimeMillis();
            featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(null, null, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + featuresOfInterest.size());
            Assert.assertNotNull("Without Entries", featuresOfInterest);
            Assert.assertTrue(featuresOfInterest.size() > 0);
            
            LOGGER.info("######################################");
            featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(new String[] { "BETR701" }, null, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + featuresOfInterest.size());
            Assert.assertNotNull("Without Entries", featuresOfInterest);
            Assert.assertTrue(featuresOfInterest.size() == 1);
            
            LOGGER.info("######################################");
            featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(null, new String[] { "http://sweet.jpl.nasa.gov/ontology/substance.owl#CarbonMonoxide" }, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + featuresOfInterest.size());
            Assert.assertNotNull("Without Entries", featuresOfInterest);
            Assert.assertTrue(featuresOfInterest.size() > 0);
            
            LOGGER.info("######################################");
            featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(null, null, new String[] { "CO" }, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + featuresOfInterest.size());
            Assert.assertNotNull("Without Entries", featuresOfInterest);
            Assert.assertTrue(featuresOfInterest.size() > 0);
            
            LOGGER.info("######################################");
            featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(null, null, null, "{\"xmin\":-180.0,\"ymin\":-90.0,\"xmax\":180.0,\"ymax\":90.0,\"spatialReference\":{\"wkid\":4326}}");
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + featuresOfInterest.size());
            Assert.assertNotNull("Without Entries", featuresOfInterest);
            Assert.assertTrue(featuresOfInterest.size() > 0);
            
            System.out.println(JSONEncoder.encodeSamplingFeatures(featuresOfInterest).toString(2));

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    @SuppressWarnings("unused")
    public void testGetFeatureOfInterest2() {
        
         Collection<Feature> featuresOfInterest = null;
    
         try {
             
             String[] observedProperties = new String[] {"http://sweet.jpl.nasa.gov/ontology/substance.owl#CarbonMonoxide"};
             String[] procedures = new String[] {"CO-SensorNetwork"};
             String spatialFilter = "{\"xmin\":28.000000000000057,\"ymin\":-16.570832999999936,\"xmax\":55.945591000000036,\"ymax\":28.054740000000038,\"spatialReference\":{\"wkid\":4326}}"; 
             
             featuresOfInterest = gdb.getFeatureAccess().getFeaturesOfInterest(null, observedProperties, procedures, spatialFilter);
             
         } catch (Exception e) {
             e.printStackTrace();
             fail();
         }
         
    }

    public void testGetObservationOfferings()
    {
        try {

            long millis;

            LOGGER.info("######################################");
            millis = System.currentTimeMillis();
            Collection<ObservationOffering> observationOfferings = gdb.getOfferingAccess().getProceduresAsObservationOfferings();
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observationOfferings.size());
            Assert.assertNotNull(observationOfferings);

            JSONObject encodeObservationOfferingsArray = JSONEncoder.encodeObservationOfferings(observationOfferings);

            LOGGER.info(encodeObservationOfferingsArray.toString(2));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testGetServiceDescription()
    {
        try {

            long millis;

            LOGGER.info("######################################");
            millis = System.currentTimeMillis();
            ServiceDescription serviceDescription = gdb.getServiceDescription();
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            Assert.assertNotNull(serviceDescription);

            JSONObject serviceDescriptionJson = JSONEncoder.encodeServiceDescription(serviceDescription);

            LOGGER.info(serviceDescriptionJson.toString(2));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testGetProcedures()
    {
        try {

            long millis;

            LOGGER.info("######################################");
            millis = System.currentTimeMillis();
            Collection<Procedure> procedures = gdb.getProcedureAccess().getProcedures(null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + procedures.size());
            Assert.assertNotNull(procedures);
            Assert.assertTrue(procedures.size() > 0);

            LOGGER.info("######################################");
            millis = System.currentTimeMillis();
            procedures = gdb.getProcedureAccess().getProcedures(new String[] { "CO" });
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + procedures.size());
            Assert.assertNotNull(procedures);
            Assert.assertTrue(procedures.size() > 0);

            JSONObject serviceDescriptionJson = JSONEncoder.encodeProcedures(procedures);

            LOGGER.info(serviceDescriptionJson.toString(2));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * for executing single geoDBQuerier methods:
     * 
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        AccessObservationGDBIT test = new AccessObservationGDBIT();
        test.setUp();
        test.testGetFeatureOfInterest2();
    }
}
