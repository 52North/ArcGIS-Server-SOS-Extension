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

package org.n52.sos.encoder;

import java.io.IOException;

import org.n52.util.logging.Logger;

/**
 * Overrides the {@link OGCObservationSWECommonEncoder} and uses AQD specific templates.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class AQDObservationEncoder extends OGCObservationSWECommonEncoder {

    protected static Logger LOGGER = Logger.getLogger(AQDObservationEncoder.class.getName());
	
    private String aqdObservationTemplate;
	private String aqdObservationEnvelopeTemplate;
    
    public AQDObservationEncoder() throws IOException {
    	super();
    	
		aqdObservationTemplate = AbstractEncoder.readText(
			OGCObservationSWECommonEncoder.class.getResourceAsStream("template_aqd_observation.xml"));
		aqdObservationEnvelopeTemplate = AbstractEncoder.readText(
    		OGCObservationSWECommonEncoder.class.getResourceAsStream("template_getobservation_response_AQD.xml"));
    }
    
    
    @Override
    protected String getObservationEnvelopeTemplate() {
    	return aqdObservationEnvelopeTemplate;
    }
    
    @Override
    protected String getObservationTemplate() {
    	return aqdObservationTemplate;
    }
}
