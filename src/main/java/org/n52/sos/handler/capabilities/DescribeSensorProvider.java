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

import org.n52.sos.Constants;

public class DescribeSensorProvider extends AbstractMetadataProvider {

	private static final String OPERATION_NAME = "DescribeSensor";
	private List<Parameter> parameters;

	public DescribeSensorProvider() {
		parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("procedureDescriptionFormat", Arrays.asList(new String[] {
			"<ows:Value>"+ Constants.RESPONSE_FORMAT_SENSORML_101 +"</ows:Value>"
		})));
		parameters.add(new Parameter.AnyValueParameter("procedure"));
	}
	
	@Override
	protected String getGetSubUrl() {
		return "/DescribeSensor?f=xml&amp;";
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
