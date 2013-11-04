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

package org.n52.sos.encoder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.n52.sos.dataTypes.Procedure;
import org.n52.sos.dataTypes.Procedure.Output;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class OGCProcedureEncoder extends AbstractEncoder {

    /*
     * definition of anchor variables within template files:
     */
    private String COMPONENT_PROCEDURE_ID 			= "@procedure-id@";
    private String COMPONENT_PROCEDURE_RESOURCE 	= "@procedure-resource@";
    private String COMPONENT_PROCEDURE_OUTPUTS  	= "@procedure-outputs@";
    private String COMPONENT_PROCEDURE_FEATURES 	= "@procedure-features@";
    private String COMPONENT_PROCEDURE_AGGREGATIONTYPES = "@procedure-aggregationTypes@";
    private String SYSTEM_COMPONENTS		 		= "@components@";
    private String COMPONENT_ENVELOPE_COMPONENT		= "@component@";
    private String RESPONSE_PROCEDURES 				= "@procedures@";
    
    // templates for SensorML 2.0:
	private final String responseTemplate;
	private final String systemTemplate;
	private final String componentTemplate;
	private final String componentEnvelopeTemplate;
	
	// templates for SensorML 1.0.1:
	private final String responseTemplate101;
	private final String systemTemplate101;
	private final String componentTemplate101;
    private final String componentEnvelopeTemplate101;
	
    public OGCProcedureEncoder() throws IOException {
    	super();

    	responseTemplate 	= readText(OGCProcedureEncoder.class.getResourceAsStream("template_describesensor_response20.xml"));
		systemTemplate 		= readText(OGCProcedureEncoder.class.getResourceAsStream("template_sensor_network20.xml"));
		componentTemplate 	= readText(OGCProcedureEncoder.class.getResourceAsStream("template_sensor_component20.xml"));
		componentEnvelopeTemplate = readText(OGCProcedureEncoder.class.getResourceAsStream("template_sensor_component_envelope20.xml"));
		
        responseTemplate101 = readText(OGCProcedureEncoder.class.getResourceAsStream("template_describesensor_response101.xml"));
        systemTemplate101 	= readText(OGCProcedureEncoder.class.getResourceAsStream("template_sensor_network101.xml"));
        componentTemplate101= readText(OGCProcedureEncoder.class.getResourceAsStream("template_sensor_component101.xml"));
        componentEnvelopeTemplate101 = readText(OGCProcedureEncoder.class.getResourceAsStream("template_sensor_component_envelope101.xml"));
    	
    }
    
    public String encodeNetwork_SensorML20(Map<String, Collection<Procedure>> mapOfProceduresPerNetwork) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    public String encodeNetwork_SensorML101(Map<String, Collection<Procedure>> mapOfProceduresPerNetwork) throws IOException {
        
    	StringBuilder sensorML = new StringBuilder("");
    	
    	for (String networkID : mapOfProceduresPerNetwork.keySet()) {
    		
    		Collection<Procedure> proceduresOfNetwork = mapOfProceduresPerNetwork.get(networkID);
    		
    		StringBuilder networkSensorML = new StringBuilder("");
    		
    		networkSensorML.append("<swes:description><swes:SensorDescription><swes:data>");
    		networkSensorML.append(encodeNetwork(proceduresOfNetwork, systemTemplate101));
    		networkSensorML.append("</swes:data></swes:SensorDescription></swes:description>");
    		
    		sensorML.append(networkSensorML);
    	}
    	
        return responseTemplate.replace(RESPONSE_PROCEDURES, sensorML);
    }
    
    private StringBuilder encodeNetwork(
    		Collection<Procedure> procedureCollection,  
    		String systemTemplate) throws IOException {
        
    	StringBuilder allProcedures = new StringBuilder("");
        for (Procedure procedure : procedureCollection) {
        	
        	String singleProcedure = encodeSingleProcedure(procedure);
            allProcedures.append(singleProcedure);
        }
        
        StringBuilder systemString = new StringBuilder(systemTemplate);
        replace(systemString, SYSTEM_COMPONENTS, allProcedures.toString());  
        
        return systemString;
    }

    

    public String encodeComponents_SensorML20(Collection<Procedure> procedureCollection) throws IOException {
    	return encodeComponents(procedureCollection, responseTemplate, componentEnvelopeTemplate);
    }
    
    public String encodeComponents_SensorML101(Collection<Procedure> procedureCollection) throws IOException {
    	return encodeComponents(procedureCollection, responseTemplate101, componentEnvelopeTemplate101);
    }
    
    private String encodeComponents(Collection<Procedure> procedureCollection, String responseTemplate, String componentEnvelopeTemplate) throws IOException {
    	
    	StringBuilder allProcedures = new StringBuilder("");
        for (Procedure procedure : procedureCollection) {
        	
        	String singleProcedure = encodeSingleProcedure(procedure);
        	
        	StringBuilder envelopedSingleProcedure = new StringBuilder(componentEnvelopeTemplate);
        	replace(envelopedSingleProcedure, COMPONENT_ENVELOPE_COMPONENT, singleProcedure);
        	
            allProcedures.append(envelopedSingleProcedure);
        }
        
        responseTemplate = responseTemplate.replace(RESPONSE_PROCEDURES, allProcedures.toString());
        
        return responseTemplate;
    }
    
    
    
    private String encodeSingleProcedure(Procedure procedure) {
    	StringBuilder componentString = new StringBuilder(componentTemplate101);
        
        replace(componentString, COMPONENT_PROCEDURE_ID, procedure.getId());        
        replace(componentString, COMPONENT_PROCEDURE_RESOURCE, procedure.getResource());
    	
        List<String> featureIDs = procedure.getFeaturesOfInterest();
        String featuresString = "";
        if (featureIDs != null) {
            int count = 1;
            for (String featureID : featureIDs) {
            	featuresString += "<swe:field name=\"FeatureOfInterest-" + count++ + "\">\n";
            	featuresString += "   <swe:Text definition=\"om:featureOfInterest\">\n";
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
    
    /**
     * Helper method.
     */
	private static StringBuilder replace(StringBuilder builder,
            String replaceWhat,
            String replaceWith)
    {
        int indexOfTarget = -1;
        while ((indexOfTarget = builder.indexOf(replaceWhat)) > 0) {
            builder.replace(indexOfTarget, indexOfTarget + replaceWhat.length(), replaceWith);
        }
        return builder;
    }
}
