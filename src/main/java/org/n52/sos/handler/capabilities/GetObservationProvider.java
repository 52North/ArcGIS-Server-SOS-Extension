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

public class GetObservationProvider extends AbstractMetadataProvider {

	private static final String OPERATION_NAME = "GetObservation";
	private List<Parameter> parameters;

	public GetObservationProvider() {
		parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("responseFormat", Arrays.asList(new String[] {
			"<ows:Value>"+ Constants.RESPONSE_FORMAT_OM +"</ows:Value>",
			"<ows:Value>"+ Constants.RESPONSE_FORMAT_AQ +"</ows:Value>"
		})));
		parameters.add(new Parameter.AnyValueParameter("observedProperty"));
		parameters.add(new Parameter.AnyValueParameter("procedure"));
		parameters.add(new Parameter.AnyValueParameter("offering"));
		parameters.add(new Parameter.AnyValueParameter("temporalFilter"));
		parameters.add(new Parameter.AnyValueParameter("spatialFilter"));
		parameters.add(new Parameter.AnyValueParameter("featureOfInterest"));
		parameters.add(new Parameter("aggregationType", Arrays.asList(new String[] {
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1d</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1h</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/3y</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/8h</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/highsummer</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/maxd8h</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/summer</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/winter</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1ddc</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1dmf</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_nt</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_nv</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_min</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_max</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_dc</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1ydx_nv</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1ydx_min</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1ydx_max</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1ydx_dc</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1ydx</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_max19</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1ydx_max4</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_max25</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1ydx_max26</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1ydx_max36</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_ex180</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_ex200</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_ex240</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_ex350</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_ex400</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1ydx_ex10</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1ydx_ex50</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1ydx_ex120</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1ydx_ex125</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_ex100</ows:Value>",
				"<ows:Value>http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1y_pe50</ows:Value>",
			})));
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
