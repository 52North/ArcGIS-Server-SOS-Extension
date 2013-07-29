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
package org.n52.sos.handler;

import java.io.IOException;
import java.util.Collection;

import org.n52.sos.OGCOperationRequestHandler;
import org.n52.sos.OGCProcedureEncoder;
import org.n52.sos.dataTypes.Procedure;
import org.n52.sos.db.AccessGDB;

import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.server.json.JSONObject;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class DescribeSensorOperationHandler extends OGCOperationRequestHandler {
	
	private static final String DESCRIBE_SENSOR_OPERATION_NAME = "DescribeSensor";

    protected String PROCEDURE_DESC_FORMAT_20  = "http://www.opengis.net/sensorML/2.0";
    protected String PROCEDURE_DESC_FORMAT_101 = "http://www.opengis.net/sensorML/1.0.1";
    
    public DescribeSensorOperationHandler() {
        super();
    }

    /**
     * service, version, request, procedure, procedureDescriptionFormat
     * @param inputObject
     * @return
     * @throws Exception
     */
    public byte[] invokeOGCOperation(AccessGDB geoDB, JSONObject inputObject,
            String[] responseProperties) throws Exception
    {
        super.invokeOGCOperation(geoDB, inputObject, responseProperties);

        // check 'version' parameter:
        checkMandatoryParameter(inputObject, "version", VERSION);
        
        // check 'procedureDescriptionFormat' parameter:
        String[] procedureDescriptionFormat = null;
        if (! inputObject.has("procedureDescriptionFormat")) {
            throw new IllegalArgumentException("Error, operation requires 'procedureDescriptionFormat' parameter with value: '" + PROCEDURE_DESC_FORMAT_20 + "' or '"+ PROCEDURE_DESC_FORMAT_101 + "'.");
        }
        else {
            procedureDescriptionFormat = inputObject.getString("procedureDescriptionFormat").split(",");

            if (procedureDescriptionFormat.length != 1) {
                throw new IllegalArgumentException("Error, parameter 'procedureDescriptionFormat' != '" + PROCEDURE_DESC_FORMAT_20 + "' or '"+ PROCEDURE_DESC_FORMAT_101 + "'.");
            }
            else if (procedureDescriptionFormat[0].equalsIgnoreCase(PROCEDURE_DESC_FORMAT_20)) {
                return encodeProcedures(geoDB, inputObject, PROCEDURE_DESC_FORMAT_20);
            }
            else if (procedureDescriptionFormat[0].equalsIgnoreCase(PROCEDURE_DESC_FORMAT_101)) {
                return encodeProcedures(geoDB, inputObject, PROCEDURE_DESC_FORMAT_101);
            }
            else {
                throw new IllegalArgumentException("Error, parameter 'procedureDescriptionFormat' != '" + PROCEDURE_DESC_FORMAT_20 + "' or '"+ PROCEDURE_DESC_FORMAT_101 + "'.");
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
     */
    private byte[] encodeProcedures(AccessGDB geoDB, JSONObject inputObject, String sensorMLVersion) throws AutomationException, IOException {
        String[] procedures = null;
        if (inputObject.has("procedure")) {
            procedures = inputObject.getString("procedure").split(",");
        }
        
        Collection<Procedure> procedureCollection = geoDB.getProcedureAccess().getProcedures(procedures);
        String result;
        
        if (sensorMLVersion.equalsIgnoreCase(PROCEDURE_DESC_FORMAT_20)){
            result = new OGCProcedureEncoder().encodeProceduresAsSensorML20(procedureCollection);
        }
        else {
            result = new OGCProcedureEncoder().encodeProceduresAsSensorML101(procedureCollection);
        }
        
        return result.getBytes("utf-8");
    }

	protected String getOperationName() {
		return DESCRIBE_SENSOR_OPERATION_NAME;
	}

}