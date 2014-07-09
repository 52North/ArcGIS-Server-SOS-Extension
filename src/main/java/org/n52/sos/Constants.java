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
package org.n52.sos;

public class Constants {

    public static final String NULL_VALUE = "NOT_SET";
    
    public static final String FEATURE_LOCALID = "99999";
    public static final String FEATURE_NAMESPACE = "NOT_DEFINED";
    public static final Double FEATURE_INLET_HEIGHT = 9999.9;
    public static final Double FEATURE_BUILDING_DISTANCE = 9999.9;
    public static final Double FEATURE_KERB_DISTANCE = 9999.9;
    
    public static final String RESPONSE_FORMAT_AQ   = "http://aqd.ec.europa.eu/aqd/0.3.7c";
    public static final String RESPONSE_FORMAT_OM   = "http://www.opengis.net/om/2.0";
    public static final String RESPONSE_FORMAT_RDF  = "http://www.w3.org/1999/02/22-rdf-syntax-ns";
    
    public static final String RESPONSE_FORMAT_SENSORML_101  = "http://www.opengis.net/sensorML/1.0.1";
    public static final String RESPONSE_FORMAT_SENSORML_20   = "http://www.opengis.net/sensorML/2.0";

	public static final String GETOBSERVATION_DEFAULT_AGGREGATIONTYPE = "http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1h";
	public static final String GETOBSERVATION_DEFAULT_AGGREGATIONTYPE_ALT = "http://dd.eionet.europa.eu/vocabulary/aq/primaryObservation/hour";
	
	public static final String GETOBSERVATION_DEFAULT_AGGREGATIONTYPE_SECOND = "http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1d";
	public static final String GETOBSERVATION_DEFAULT_AGGREGATIONTYPE_SECOND_ALT = "http://dd.eionet.europa.eu/vocabulary/aq/primaryObservation/day";
}
