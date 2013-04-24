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
package org.n52.sos;

import static org.junit.Assert.fail;

import java.net.URI;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.n52.gml.Identifier;
import org.n52.om.observation.MultiValueObservation;
import org.n52.om.result.MeasureResult;
import org.n52.om.sampling.Feature;
import org.n52.oxf.valueDomains.time.TimeFactory;
import org.n52.oxf.valueDomains.time.TimePosition;
import org.n52.sos.dataTypes.ContactDescription;
import org.n52.sos.dataTypes.ObservedProperty;
import org.n52.sos.dataTypes.Procedure;
import org.n52.sos.dataTypes.ServiceDescription;

/**
 * @author Arne
 *
 */
public class JSONEncoderTest {

    MultiValueObservation o1;
    
    Procedure p1;
    
    Feature f1;
    
    ServiceDescription sd;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        ContactDescription contact = new ContactDescription("individual name", "positionName", "phone", "facsimile", "deliveryPoint", "city", "administrativeArea", "postalCode", "country", "electronicMailAddress");
        ObservedProperty[] outputs = new ObservedProperty[2];
        outputs[0] = new ObservedProperty("http://co", "numeric", "mg/m3");
        outputs[1] = new ObservedProperty("http://ozone", "numeric", "ug/m3");
        
        this.p1 = new Procedure("1", "p123", "name", "description", "intendedApp", "sensorType", null, contact, outputs);
        ArrayList<Procedure> procedures = new ArrayList<Procedure>();
        procedures.add(p1);
        
        this.f1 = new Feature(new Identifier(new URI("http://myserver.org"), "feature987"), "name", "desc", "http://mysampledfeature", null);
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(f1);
        
        this.sd = new ServiceDescription("title", "description", new String[] { "keyword1", "keyword2" }, "providerName", "www.providerSite.de", null, procedures);
        
        this.o1 = new MultiValueObservation(new Identifier(null, "123"), "thermometer", "temperature", "feature1", "deg", TimeFactory.createTime("2013-04-03T13:00"));
        this.o1.getResult().addResultValue(new MeasureResult(new TimePosition("2013-04-03T14:00"), new TimePosition("2013-04-03T15:00"), "1", "1", "0", 123.0));
        this.o1.getResult().addResultValue(new MeasureResult(new TimePosition("2013-04-03T15:00"), new TimePosition("2013-04-03T16:00"), "1", "1", "0", 456.0));
        this.o1.getResult().addResultValue(new MeasureResult(new TimePosition("2013-04-03T16:00"), new TimePosition("2013-04-03T17:00"), "1", "1", "0", 789.0));
    }

    @Test
    public void testEncodeObservation()
    {
        try {
            System.out.println(JSONObservationEncoder.encodeObservation(o1));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void testEncodeProcedure()
    {
        try {
            System.out.println(JSONEncoder.encodeProcedure(p1));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
