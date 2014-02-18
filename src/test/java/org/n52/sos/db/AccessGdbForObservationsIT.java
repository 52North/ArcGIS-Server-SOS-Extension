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
package org.n52.sos.db;

import static org.hamcrest.CoreMatchers.is;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.n52.om.observation.MultiValueObservation;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimeConverter;
import org.n52.sos.db.impl.SubField;
import org.n52.sos.encoder.AQDObservationEncoder;
import org.n52.sos.encoder.JSONObservationEncoder;
import org.n52.sos.it.EsriTestBase;

public class AccessGdbForObservationsIT extends EsriTestBase {

    static Logger LOGGER = Logger.getLogger(AccessGdbForObservationsIT.class.getName());
    
    @Test
    public void testGetObservationsStringArray()
    {
        String observationID = "GB_Observation_59";

        try {
        	Map<String, MultiValueObservation> result = gdb.getObservationAccess().getObservations(new String[]{observationID});
        	
        	for (String key : result.keySet()) {
        		Assert.assertEquals(key, observationID);
        	}
        	
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    
	@Test
    public void testGetObservations()
    {
        try {
            String[] offerings = new String[]{"Network_GBXXXX"};
            String spatialFilter = null; //"{\"xmin\":-180.0,\"ymin\":-90.0,\"xmax\":180.0,\"ymax\":90.0,\"spatialReference\":{\"wkid\":4326}}";
            String temporalFilter = "before:2013-04-15T01:00:00";
            String where = null;//"value_numeric > 9";
            String[] observedProperties = new String[]{"http://dd.eionet.europa.eu/vocabulary/aq/pollutant/1"};
            String[] procedures = null;
            String[] featuresOfInterest = null;
            String[] aggregationTypes = new String[]{"http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1d"};
            
            Map<String, MultiValueObservation> idObsList = gdb.getObservationAccess().getObservations(offerings, featuresOfInterest, observedProperties, procedures, spatialFilter, temporalFilter, aggregationTypes, where);
            
            AQDObservationEncoder encoder = new AQDObservationEncoder();
            String result = encoder.encodeObservations(idObsList);
            
            OutputStream out = new FileOutputStream("c:/temp/observations.xml");
            out.write(result.getBytes());
            out.flush();
            out.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testCreateTemporalClauseSDE()
    {
        // TEST: equals:yyyy-MM-ddTHH:mm:ss+HH:mm
        String temporalFilter = "equals:2011-12-04T15:45:30+04:00";
        String expectedTemporalClause = SubField.VALUE_DATETIME_END + " = '2011-12-04 11:45:30'";
        String temporalClause = gdb.getObservationAccess().createTemporalClauseSDE(temporalFilter);
        Assert.assertEquals(expectedTemporalClause, temporalClause);
        
        // TEST: after:yyyy-MM-ddTHH:mm:ss+HH:mm<br>
        temporalFilter = "after:2011-12-04T15:45:30+04:00";
        expectedTemporalClause = SubField.VALUE_DATETIME_END + " > '2011-12-04 11:45:30'";
        temporalClause = gdb.getObservationAccess().createTemporalClauseSDE(temporalFilter);
        Assert.assertEquals(expectedTemporalClause, temporalClause);
        
        // TEST: before:yyyy-MM-ddTHH:mm:ss+HH:mm<br>
        temporalFilter = "before:2011-12-04T15:45:30+04:00";
        expectedTemporalClause = SubField.VALUE_DATETIME_END + " < '2011-12-04 11:45:30'";
        temporalClause = gdb.getObservationAccess().createTemporalClauseSDE(temporalFilter);
        Assert.assertEquals(expectedTemporalClause, temporalClause);
        
        // TEST: during:yyyy-MM-ddTHH:mm:ss+HH:mm,yyyy-MM-dd HH:mm:ss+HH:mm
        temporalFilter = "during:2011-12-04T15:45:30+04:00,2011-12-04T15:45:30+04:00";
        expectedTemporalClause = SubField.VALUE_DATETIME_END + " BETWEEN '2011-12-04 11:45:30' AND '2011-12-04 11:45:30'";
        temporalClause = gdb.getObservationAccess().createTemporalClauseSDE(temporalFilter);
        Assert.assertEquals(expectedTemporalClause, temporalClause);
        
        // TEST: last:milliseconds,+HH:mm
        // cannot geoDBQuerier this, since there is a System.currentTimeMillis() call in the method.
    }
    
    public void testCreatePhenomenonTimeFromDate(){
        try {
            // assuming this is the temporal filter submitted by the client: 
            String temporalFilter = "equals:2011-12-04T15:45:30+04:00";
            
            // assuming this is the corresponding date of an observation coming from our UTC database:
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2011-12-04 11:45:30");
            
            ITimePosition timePos = TimeConverter.createTimeFromDate(date, temporalFilter);
            
            Assert.assertEquals("2011-12-04T15:45:30+04:00", timePos.toISO8601Format());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testExtractTemporalOperandAfterKeyWord(){
    	Assert.assertEquals("2011-12-04T15:45:30+04:00", TimeConverter.extractTemporalOperandAfterKeyWord("equals:2011-12-04T15:45:30+04:00"));
    	Assert.assertEquals("2011-12-04T15:45:30+04:00,2011-12-04T15:50:30+04:00", TimeConverter.extractTemporalOperandAfterKeyWord("during:2011-12-04T15:45:30+04:00,2011-12-04T15:50:30+04:00"));
    	Assert.assertEquals("100,+02:00", TimeConverter.extractTemporalOperandAfterKeyWord("last:100,+02:00"));
    }
    
    /**
     * Test method for {@link
     * org.n52.sos.db.AccessObservationGDB#getObservation(...)} .
     */
    public void testVariousGetObservations()
    {
        try {

            Map<String, MultiValueObservation> observations = null;

            long millis;

            LOGGER.info("###################################### no parameter geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, null, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("without Entries ", observations);

            LOGGER.info("###################################### offering geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(new String[] { "Observations of my thermometer" }, null, null, null, null, null, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("with Offerings", observations);

            LOGGER.info("###################################### featuresOfInterest geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, new String[] { "WXT500" }, null, null, null, null, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("with feature of interest '' ", observations);

            LOGGER.info("###################################### spatial filter 1 geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, "{xmin:0.0,ymin:40.0,xmax:2.0,ymax:43.0,spatialReference:{wkid:4326}}", null, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("with feature of interest '' ", observations);

            LOGGER.info("###################################### spatial filter 2 geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, "{x:1.121389,y:41.152222,spatialReference:{wkid:4326}}", null, null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("with feature of interest '' ", observations);

            LOGGER.info("###################################### temporal filter 'equals' geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, "equals:2011-07-28T10:00:00+12:00", null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("equals:<time instant>", observations);

            LOGGER.info("###################################### temporal filter 'during' geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, "during:2011-08-13T10:00:00,2011-08-13T11:00:00", null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("during:<time start>,<time end>", observations);

            LOGGER.info("###################################### temporal filter 'after' geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, "after:2011-08-13T10:00:00", null,  null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("after:<time instant>", observations);

            LOGGER.info("###################################### temporal filter 'before' geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, "before:2011-08-13T10:00:00", null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("before:<time instant>", observations);

            LOGGER.info("###################################### temporal filter 'last' geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, "last:3600000000,+02:00", null, null);
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
            LOGGER.info("Count: " + observations.size());
            Assert.assertNotNull("last:<time duration>", observations);

            LOGGER.info("###################################### where geoDBQuerier");
            millis = System.currentTimeMillis();
            observations = gdb.getObservationAccess().getObservations(null, null, null, null, null, null, null, "NUMERIC_VALUE > 2500");
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
}
