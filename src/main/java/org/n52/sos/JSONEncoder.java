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

package org.n52.sos;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.n52.om.sampling.Feature;
import org.n52.sos.dataTypes.ContactDescription;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.dataTypes.Procedure;
import org.n52.sos.dataTypes.ServiceDescription;
import com.esri.arcgis.interop.AutomationException;
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
    public static JSONObject encodeServiceDescription(ServiceDescription sd) throws JSONException, Exception
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
                json.put("observedarea", ServerUtilities.getJSONFromEnvelope(o.getObservedArea()));
            }
        } catch (AutomationException e) {
            LOGGER.log(Level.WARNING, "", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "", e);
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
    public static JSONObject encodeProcedure(Procedure p) throws JSONException, Exception
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
    public static JSONObject encodeProcedures(Collection<Procedure> procedures) throws JSONException, Exception
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
    public static JSONObject encodeProcedureIDs(Collection<Procedure> procedures)
    {
        JSONObject json = new JSONObject();
        
        JSONArray jsonProcedureIDArray = new JSONArray();
        for (Procedure p : procedures) {
            JSONObject proc = new JSONObject();
            proc.put("id", p.getId());
            jsonProcedureIDArray.put(proc);
        }
        json.put("procedures", jsonProcedureIDArray);
        
        return json;
    }


    /**
     * creates a JSON representation for a {@link SpatialSamplingFeature}.
     * 
     * @throws Exception
     */
    public static JSONObject encodeSamplingFeature(Feature foi) throws Exception
    {
        JSONObject json = new JSONObject();

        if (foi.getIdentifier() != null) {
            json.put("id", foi.getIdentifier().getIdentifierValue());
            if (foi.getIdentifier().getCodeSpace() != null) {
                json.put("codeSpace", foi.getIdentifier().getCodeSpace().toString());
            }
        }
        
        json.put("name", foi.getName());
        
        json.put("description", foi.getDescription());

        json.put("type", foi.getFeatureType());

        json.put("sampledFeature", foi.getSampledFeature());

        //json.put("boundedBy", ServerUtilities.getJSONFromEnvelope((Envelope) foi.getBoundedBy()));

        json.put("shape", ServerUtilities.getJSONFromGeometry(foi.getShape()));

        return json;
    }

    /**
     * creates a JSON representation for an array of
     * {@link SpatialSamplingFeature} objects.
     * 
     * @param fois
     */
    public static JSONObject encodeSamplingFeatures(Collection<Feature> fois) throws Exception
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
            feature.put("id", f.getIdentifier().getIdentifierValue());
            jsonFeatureIDArray.put(feature);
        }
        json.put("features", jsonFeatureIDArray);
        
        return json;
    }

}
