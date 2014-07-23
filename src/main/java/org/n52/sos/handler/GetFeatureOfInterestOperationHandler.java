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
import java.util.Collection;

import org.n52.om.sampling.Feature;
import org.n52.ows.ExceptionReport;
import org.n52.ows.NoApplicableCodeException;
import org.n52.sos.db.AccessGDB;
import org.n52.sos.encoder.OGCFeatureEncoder;

import com.esri.arcgis.server.json.JSONObject;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class GetFeatureOfInterestOperationHandler extends OGCOperationRequestHandler {

	private static final String GET_FOI_OPERATION_NAME = "GetFeatureOfInterest";
	
    public GetFeatureOfInterestOperationHandler() {
        super();
    }

    /**
     * @param inputObject
     * @return
     * @throws Exception
     */
    public byte[] invokeOGCOperation(AccessGDB geoDB, JSONObject inputObject,
            String[] responseProperties) throws ExceptionReport
    {
        super.invokeOGCOperation(geoDB, inputObject, responseProperties);

        // check 'version' parameter:
        checkMandatoryParameter(inputObject, "version", VERSION);

        String[] featuresOfInterest = null;
        if (inputObject.has("featureOfInterest")) {
            featuresOfInterest = inputObject.getString("featureOfInterest").split(",");
        }
        String[] observedProperties = null;
        if (inputObject.has("observedProperty")) {
            observedProperties = inputObject.getString("observedProperty").split(",");
        }
        String[] procedures = null;
        if (inputObject.has("procedure")) {
            procedures = inputObject.getString("procedure").split(",");
        }
        String spatialFilter = null;
        if (inputObject.has("spatialFilter")) {
            String spatialFilterOGC = inputObject.getString("spatialFilter");
            spatialFilter = convertSpatialFilterFromOGCtoArcGisREST(spatialFilterOGC);
        }

        try {
            Collection<Feature> featureCollection = geoDB.getFeatureAccess().getFeaturesOfInterest(featuresOfInterest, observedProperties, procedures, spatialFilter);
            
            String result = new OGCFeatureEncoder().encodeFeatures(featureCollection);
            
            return result.getBytes("utf-8");		
		} catch (IOException e) {
			throw new NoApplicableCodeException(e);
		}
    }

	@Override
	protected String getOperationName() {
		return GET_FOI_OPERATION_NAME;
	}

	@Override
	public int getExecutionPriority() {
		return 6;
	}

}