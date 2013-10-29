/**
 * 
 */
package org.n52.sos.it;

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines constants for integration testing of the SOS SOE.
 * 
 * @author Arne
 */
public class ITConstants {
	
	//
	// static access constants:
	//
	public static final String NETWORK_ID = "Network_GBXXXX";
	public static final String PROCEDURE_RESOURCE = "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_StationProcess.xml#GB_StationProcess_3189";
	
	//
	// parameter groups for GetObservation operation requests & according endpoints:
	//
	public static String SOS_GETOBSERVATION_ENDPOINT_LOCAL = "http://localhost:6080/arcgis/rest/services/AirQualitySOS/MapServer/exts/52nArcGisSos/GetObservation";
	public static Map<String, String> SOS_GETOBSERVATION_LOCAL = new HashMap<String, String>();
	static {
		SOS_GETOBSERVATION_LOCAL.put("service", "SOS");
	    SOS_GETOBSERVATION_LOCAL.put("version", "2.0.0");
	    SOS_GETOBSERVATION_LOCAL.put("request", "GetObservation");
	    SOS_GETOBSERVATION_LOCAL.put("offering", "Network_GBXXXX");
	    SOS_GETOBSERVATION_LOCAL.put("procedure", "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_StationProcess.xml#GB_StationProcess_3189");//"GB_StationProcess_3189");
	    SOS_GETOBSERVATION_LOCAL.put("featureOfInterest", "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_Sample.xml#GB_SamplingFeature_300"); //"GB_SamplingFeature_300");
	    SOS_GETOBSERVATION_LOCAL.put("observedProperty", "http://dd.eionet.europa.eu/vocabulary/aq/pollutant/1");
	    SOS_GETOBSERVATION_LOCAL.put("temporalFilter", "om:phenomenonTime,2013-03-01T01:00:00/2013-04-15T01:00:00");
	    SOS_GETOBSERVATION_LOCAL.put("aggregationType", "http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1d");
	    SOS_GETOBSERVATION_LOCAL.put("responseFormat", "http://aqd.ec.europa.eu/aqd/0.3.7c");
	    SOS_GETOBSERVATION_LOCAL.put("f", "xml");
	}
	
	
	public static String SOS_GETOBSERVATION_ENDPOINT_AGS = "http://ags.dev.52north.org:6080/arcgis/rest/services/EEA/AirQualitySos/MapServer/exts/52nArcGisSos/GetObservation";
	public static Map<String, String> SOS_GETOBSERVATION_AGS = new HashMap<String, String>();
	static {
	    SOS_GETOBSERVATION_AGS.put("service", "SOS");
	    SOS_GETOBSERVATION_AGS.put("version", "2.0.0");
	    SOS_GETOBSERVATION_AGS.put("request", "GetObservation");
	    SOS_GETOBSERVATION_AGS.put("offering", "Network_GBXXXX");
	    SOS_GETOBSERVATION_AGS.put("procedure", "GB_StationProcess_3189");
	    SOS_GETOBSERVATION_AGS.put("observedProperty", "http://dd.eionet.europa.eu/vocabulary/aq/pollutant/1");
	    SOS_GETOBSERVATION_AGS.put("temporalFilter", "om:phenomenonTime,2013-03-01T01:00:00/2013-04-15T01:00:00");
	    SOS_GETOBSERVATION_AGS.put("aggregationType", "http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1d");
	    SOS_GETOBSERVATION_AGS.put("f", "xml");
	}
	
	
	public static String SOS_DESCRIBESENSOR_ENDPOINT_LOCAL = "http://localhost:6080/arcgis/rest/services/AirQualitySOS/MapServer/exts/52nArcGisSos/DescribeSensor";
	public static Map<String, String> SOS_DESCRIBESENSOR_LOCAL = new HashMap<String, String>();
	static {
		SOS_DESCRIBESENSOR_LOCAL.put("service", "SOS");
		SOS_DESCRIBESENSOR_LOCAL.put("version", "2.0.0");
		SOS_DESCRIBESENSOR_LOCAL.put("request", "DescribeSensor");
		SOS_DESCRIBESENSOR_LOCAL.put("procedure", "Network_GBXXXX,Network_BM0001");
		SOS_DESCRIBESENSOR_LOCAL.put("procedureDescriptionFormat", "http://www.opengis.net/sensorML/1.0.1");
		SOS_DESCRIBESENSOR_LOCAL.put("f", "xml");
	}
	
	
	public static String SOS_GETFOI_ENDPOINT_LOCAL = "http://localhost:6080/arcgis/rest/services/AirQualitySOS/MapServer/exts/52nArcGisSos/GetFeatureOfInterest";
	public static Map<String, String> SOS_GETFOI_LOCAL = new HashMap<String, String>();
	static {
		SOS_GETFOI_LOCAL.put("service", "SOS");
		SOS_GETFOI_LOCAL.put("version", "2.0.0");
		SOS_GETFOI_LOCAL.put("request", "GetFeatureOfInterest");
		SOS_GETFOI_LOCAL.put("featureOfInterest", "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_Sample.xml#GB_SamplingFeature_300");
		SOS_GETFOI_LOCAL.put("f", "xml");
	}
	public static Map<String, String> SOS_GETFOI_LOCAL_2 = new HashMap<String, String>();
	static {
		SOS_GETFOI_LOCAL_2.put("service", "SOS");
		SOS_GETFOI_LOCAL_2.put("version", "2.0.0");
		SOS_GETFOI_LOCAL_2.put("request", "GetFeatureOfInterest");
		SOS_GETFOI_LOCAL_2.put("observedProperty", "http://dd.eionet.europa.eu/vocabulary/aq/pollutant/1");
		SOS_GETFOI_LOCAL_2.put("procedure", "http://cdr.eionet.europa.eu/gb/eu/aqd/e2a/colutn32a/envuvlxkq/D_GB_StationProcess.xml#GB_StationProcess_3189");//"GB_StationProcess_3189");
	    SOS_GETFOI_LOCAL_2.put("f", "xml");
	}
	
	
	public static String SOS_GETOBSERVATIONBYID_ENDPOINT_LOCAL = "http://localhost:6080/arcgis/rest/services/AirQualitySOS/MapServer/exts/52nArcGisSos/GetObservationByID";
	public static Map<String, String> SOS_GETOBSERVATIONBYID_LOCAL = new HashMap<String, String>();
	static {
		SOS_GETOBSERVATIONBYID_LOCAL.put("service", "SOS");
		SOS_GETOBSERVATIONBYID_LOCAL.put("version", "2.0.0");
		SOS_GETOBSERVATIONBYID_LOCAL.put("request", "GetObservationByID");
		SOS_GETOBSERVATIONBYID_LOCAL.put("observation", "GB_Observation_333,GB_Observation_25");
		SOS_GETOBSERVATIONBYID_LOCAL.put("f", "xml");
	}
}
