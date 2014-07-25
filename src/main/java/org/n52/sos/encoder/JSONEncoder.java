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
package org.n52.sos.encoder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.n52.om.sampling.Feature;
import org.n52.ows.NoApplicableCodeException;
import org.n52.sos.dataTypes.ContactDescription;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.dataTypes.Procedure;
import org.n52.sos.dataTypes.ServiceDescription;
import org.n52.util.logging.Logger;

import com.esri.arcgis.server.json.JSONArray;
import com.esri.arcgis.server.json.JSONException;
import com.esri.arcgis.server.json.JSONObject;
import com.esri.arcgis.system.ServerUtilities;

/**
 * This class provides methods for encoding SOS-related objects in an ESRI-style
 * JSON format.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class JSONEncoder {

    static Logger LOGGER = Logger.getLogger(JSONEncoder.class.getName());
    
    /**
     * creates a JSON representation of a {@link ServiceDescription}.
     * 
     * @throws Exception
     * @throws JSONException
     */
    public static JSONObject encodeServiceDescription(ServiceDescription sd) throws JSONException
    {
        JSONObject json = new JSONObject();

        json.put("title", sd.getTitle());

        json.put("description", sd.getDescription());

        JSONArray keywordArray = new JSONArray();
        for (String keyword : sd.getKeywordArray()) {
            keywordArray.put(keyword);
        }
        json.put("keywords", keywordArray);

        json.put("providerName", sd.getProviderName());

        json.put("providerSite", sd.getProviderSite());

        JSONArray contactsArray = new JSONArray();
        for (ContactDescription c : sd.getServiceContacts()) {
            contactsArray.put(encodeServiceContact(c));
        }
        json.put("serviceContacts", contactsArray);

        JSONArray procedureArray = new JSONArray();
        for (String id : sd.getProcedureIdList()) {
            JSONObject procedure = new JSONObject();
            procedure.put("id", id);
            procedureArray.put(procedure);
        }
        json.put("procedures", procedureArray);
        

        /*
        JSONArray featureArray = new JSONArray();
        for (SpatialSamplingFeature f : sd.getFeatures()) {
            JSONObject feature = new JSONObject();
            feature.put("id", f.getIdentifier().getIdentifierValue());
            feature.put("name", f.getName());
            featureArray.put(feature);
        }
        json.put("features", featureArray);
        */
        
        return json;
    }

    /**
     * creates a JSON representation of a {@link ContactDescription}.
     * 
     */
    public static JSONObject encodeServiceContact(ContactDescription c)
    {
        JSONObject json = new JSONObject();

        json.put("individualName", c.getIndividualName());

        json.put("positionName", c.getPositionName());

        json.put("phone", c.getPhone());

        json.put("facsimile", c.getFacsimile());

        json.put("deliveryPoint", c.getDeliveryPoint());

        json.put("city", c.getCity());

        json.put("administrativeArea", c.getAdministrativeArea());

        json.put("postalCode", c.getPostalCode());

        json.put("country", c.getCountry());

        json.put("electronicMailAddress", c.getElectronicMailAddress());

        return json;
    }

    /**
     * creates a JSON representation of an {@link ObservationOffering}.
     * 
     */
    public static JSONObject encodeObservationOffering(ObservationOffering o)
    {
        JSONObject json = new JSONObject();

        json.put("id", o.getId());

        json.put("name", o.getName());

        JSONArray observedPropertiesArray = new JSONArray();
        for (String opID : o.getObservedProperties()) {
            observedPropertiesArray.put(opID);
        }
        json.put("observedProperties", observedPropertiesArray);

        json.put("procedure", o.getProcedureIdentifier());

        try {
            if (!o.getObservedArea().isEmpty()) {
                json.put("observedarea", o.getObservedArea().toJSON());
            }
        } catch (IOException e) {
        	LOGGER.warn(e.getMessage(), e);
        }

        if (o.getTimeExtent() != null) {
            json.put("timeExtent", o.getTimeExtent().toISO8601Format());
        }

        return json;
    }

    /**
     * creates a JSON representation for an array of {@link ObservationOffering}
     * s.
     * 
     */
    public static JSONObject encodeObservationOfferings(Collection<ObservationOffering> offerings)
    {
        JSONObject json = new JSONObject();

        JSONArray obsOffArray = new JSONArray();
        for (ObservationOffering oo : offerings) {
            obsOffArray.put(encodeObservationOffering(oo));
        }
        json.put("observationOfferings", obsOffArray);

        return json;
    }
    
    
    
    /**
     * creates a JSON representation of a {@link Procedure}.
     * 
     * @throws Exception
     * @throws JSONException
     */
    public static JSONObject encodeProcedure(Procedure p) throws JSONException
    {
        JSONObject json = new JSONObject();

        json.put("id", p.getId());

        json.put("resource", p.getResource());

        return json;
    }

    /**
     * creates a JSON representation of an array of {@link Procedure} objects.
     * 
     * @param procedureArray
     * @throws Exception
     * @throws JSONException
     */
    public static JSONObject encodeProcedures(Collection<Procedure> procedures) throws JSONException
    {
        JSONObject json = new JSONObject();

        JSONArray jsonProcedureArray = new JSONArray();
        for (Procedure p : procedures) {
            jsonProcedureArray.put(encodeProcedure(p));
        }
        json.put("procedures", jsonProcedureArray);

        return json;
    }

    /**
     * creates a JSON representation of a list of IDs for an array of {@link Procedure} objects.
     * 
     * @param procedures
     * @return
     */
    public static JSONObject encodeProcedureIDs(List<String> procedureIDs)
    {
        JSONObject json = new JSONObject();
        
        JSONArray jsonProcedureIDArray = new JSONArray();
        for (String procedureID : procedureIDs) {
            JSONObject proc = new JSONObject();
            proc.put("id", procedureID);
            jsonProcedureIDArray.put(proc);
        }
        json.put("procedures", jsonProcedureIDArray);
        
        return json;
    }


    /**
     * creates a JSON representation for a {@link SpatialSamplingFeature}.
     * @throws IOException 
     * @throws NoApplicableCodeException 
     * @throws  
     * @throws JSONException 
     * 
     * @throws Exception
     */
    public static JSONObject encodeSamplingFeature(Feature foi) throws JSONException, IOException, NoApplicableCodeException
    {
        JSONObject json = new JSONObject();

        if (foi.getUri() != null) {
            json.put("uri", foi.getUri());
        }
        
        if (foi.getGmlId() != null) {
            json.put("gml-id", foi.getGmlId());
        }
        
        json.put("name", foi.getName());
        
        json.put("description", foi.getDescription());

        json.put("type", foi.getFeatureType());

        json.put("sampledFeature", foi.getSampledFeature());

        //json.put("boundedBy", ServerUtilities.getJSONFromEnvelope((Envelope) foi.getBoundedBy()));

        try {
			json.put("shape", ServerUtilities.getJSONFromGeometry(foi.getShape()));
		} catch (Exception e) {
			throw new NoApplicableCodeException(e);
		}

        return json;
    }

    /**
     * creates a JSON representation for an array of
     * {@link SpatialSamplingFeature} objects.
     * 
     * @param fois
     * @throws IOException 
     * @throws NoApplicableCodeException 
     * @throws JSONException 
     */
    public static JSONObject encodeSamplingFeatures(Collection<Feature> fois) throws JSONException, NoApplicableCodeException, IOException
    {
        JSONObject json = new JSONObject();

        JSONArray jsonFeatureArray = new JSONArray();
        for (Feature p : fois) {
            jsonFeatureArray.put(encodeSamplingFeature(p));
        }
        json.put("features", jsonFeatureArray);

        return json;
    }
    
    /**
     * creates a JSON representation of a list of IDs for an array of
     * {@link SpatialSamplingFeature} objects.
     * 
     * @param fois
     */
    public static JSONObject encodeSamplingFeaturesIDs(Collection<Feature> fois)
    {
        JSONObject json = new JSONObject();
        
        JSONArray jsonFeatureIDArray = new JSONArray();
        for (Feature f : fois) {
            JSONObject feature = new JSONObject();
            feature.put("gml-id", f.getGmlId());
            jsonFeatureIDArray.put(feature);
        }
        json.put("features", jsonFeatureIDArray);
        
        return json;
    }

}
