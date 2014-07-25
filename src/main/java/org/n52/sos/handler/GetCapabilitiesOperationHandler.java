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
import java.util.List;
import java.util.ServiceLoader;

import org.n52.ows.ExceptionReport;
import org.n52.ows.NoApplicableCodeException;
import org.n52.sos.cache.CacheException;
import org.n52.sos.cache.CacheNotYetAvailableException;
import org.n52.sos.cache.ObservationOfferingCache;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.dataTypes.ServiceDescription;
import org.n52.sos.db.AccessGDB;
import org.n52.sos.encoder.OGCCapabilitiesEncoder;
import org.n52.sos.handler.capabilities.OperationsMetadataProvider;

import com.esri.arcgis.server.json.JSONObject;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class GetCapabilitiesOperationHandler extends OGCOperationRequestHandler {
	
	private static final String GET_CAPABILITIES_OPERATION_NAME = "GetCapabilities";
	private static List<OperationsMetadataProvider> operationsMetadataProviders;

	
    public GetCapabilitiesOperationHandler() {
        super();
    }
    
    @Override
    public void initialize(String urlSosExtension) {
    	super.initialize(urlSosExtension);
    	
    	if (operationsMetadataProviders == null) {
    		operationsMetadataProviders = loadOperationsMetadataProviders(urlSosExtension);
    	}
    }
    
    public List<OperationsMetadataProvider> loadOperationsMetadataProviders(String urlSosExtension) {

    	synchronized (this) {
			List<OperationsMetadataProvider> providers = new ArrayList<OperationsMetadataProvider>();
	    	
			ServiceLoader<OperationsMetadataProvider> loader = ServiceLoader.load(OperationsMetadataProvider.class);
			
			for (OperationsMetadataProvider omp : loader) {
				omp.setServiceURL(urlSosExtension);
				providers.add(omp);
			}
			return providers;
    	}
    }
    
    /**
     * 
     * @param inputObject
     * @return
     * @throws IOException 
     * @throws NoApplicableCodeException 
     * @throws Exception
     */
    public byte[] invokeOGCOperation(AccessGDB geoDB, JSONObject inputObject, String[] responseProperties) throws ExceptionReport
    {
        super.invokeOGCOperation(geoDB, inputObject, responseProperties);
        
//        String[] acceptVersions = null;
//        if (inputObject.has("AcceptVersions")) {
//            acceptVersions = inputObject.getString("AcceptVersions").split(",");
//        }
        try {
	        ServiceDescription serviceDesc = geoDB.getServiceDescription();
	//        Collection<ObservationOffering> obsOfferings = geoDB.getOfferingAccess().getNetworksAsObservationOfferings();
	        
	        Collection<ObservationOffering> obsOfferings;
			
			obsOfferings = ObservationOfferingCache.instance().getEntityCollection(geoDB).values();
	        
	        String capabilitiesDocument = new OGCCapabilitiesEncoder().encodeCapabilities(serviceDesc, obsOfferings, operationsMetadataProviders);
	                
	        // sending the Capabilities document:
	        LOGGER.info("Returning capabilities document.");
	        return capabilitiesDocument.getBytes("utf-8");
		} catch (CacheException | CacheNotYetAvailableException | IOException e) {
			throw new NoApplicableCodeException(e);
		}
    }

	@Override
	protected String getOperationName() {
		return GET_CAPABILITIES_OPERATION_NAME;
	}

	@Override
	public int getExecutionPriority() {
		return 4;
	} 
    
}
