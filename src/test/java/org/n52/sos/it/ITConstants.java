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
/**
 * 
 */
package org.n52.sos.it;

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines constants for integration testing of the SOS SOE.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class ITConstants {
	
	//
	// static access constants:
	//
	public static final String NETWORK_ID = "Network_GBXXXX";
	public static final String PROCEDURE_RESOURCE = "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_StationProcess.xml#GB_StationProcess_3189";

	public static String SOS_ENDPOINT_LOCAL = "http://localhost:6080/arcgis/rest/services/AirQualitySOS/MapServer/exts/52nArcGisSos";
	public static String SOS_ENDPOINT_AGS = "http://ags.dev.52north.org:6080/arcgis/rest/services/EEA/AirQualitySos/MapServer/exts/52nArcGisSos";
	
	//
	// parameter groups for GetObservation operation requests & according endpoints:
	//
	public static String SOS_GETOBSERVATION_ENDPOINT_LOCAL = SOS_ENDPOINT_LOCAL + "/GetObservation";
	public static String SOS_GETOBSERVATION_ENDPOINT_AGS   = SOS_ENDPOINT_AGS + "/GetObservation";
	
	public static Map<String, String> SOS_GETOBSERVATION = new HashMap<String, String>();
	static {
		SOS_GETOBSERVATION.put("service", "SOS");
	    SOS_GETOBSERVATION.put("version", "2.0.0");
	    SOS_GETOBSERVATION.put("request", "GetObservation");
	    SOS_GETOBSERVATION.put("offering", "Network_GBXXXX");
	    SOS_GETOBSERVATION.put("procedure", "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_StationProcess.xml#GB_StationProcess_3189");
	    SOS_GETOBSERVATION.put("featureOfInterest", "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_Sample.xml#GB_SamplingFeature_300");
	    SOS_GETOBSERVATION.put("observedProperty", "http://dd.eionet.europa.eu/vocabulary/aq/pollutant/1");
	    SOS_GETOBSERVATION.put("temporalFilter", "om:phenomenonTime,2013-03-01T01:00:00/2013-04-15T01:00:00");
	    SOS_GETOBSERVATION.put("aggregationType", "http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1d");
	    SOS_GETOBSERVATION.put("responseFormat", "http://aqd.ec.europa.eu/aqd/0.3.7c");
	    SOS_GETOBSERVATION.put("f", "xml");
	}
	public static Map<String, String> SOS_GETOBSERVATION_MULTIPLE_PROPERTIES = new HashMap<String, String>();
	static {
		SOS_GETOBSERVATION_MULTIPLE_PROPERTIES.put("service", "SOS");
		SOS_GETOBSERVATION_MULTIPLE_PROPERTIES.put("version", "2.0.0");
		SOS_GETOBSERVATION_MULTIPLE_PROPERTIES.put("request", "GetObservation");
		SOS_GETOBSERVATION_MULTIPLE_PROPERTIES.put("offering", "Network_GBXXXX");
		SOS_GETOBSERVATION_MULTIPLE_PROPERTIES.put("observedProperty", "http://dd.eionet.europa.eu/vocabulary/aq/pollutant/1,http://dd.eionet.europa.eu/vocabulary/aq/pollutant/7");
		SOS_GETOBSERVATION_MULTIPLE_PROPERTIES.put("temporalFilter", "om:phenomenonTime,2013-03-20T01:00:00/2013-04-10T01:00:00");
		SOS_GETOBSERVATION_MULTIPLE_PROPERTIES.put("aggregationType", "http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1d");
		SOS_GETOBSERVATION_MULTIPLE_PROPERTIES.put("responseFormat", "http://aqd.ec.europa.eu/aqd/0.3.7c");
		SOS_GETOBSERVATION_MULTIPLE_PROPERTIES.put("f", "xml");
	}
	public static Map<String, String> SOS_GETOBSERVATION_GET_LATEST = new HashMap<String, String>();
	static {
		SOS_GETOBSERVATION_GET_LATEST.put("service", "SOS");
		SOS_GETOBSERVATION_GET_LATEST.put("version", "2.0.0");
		SOS_GETOBSERVATION_GET_LATEST.put("request", "GetObservation");
		SOS_GETOBSERVATION_GET_LATEST.put("offering", "Network_GBXXXX");
		SOS_GETOBSERVATION_GET_LATEST.put("observedProperty", "http://dd.eionet.europa.eu/vocabulary/aq/pollutant/1,http://dd.eionet.europa.eu/vocabulary/aq/pollutant/7");
		SOS_GETOBSERVATION_GET_LATEST.put("temporalFilter", "om:phenomenonTime,latest");
		SOS_GETOBSERVATION_GET_LATEST.put("aggregationType", "http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1d");
		SOS_GETOBSERVATION_GET_LATEST.put("responseFormat", "http://aqd.ec.europa.eu/aqd/0.3.7c");
		SOS_GETOBSERVATION_GET_LATEST.put("f", "xml");
	}
	
	
	//
	// parameter groups for DescribeSensor operation requests & according endpoints:
	//
	public static String SOS_DESCRIBESENSOR_ENDPOINT_LOCAL = SOS_ENDPOINT_LOCAL + "/DescribeSensor";
	public static String SOS_DESCRIBESENSOR_ENDPOINT_AGS   = SOS_ENDPOINT_AGS + "/DescribeSensor";
	
	public static Map<String, String> SOS_DESCRIBESENSOR_NETWORK = new HashMap<String, String>();
	static {
		SOS_DESCRIBESENSOR_NETWORK.put("service", "SOS");
		SOS_DESCRIBESENSOR_NETWORK.put("version", "2.0.0");
		SOS_DESCRIBESENSOR_NETWORK.put("request", "DescribeSensor");
		SOS_DESCRIBESENSOR_NETWORK.put("procedure", "Network_GBXXXX,Network_BM0001");
		SOS_DESCRIBESENSOR_NETWORK.put("procedureDescriptionFormat", "http://www.opengis.net/sensorML/1.0.1");
		SOS_DESCRIBESENSOR_NETWORK.put("f", "xml");
	}
	public static Map<String, String> SOS_DESCRIBESENSOR_SUBCOMPONENT = new HashMap<String, String>();
	static {
		SOS_DESCRIBESENSOR_SUBCOMPONENT.put("service", "SOS");
		SOS_DESCRIBESENSOR_SUBCOMPONENT.put("version", "2.0.0");
		SOS_DESCRIBESENSOR_SUBCOMPONENT.put("request", "DescribeSensor");
		SOS_DESCRIBESENSOR_SUBCOMPONENT.put("procedure", "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_StationProcess.xml#GB_StationProcess_5,http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_StationProcess.xml#GB_StationProcess_278");
		SOS_DESCRIBESENSOR_SUBCOMPONENT.put("procedureDescriptionFormat", "http://www.opengis.net/sensorML/1.0.1");
		SOS_DESCRIBESENSOR_SUBCOMPONENT.put("f", "xml");
	}
	
	
	//
	// parameter groups for GetFeatureOfInterest operation requests & according endpoints:
	//
	public static String SOS_GETFOI_ENDPOINT_LOCAL = SOS_ENDPOINT_LOCAL + "/GetFeatureOfInterest";
	public static String SOS_GETFOI_ENDPOINT_AGS   = SOS_ENDPOINT_AGS + "/GetFeatureOfInterest";
	
	public static Map<String, String> SOS_GETFOI = new HashMap<String, String>();
	static {
		SOS_GETFOI.put("service", "SOS");
		SOS_GETFOI.put("version", "2.0.0");
		SOS_GETFOI.put("request", "GetFeatureOfInterest");
		SOS_GETFOI.put("featureOfInterest", "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_Sample.xml#GB_SamplingFeature_300");
		SOS_GETFOI.put("f", "xml");
	}
	public static Map<String, String> SOS_GETFOI_2 = new HashMap<String, String>();
	static {
		SOS_GETFOI_2.put("service", "SOS");
		SOS_GETFOI_2.put("version", "2.0.0");
		SOS_GETFOI_2.put("request", "GetFeatureOfInterest");
		SOS_GETFOI_2.put("observedProperty", "http://dd.eionet.europa.eu/vocabulary/aq/pollutant/1");
		SOS_GETFOI_2.put("procedure", "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_StationProcess.xml#GB_StationProcess_3189");//"GB_StationProcess_3189");
	    SOS_GETFOI_2.put("f", "xml");
	}
	
	
	//
	// parameter groups for GetObservationByID operation requests & according endpoints:
	//
	public static String SOS_GETOBSERVATIONBYID_ENDPOINT_LOCAL = SOS_ENDPOINT_LOCAL + "/GetObservationByID";
	public static String SOS_GETOBSERVATIONBYID_ENDPOINT_AGS   = SOS_ENDPOINT_AGS + "/GetObservationByID";
	
	public static Map<String, String> SOS_GETOBSERVATIONBYID = new HashMap<String, String>();
	static {
		SOS_GETOBSERVATIONBYID.put("service", "SOS");
		SOS_GETOBSERVATIONBYID.put("version", "2.0.0");
		SOS_GETOBSERVATIONBYID.put("request", "GetObservationByID");
		SOS_GETOBSERVATIONBYID.put("observation", "GB_Observation_333,GB_Observation_25");
		SOS_GETOBSERVATIONBYID.put("f", "xml");
	}
	
	
	//
	// parameter groups for GetCapabilities operation requests & according endpoints:
	//
	public static String SOS_GETCAPABILITIES_ENDPOINT_LOCAL = SOS_ENDPOINT_LOCAL + "/GetCapabilities";
	public static String SOS_GETCAPABILITIES_ENDPOINT_AGS   = SOS_ENDPOINT_AGS + "/GetCapabilities";
	
	public static Map<String, String> SOS_GETCAPABILITIES = new HashMap<String, String>();
	static {
		SOS_GETCAPABILITIES.put("service", "SOS");
		SOS_GETCAPABILITIES.put("version", "2.0.0");
		SOS_GETCAPABILITIES.put("request", "GetCapabilities");
		SOS_GETCAPABILITIES.put("f", "xml");
	}
}
