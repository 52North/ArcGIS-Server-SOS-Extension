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
package org.n52.sos.handler.capabilities;

import java.util.ArrayList;
import java.util.List;

public class GetObservationByIDProvider extends AbstractMetadataProvider {

	private static final String OPERATION_NAME = "GetObservationByID";
	private List<Parameter> parameters;

	public GetObservationByIDProvider() {
		parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter.AnyValueParameter("observation"));
	}
	
	@Override
	protected String getGetSubUrl() {
		return "/GetObservationByID?f=xml&amp;";
	}

	@Override
	protected String getOperationName() {
		return OPERATION_NAME;
	}

	@Override
	protected List<Parameter> getParameters() {
		return parameters;
	}
	
}
