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

import java.util.Date;
import java.util.logging.Logger;

import org.junit.Test;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimeFactory;
import org.n52.sos.it.EsriTestBase;

/**
 * @author Arne
 *
 */
public class InsertGdbForObservationsIT extends EsriTestBase {

    static Logger LOGGER = Logger.getLogger(AccessObservationGDBIT.class.getName());
    
    
    /**
     * Test method for {@link org.n52.sos.db.InsertGdbForObservations#insertObservation(int, com.esri.arcgis.system.Time, int, int, int, java.lang.String, float)}.
     */
    @Test
    public void testInsertObservation()
    {
        try {
            long millis;
            LOGGER.info("######################################");
            millis = System.currentTimeMillis();
            
            int offeringID = 1;
            
            String phenomenonTime = "2012-08-23T15:45:00";
            ITimePosition t = (ITimePosition)TimeFactory.createTime(phenomenonTime);
            Date phenomenonTimeAsDate = t.getCalendar().getTime();
            
            int featureID = 112;
            
            int observedPropertyID = 1;
            
            int procedureID = 1;
            
            float result = 23;

            int observationID = this.gdb.getObservationInsert().insertObservation(offeringID, phenomenonTimeAsDate, featureID, observedPropertyID, procedureID, result);
            LOGGER.info("new observation ID: " + observationID);
            
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetObservationMaxID()
    {
        try {
            long millis;
            LOGGER.info("######################################");
            millis = System.currentTimeMillis();
            
            int maxID = this.gdb.getObservationInsert().getObservationMaxID();
            LOGGER.info("Max ID: " + maxID);
            
            LOGGER.info("Duration: " + (System.currentTimeMillis() - millis));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
