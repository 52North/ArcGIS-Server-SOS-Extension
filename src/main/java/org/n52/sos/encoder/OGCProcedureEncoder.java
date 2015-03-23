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
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.n52.sos.dataTypes.Output;
import org.n52.sos.dataTypes.Procedure;
import org.n52.util.logging.Logger;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class OGCProcedureEncoder extends AbstractEncoder {
	
	protected static Logger LOGGER = Logger.getLogger(OGCProcedureEncoder.class.getName());

    /*
     * definition of anchor variables within template files:
     */
    private String COMPONENT_PROCEDURE_ID 			= "@procedure-id@";
    private String COMPONENT_PROCEDURE_RESOURCE 	= "@procedure-resource@";
    private String COMPONENT_PROCEDURE_OUTPUTS  	= "@procedure-outputs@";
    private String COMPONENT_PROCEDURE_FEATURES 	= "@procedure-features@";
    private String COMPONENT_PROCEDURE_AGGREGATIONTYPES = "@procedure-aggregationTypes@";
    private String SYSTEM_NETWORK_ID		 		= "@network-id@";
    private String SYSTEM_COMPONENTS		 		= "@components@";
    private String RESPONSE_PROCEDURES 				= "@procedures@";
	
	// templates for SensorML 1.0.1:
	private final String responseTemplate101;
	private final String systemTemplate101;
	private final String componentTemplate101;
	
    public OGCProcedureEncoder() throws IOException {
    	super();

        responseTemplate101 = readText(OGCProcedureEncoder.class.getResourceAsStream("template_describesensor_response101.xml"));
        systemTemplate101 	= readText(OGCProcedureEncoder.class.getResourceAsStream("template_sensor_network101.xml"));
        componentTemplate101= readText(OGCProcedureEncoder.class.getResourceAsStream("template_sensor_component101.xml"));
    }
    
    
    /**
     * encodes a Map of Networks containing Procedures as a SensorML 1.0.1 System with contained Components. 
     */
    public String encodeNetwork_SensorML101(Map<String, Collection<Procedure>> mapOfProceduresPerNetwork) throws IOException {
        
    	StringBuilder sensorML = new StringBuilder();
    	
    	for (String networkID : mapOfProceduresPerNetwork.keySet()) {
    	
        	StringBuilder systemSensorML = new StringBuilder();
        	systemSensorML.append(systemTemplate101);
    		replace(systemSensorML, SYSTEM_NETWORK_ID, networkID);
    		
    		Collection<Procedure> proceduresOfNetwork = mapOfProceduresPerNetwork.get(networkID);
    		
    		StringBuilder networkSensorML = new StringBuilder();
    		networkSensorML.append("<swes:description><swes:SensorDescription><swes:data><SensorML version=\"1.0.1\"><member>");
    		networkSensorML.append(encodeNetwork(proceduresOfNetwork, systemSensorML.toString()));
    		networkSensorML.append("</member></SensorML></swes:data></swes:SensorDescription></swes:description>");
    		
    		sensorML.append(networkSensorML);
    	}
    	
    	StringBuilder response = new StringBuilder();
    	response.append(responseTemplate101);
        replace(response, RESPONSE_PROCEDURES, sensorML.toString());
    	
        return response.toString();
    }
    
    /**
     * Helper method to encode network
     */
    private StringBuilder encodeNetwork(
    		Collection<Procedure> procedureCollection,  
    		String systemTemplate) throws IOException {
        
    	StringBuilder allComponents = new StringBuilder();
    	
    	if (procedureCollection.size() > 0) {
    		allComponents.append("<components><ComponentList>");
        	
	        for (Procedure procedure : procedureCollection) {
	        	
	        	StringBuilder componentEnvelope = new StringBuilder();
	        	componentEnvelope.append("<component name=\"" + procedure.getId() + "\">");
	        	componentEnvelope.append(encodeSingleProcedure(procedure));
	        	componentEnvelope.append("</component>");
	        	
	            allComponents.append(componentEnvelope);
	        }
	        
	        allComponents.append("</ComponentList></components>");
    	}
        
        StringBuilder systemString = new StringBuilder();
        systemString.append(systemTemplate);
        replace(systemString, SYSTEM_COMPONENTS, allComponents.toString());  
        
        return systemString;
    }

    
    /**
     * encodes a Collection of Procedures as SensorML 1.0.1 components.
     */
    public String encodeComponents_SensorML101(Collection<Procedure> procedureCollection) throws IOException {
    	
    	StringBuilder allProcedures = new StringBuilder();
        for (Procedure procedure : procedureCollection) {
        	
        	StringBuilder envelopedSingleProcedure = new StringBuilder();
        	envelopedSingleProcedure.append("<swes:description><swes:SensorDescription><swes:data><SensorML version=\"1.0.1\"><member>");
        	envelopedSingleProcedure.append(encodeSingleProcedure(procedure));
        	envelopedSingleProcedure.append("</member></SensorML></swes:data></swes:SensorDescription></swes:description>");
        	
            allProcedures.append(envelopedSingleProcedure);
        }
        
        StringBuilder response = new StringBuilder();
        response.append(responseTemplate101);
        replace(response, RESPONSE_PROCEDURES, allProcedures.toString());
        
        return response.toString();
    }
    
    /**
     * Helper method to encode a Procedure as a SensorML 1.0.1 Component.
     */
    private String encodeSingleProcedure(Procedure procedure) {
    	StringBuilder componentString = new StringBuilder();
    	componentString.append(componentTemplate101);
        
        replace(componentString, COMPONENT_PROCEDURE_ID, procedure.getId());        
        replace(componentString, COMPONENT_PROCEDURE_RESOURCE, procedure.getResource());
    	
        List<String> featureIDs = procedure.getFeaturesOfInterest();
        String featuresString = "";
        if (featureIDs != null) {
            int count = 1;
            for (String featureID : featureIDs) {
            	featuresString += "<swe:field name=\"FeatureOfInterest-" + count++ + "\">\n";
            	featuresString += "   <swe:Text definition=\"FeatureOfInterestID\">\n";
            	featuresString += "      <swe:value>" + featureID +"</swe:value>\n";
            	featuresString += "   </swe:Text>\n";
            	featuresString += "</swe:field>";
            }
        }
        replace(componentString, COMPONENT_PROCEDURE_FEATURES, featuresString);
        
        List<String> aggregationTypes = procedure.getAggregationTypeIDs();
        String aggrTypeString = "";
        if (aggregationTypes != null) {
            int count2 = 1;
            for (String aggrTypeID : aggregationTypes) {
            	aggrTypeString += "<swe:field name=\"AggregationType-" + count2++ + "\">\n";
            	aggrTypeString += "   <swe:Text definition=\"http://dd.eionet.europa.eu/vocabularies/aq/averagingperiod\">\n";
            	aggrTypeString += "      <swe:value>" + aggrTypeID +"</swe:value>\n";
            	aggrTypeString += "   </swe:Text>\n";
            	aggrTypeString += "</swe:field>";
            }
        }
        replace(componentString, COMPONENT_PROCEDURE_AGGREGATIONTYPES, aggrTypeString);
        
        List<Output> outputs = procedure.getOutputs();
        String outputsString = "";
        for (Output output : outputs) {
        	
        	String propertyLabel = StringEscapeUtils.escapeXml(output.getObservedPropertyLabel());

            outputsString += "<output name=\"" + propertyLabel + "\">\n";
            outputsString += "   <swe:Quantity definition=\""+ output.getObservedPropertyID() +"\">\n";
            
            if (output.getUnit() != null) {
            	outputsString += "      <swe:uom code=\"" + output.getUnit() +"\"/>\n";
            }
            
            outputsString += "   </swe:Quantity>\n";
            outputsString += "</output>";
        }
        replace(componentString, COMPONENT_PROCEDURE_OUTPUTS, outputsString);
        
        return componentString.toString();
    }
    
    
}
