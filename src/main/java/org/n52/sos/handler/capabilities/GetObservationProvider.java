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
package org.n52.sos.handler.capabilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetObservationProvider extends AbstractMetadataProvider {

	private static final String OPERATION_NAME = "GetObservation";
	private List<Parameter> parameters;

	public GetObservationProvider() {
		parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("responseFormat", Arrays.asList(new String[] {
			"<ows:Value>http://www.opengis.net/om/2.0</ows:Value>",
			"<ows:Value>http://aqd.ec.europa.eu/aqd/0.3.7c</ows:Value>"
		})));
		parameters.add(new Parameter.AnyValueParameter("observedProperty"));
		parameters.add(new Parameter.AnyValueParameter("procedure"));
		parameters.add(new Parameter.AnyValueParameter("offering"));
		parameters.add(new Parameter.AnyValueParameter("temporalFilter"));
		parameters.add(new Parameter.AnyValueParameter("spatialFilter"));
		parameters.add(new Parameter.AnyValueParameter("featureOfInterest"));
	}
	
	@Override
	protected String getGetSubUrl() {
		return "/GetObservation?f=xml&amp;";
	}

	@Override
	protected String getOperationName() {
		return OPERATION_NAME;
	}

	@Override
	protected List<Parameter> getParameters() {
		return this.parameters;
	}
	
}
