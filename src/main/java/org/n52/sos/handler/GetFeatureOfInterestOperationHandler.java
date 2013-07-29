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

import java.util.Collection;

import org.n52.om.sampling.Feature;
import org.n52.sos.OGCFeatureEncoder;
import org.n52.sos.OGCOperationRequestHandler;
import org.n52.sos.db.AccessGDB;

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
            String[] responseProperties) throws Exception
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
            spatialFilter = convertSpatialFilterFromOGCtoESRI(spatialFilterOGC);
        }

        Collection<Feature> featureCollection = geoDB.getFeatureAccess().getFeaturesOfInterest(featuresOfInterest, observedProperties, procedures, spatialFilter);
        
        String result = new OGCFeatureEncoder().encodeFeatures(featureCollection);
        
        return result.getBytes("utf-8");
    }

	@Override
	protected String getOperationName() {
		return GET_FOI_OPERATION_NAME;
	}

}