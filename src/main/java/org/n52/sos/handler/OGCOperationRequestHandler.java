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
package org.n52.sos.handler;


import org.n52.ows.ExceptionReport;
import org.n52.ows.InvalidParameterValueException;
import org.n52.ows.MissingParameterValueException;
import org.n52.oxf.valueDomains.time.ITime;
import org.n52.oxf.valueDomains.time.ITimePeriod;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimeFactory;
import org.n52.sos.db.AccessGDB;
import org.n52.util.logging.Logger;

import com.esri.arcgis.server.json.JSONObject;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public abstract class OGCOperationRequestHandler implements OperationRequestHandler {

    private static final String SERVICE_KEY = "service";
	private static final String REQUEST_KEY = "request";
	public static final String DEFAULT_RESPONSE_PROPERTIES = "{ \"Content-Disposition\":\"inline; filename=\\\"ogc-sos-response.xml\\\"\", \"Content-Type\":\"application/xml\" }";

	protected static Logger LOGGER = Logger.getLogger(OGCOperationRequestHandler.class.getName());
    
    protected static String SERVICE = "SOS";

    protected static String VERSION = "2.0.0";
    
    protected String sosUrlExtension;
    
    
    public OGCOperationRequestHandler() {
    }
    
    @Override
    public void initialize(String urlSosExtension) {
    	this.sosUrlExtension = urlSosExtension;
    }

    public byte[] invokeOGCOperation(AccessGDB geoDB, JSONObject inputObject,
            String[] responseProperties) throws ExceptionReport
    {
   		LOGGER.debug("Start " + getOperationName() + " query.");
        
    	if (responseProperties == null || responseProperties.length == 0){
    		responseProperties = new String[1];
    	}
    	
        responseProperties[0] = DEFAULT_RESPONSE_PROPERTIES;
        
        if (inputObject == null) {
            throw new MissingParameterValueException("Error, no parameters specified.");
        }
        
        // check 'service' parameter:
        checkMandatoryParameter(inputObject, SERVICE_KEY, SERVICE);
        
        // check 'request' parameter:
        checkMandatoryParameter(inputObject, REQUEST_KEY, getOperationName());
        
        return null;
    }
    
    /**
     * checks whether a parameter with the given parameterName is presented and has the allowedValue.
     *  
     * @throws IllegalArgumentException in case parameter is not given or value is not allowed.
     * 
     * @return the value of the checked parameter.
     * @throws ExceptionReport 
     */
    public String checkMandatoryParameter(JSONObject inputObject, String parameterName, String allowedValue) throws ExceptionReport {
        String parameterValue = null;
        if (inputObject.has(parameterName)) {
            parameterValue = inputObject.getString(parameterName);
            
            if (! parameterValue.equalsIgnoreCase(allowedValue)) {
                throw new InvalidParameterValueException("Error, '" + parameterName + "' parameter != '"+ allowedValue +"'.");
            }
        }
        else {
            throw new MissingParameterValueException("Error, operation requires '" + parameterName + "' parameter with value '" + allowedValue + "'.");
        }
        return parameterValue;
    }

    /**
     * Converts a spatial filter that conforms to the SOS 2.0 standard [OGC
     * 10-037] to a spatial filter that conforms to ESRI's GeoServices REST API
     * specification.
     * 
     * <br>
     * As spatial filter, the SOS uses a bounding box definition. The encoding
     * of the bounding box is a list of comma separated values. The first value
     * is the valueReference of the spatial property of the observations to
     * which this bounding box, as a spatial filter, is applied.
     * 
     * <br>
     * This results in the following encoding:
     * <code>valueReference,lowerCornerLongitude,lowerCornerLatitude,upperCornerLongitude,upperCornerLatitude,crsURI</code>
     * 
     * <br>
     * An example for an SOS conform spatial filter looks like this:
     * 
     * <code>spatialFilter=om:featureOfInterest/*&#47;sams:shape,0.0,40.0,2.0,43.0,urn:ogc:def:crs:EPSG::4326</code>
     * 
     * <br>
     * An example for a ESRI GeoServices REST API conform spatial filter looks like this:
     * 
     * <code>spatialFilter={xmin:0.0,ymin:40.0,xmax:2.0,ymax:43.0,spatialReference:{wkid:4326}}</code>
     * 
     * @param spatialFilterOGC
     * @return a spatial filter that conforms to ESRI's GeoServices REST API (ArcGIS REST API)
     */
    protected String convertSpatialFilterFromOGCtoArcGisREST(String spatialFilterOGC)
    {
        String[] spatialFilterOGCSubComponentArray = spatialFilterOGC.split(",");
        
        // check if array contains 6 members, otherwise throw exception
        if (spatialFilterOGCSubComponentArray.length != 6) {
            throw new IllegalArgumentException("Error while decoding spatialFilter: '" + spatialFilterOGC + "'. A split(',') should result in 6 member array.");
        }
        
        String valueReference = spatialFilterOGCSubComponentArray[0];
        
        String crsURI = spatialFilterOGCSubComponentArray[5];
        
        // check 'valueReference':
        if (!valueReference.equalsIgnoreCase("om:featureOfInterest/*/sams:shape")) {
            throw new IllegalArgumentException("Error, can only handle 'spatialFilter' with valueReference = 'om:featureOfInterest/*/sams:shape'. The valueReference '" + valueReference + "' is not supported.");
        }
        
        // check 'valueReference':
        if (!crsURI.equalsIgnoreCase("urn:ogc:def:crs:EPSG::4326")) {
            throw new IllegalArgumentException("Error, can only handle 'spatialFilter' with crsURI = 'urn:ogc:def:crs:EPSG::4326'. The crsURI '" + crsURI + "' is not supported.");
        }
        
        String lowerCornerLongitude = spatialFilterOGCSubComponentArray[1];
        String lowerCornerLatitude = spatialFilterOGCSubComponentArray[2];
        
        String upperCornerLongitude = spatialFilterOGCSubComponentArray[3];
        String upperCornerLatitude = spatialFilterOGCSubComponentArray[4];
        
        return "{xmin:"+ lowerCornerLongitude + ",ymin:"+ lowerCornerLatitude + ",xmax:"+ upperCornerLongitude + ",ymax:"+ upperCornerLatitude + ",spatialReference:{wkid:4326}}";
    }
    
    /**
     * This method converts from OGC (ISO 8601) to ArcGIS REST API-style time filters.
     * 
     * 1)
     * OGC: 2011-10-18T10:00/2011-10-19T10:00
     * to
     * ArcGIS REST API: during:2011-10-18T10:00:00+00:00,2011-10-19T10:00:00+00:00
     * 
     * 2)
     * OGC: 2011-10-18T00:00
     * to
     * ArcGIS REST API: equals:2011-10-18T00:00:00+00:00
     * 
     * @param temporalFilterOGC
     * @return
     * @throws InvalidParameterValueException 
     */
    protected static String convertTemporalFilterFromOGCtoArcGisREST(String temporalFilterOGC)
    {
        String result = "";
        
        ITime timeObj = TimeFactory.createTime(temporalFilterOGC);
        
        if (timeObj instanceof ITimePosition) {
            result += "equals:";
            result += timeObj.toISO8601Format();
        }
        else if (timeObj instanceof ITimePeriod) {
            result += "during:";
            
            ITimePeriod timePeriod = (ITimePeriod)timeObj;
            
            result += timePeriod.getStart().toISO8601Format() + "," + timePeriod.getEnd().toISO8601Format();
        }
        
        return result;
    }
    
    
	@Override
	public boolean canHandle(String operationName) {
		return operationName.equalsIgnoreCase(getOperationName());
	}
    
    protected abstract String getOperationName();
    
	@Override
	public int compareTo(OperationRequestHandler o) {
		return this.getExecutionPriority() - o.getExecutionPriority();
	}

}
