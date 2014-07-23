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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.ows.ExceptionReport;
import org.n52.ows.InvalidParameterValueException;
import org.n52.ows.MissingParameterValueException;
import org.n52.ows.NoApplicableCodeException;
import org.n52.sos.Constants;
import org.n52.sos.dataTypes.Procedure;
import org.n52.sos.db.AccessGDB;
import org.n52.sos.encoder.OGCProcedureEncoder;

import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.server.json.JSONObject;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class DescribeSensorOperationHandler extends OGCOperationRequestHandler {
	
	private static final String DESCRIBE_SENSOR_OPERATION_NAME = "DescribeSensor";
    
	private static final String invalidOrMissingProcedureDescriptionFormat = "Error, operation requires 'procedureDescriptionFormat' parameter with value: '" + Constants.RESPONSE_FORMAT_SENSORML_20 + "' or '"+ Constants.RESPONSE_FORMAT_SENSORML_101 + "'.";
	private static final InvalidParameterValueException invalidParamExc = new InvalidParameterValueException(invalidOrMissingProcedureDescriptionFormat);
	private static final MissingParameterValueException missingParamExc = new MissingParameterValueException(invalidOrMissingProcedureDescriptionFormat);
	
    public DescribeSensorOperationHandler() {
        super();
    }

    /**
     * realizes the DescribeSensor operation. Expects the following parameters in the inputObject:
     * <code>service, version, request, procedure, procedureDescriptionFormat</code>
     */
    public byte[] invokeOGCOperation(AccessGDB geoDB, JSONObject inputObject,
            String[] responseProperties) throws ExceptionReport
    {
        super.invokeOGCOperation(geoDB, inputObject, responseProperties);
        
        // check 'version' parameter:
        checkMandatoryParameter(inputObject, "version", VERSION);
        
        // check 'procedureDescriptionFormat' parameter:
        String[] procedureDescriptionFormat = null;
        if (! inputObject.has("procedureDescriptionFormat")) {
            throw missingParamExc;
        }
        else {
            procedureDescriptionFormat = inputObject.getString("procedureDescriptionFormat").split(",");

            if (procedureDescriptionFormat.length != 1) {
                throw invalidParamExc;
            }
            else if (procedureDescriptionFormat[0].equalsIgnoreCase(Constants.RESPONSE_FORMAT_SENSORML_20)) {

                // TODO: implement 2.0 support:
            	throw new UnsupportedOperationException("SensorML 2.0 not yet supported...");
                //return encodeProcedures(geoDB, inputObject, PROCEDURE_DESC_FORMAT_20);
            }
            else if (procedureDescriptionFormat[0].equalsIgnoreCase(Constants.RESPONSE_FORMAT_SENSORML_101)) {
                try {
					return queryAndEncodeProcedures(geoDB, inputObject, Constants.RESPONSE_FORMAT_SENSORML_101);
				} catch (IOException e) {
					throw new NoApplicableCodeException(e);
				}
            }
            else {
                throw invalidParamExc;
            }
        }
    }
    
    /**
     * 
     * @param inputObject
     * @param sensorMLVersion
     * @return
     * @throws IOException 
     * @throws AutomationException 
     * @throws InvalidParameterValueException 
     * @throws NoApplicableCodeException 
     */
    private byte[] queryAndEncodeProcedures(AccessGDB geoDB, JSONObject inputObject, String sensorMLVersion) throws AutomationException, IOException, InvalidParameterValueException, NoApplicableCodeException {
        
    	String[] procedures = null;
    	if (inputObject.has("procedure")) {
            procedures = inputObject.getString("procedure").split(",");
        }
    	        
		/*
		 * The 'procedure' parameter of DescribeSensor can either be a
		 * NETWORK identifier or a PROCEDURE resource.
		 * 
		 * Hence, we have to check what they are first:
		 */
    	List<String> proceduresWhichAreNetworks   = new ArrayList<String>();
    	List<String> proceduresWhichAreProcedures = new ArrayList<String>();
    	for (String procedure : procedures) {
    		if (geoDB.getProcedureAccess().isNetwork(procedure)) {
    			proceduresWhichAreNetworks.add(procedure);
    		}
    		else if (geoDB.getProcedureAccess().isProcedure(procedure)) {
    			proceduresWhichAreProcedures.add(procedure);
    		}
    	}
    	
    	/*
    	 * We only support the request of one kind of procedure per request:
    	 */
    	if (proceduresWhichAreNetworks.size() > 0 && proceduresWhichAreProcedures.size() > 0) {
    		throw new InvalidParameterValueException("The parameter 'PROCEDURE' can either contain NETWORK identifiers or PROCEDURE resource identifiers. A mix is unsupported.");
    	}
    	
    	/*
    	 * Depending on type of procedure: query & encode accordingly:
    	 */
    	String result = "";
    	if (proceduresWhichAreNetworks.size() > 0) {
    		
    		Map<String, Collection<Procedure>> mapOfProceduresPerNetwork = new HashMap<String, Collection<Procedure>>();
    		
    		for (String networkID : procedures) {
    			Collection<Procedure> procedureCollection = geoDB.getProcedureAccess().getProceduresForNetwork(networkID);
    			mapOfProceduresPerNetwork.put(networkID, procedureCollection);
    		}
    		
    		if (sensorMLVersion.equalsIgnoreCase(Constants.RESPONSE_FORMAT_SENSORML_20)){
    			throw new UnsupportedOperationException();
            }
            else {
                result = new OGCProcedureEncoder().encodeNetwork_SensorML101(mapOfProceduresPerNetwork);
            }
    	}
    	else if (proceduresWhichAreProcedures.size() > 0) {
    		Collection<Procedure> procedureCollection = geoDB.getProcedureAccess().getProceduresComplete(procedures);
    		
    		if (sensorMLVersion.equalsIgnoreCase(Constants.RESPONSE_FORMAT_SENSORML_20)){
    			throw new UnsupportedOperationException();
            }
            else {
            	LOGGER.info("start encoding components as SensorML 1.0.1");
                result = new OGCProcedureEncoder().encodeComponents_SensorML101(procedureCollection);
            }
    	}
    	else { // case: no valid procedure was given in the request
    		throw new InvalidParameterValueException("The passed procedure parameter did not specify existing procedure IDs.");
    	}
    	
        return result.getBytes("utf-8");
    }

    
	protected String getOperationName() {
		return DESCRIBE_SENSOR_OPERATION_NAME;
	}

	@Override
	public int getExecutionPriority() {
		return 10;
	}

}