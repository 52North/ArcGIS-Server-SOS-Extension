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

	private static ITConstants testConstants;
	
	//
	// constants:
	//
	private static Map<String, String> SOS_GETOBSERVATION_LOCAL = new HashMap<String, String>();
	private static String SOS_GETOBSERVATION_ENDPOINT_LOCAL = "http://localhost:6080/arcgis/rest/services/ObservationDB/MapServer/exts/SOSExtension/GetObservation";
	
	private static Map<String, String> SOS_GETOBSERVATION_AGS = new HashMap<String, String>();
	private static String SOS_GETOBSERVATION_ENDPOINT_AGS = "http://ags.dev.52north.org:6080/arcgis/rest/services/EEA/AirQualitySos/MapServer/exts/52nArcGisSos/GetObservation";
	
	private ITConstants () {
		
		// init GetObservation Query Group 1:
		SOS_GETOBSERVATION_LOCAL.put("service", "SOS");
	    SOS_GETOBSERVATION_LOCAL.put("version", "2.0.0");
	    SOS_GETOBSERVATION_LOCAL.put("request", "GetObservation");
	    SOS_GETOBSERVATION_LOCAL.put("offering", "Network_GBXXXX");
	    SOS_GETOBSERVATION_LOCAL.put("procedure", "GB_StationProcess_7");
	    SOS_GETOBSERVATION_LOCAL.put("observedProperty", "http://dd.eionet.europa.eu/vocabulary/aq/pollutant/7");
	    SOS_GETOBSERVATION_LOCAL.put("temporalFilter", "om:phenomenonTime,2013-04-01T01:00:00/2013-04-10T01:00:00");
	    SOS_GETOBSERVATION_LOCAL.put("aggregationType", "http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1d");
	    SOS_GETOBSERVATION_LOCAL.put("f", "xml");
	    
	    SOS_GETOBSERVATION_AGS.put("service", "SOS");
	    SOS_GETOBSERVATION_AGS.put("version", "2.0.0");
	    SOS_GETOBSERVATION_AGS.put("request", "GetObservation");
	    SOS_GETOBSERVATION_AGS.put("offering", "Network_GBXXXX");
	    SOS_GETOBSERVATION_AGS.put("procedure", "GB_StationProcess_7");
	    SOS_GETOBSERVATION_AGS.put("observedProperty", "http://dd.eionet.europa.eu/vocabulary/aq/pollutant/7");
	    SOS_GETOBSERVATION_AGS.put("temporalFilter", "om:phenomenonTime,2013-04-01T01:00:00/2013-04-10T01:00:00");
	    SOS_GETOBSERVATION_AGS.put("aggregationType", "http://dd.eionet.europa.eu/vocabulary/aq/averagingperiod/1d");
	    SOS_GETOBSERVATION_AGS.put("f", "xml");
	}
	
	public static ITConstants getInstance() {
		if (testConstants == null) {
			return new ITConstants();
		}
		else {
			return testConstants;
		}
	}
	
	public Map<String, String> getSosGetObservationLocal () {
		return SOS_GETOBSERVATION_LOCAL;
	}
	
	public String getSosGetObservationEndpointLocal() {
		return SOS_GETOBSERVATION_ENDPOINT_LOCAL;
	}
	

	public Map<String, String> getSosGetObservationAgs () {
		return SOS_GETOBSERVATION_AGS;
	}
	
	public String getSosGetObservationEndpointAgs() {
		return SOS_GETOBSERVATION_ENDPOINT_AGS;
	}
}
