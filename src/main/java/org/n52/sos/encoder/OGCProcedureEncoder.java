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

import org.n52.sos.dataTypes.Procedure;
import org.n52.sos.dataTypes.Procedure.Output;
import org.n52.util.logging.Logger;

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
    private String RESPONSE_PROCEDURES 				= "@procedures@";
    
    // templates for SensorML 2.0:
	private String responseTemplate;
	private String systemTemplate;
	private String componentTemplate;
	
	// templates for SensorML 1.0.1:
	private String responseTemplate101;
	private String systemTemplate101;
	private String componentTemplate101;
    
	
    public OGCProcedureEncoder() throws IOException {
    	super();

    	responseTemplate = readText(OGCProcedureEncoder.class.getResourceAsStream("template_describesensor_response.xml"));
		systemTemplate = readText(OGCProcedureEncoder.class.getResourceAsStream("template_sensor.xml"));
		componentTemplate = readText(OGCProcedureEncoder.class.getResourceAsStream("template_component.xml"));

        responseTemplate101 = readText(OGCProcedureEncoder.class.getResourceAsStream("template_describesensor_response101.xml"));
        systemTemplate101 = readText(OGCProcedureEncoder.class.getResourceAsStream("template_sensor101.xml"));
        componentTemplate101 = readText(OGCProcedureEncoder.class.getResourceAsStream("template_component101.xml"));
    }
    
    public String encodeProceduresAsSensorML20(Collection<Procedure> procedureCollection) throws IOException {
        return encodeProcedures(procedureCollection, responseTemplate, systemTemplate, componentTemplate);
    }
    
    public String encodeProceduresAsSensorML101(Collection<Procedure> procedureCollection) throws IOException {
        return encodeProcedures(procedureCollection, responseTemplate101, systemTemplate101, componentTemplate101);
    }
    
    public String encodeProcedures(Collection<Procedure> procedureCollection, String responseTemplate, String systemTemplate, String componentTemplate) throws IOException {
        
    	StringBuilder allProcedures = new StringBuilder("");
        
        for (Procedure procedure : procedureCollection) {
            
        	StringBuilder componentString = new StringBuilder(componentTemplate);
            
            replace(componentString, COMPONENT_PROCEDURE_ID, procedure.getId());        
            replace(componentString, COMPONENT_PROCEDURE_RESOURCE, procedure.getResource());
        	
            List<String> featureIDs = procedure.getFeaturesOfInterest();
            String featuresString = "";
            int count = 1;
            for (String featureID : featureIDs) {
            	featuresString += "<swe:field name=\"FeatureOfInterest-" + count++ + "\">\n";
            	featuresString += "   <swe:Text definition=\"om:featureOfInterest\">\n";
            	featuresString += "      <swe:value>" + featureID +"</swe:value>\n";
            	featuresString += "   </swe:Text>\n";
            	featuresString += "</swe:field>";
            }
            replace(componentString, COMPONENT_PROCEDURE_FEATURES, featuresString);
            
            List<String> aggregationTypes = procedure.getAggregationTypeIDs();
            String aggrTypeString = "";
            int count2 = 1;
            for (String aggrTypeID : aggregationTypes) {
            	aggrTypeString += "<swe:field name=\"AggregationType-" + count2++ + "\">\n";
            	aggrTypeString += "   <swe:Text definition=\"http://dd.eionet.europa.eu/vocabularies/aq/averagingperiod\">\n";
            	aggrTypeString += "      <swe:value>" + aggrTypeID +"</swe:value>\n";
            	aggrTypeString += "   </swe:Text>\n";
            	aggrTypeString += "</swe:field>";
            }
            replace(componentString, COMPONENT_PROCEDURE_AGGREGATIONTYPES, aggrTypeString);
            
            List<Output> outputs = procedure.getOutputs();
            String outputsString = "";
            for (Output output : outputs) {
                outputsString += "<output name=\"" + output.getObservedPropertyLabel() + "\">\n";
                outputsString += "   <swe:Quantity definition=\""+ output.getObservedPropertyID() +"\">\n";
                outputsString += "      <swe:uom code=\"" + output.getUnit() +"\"/>\n";
                outputsString += "   </swe:Quantity>\n";
                outputsString += "</output>";
            }
            replace(componentString, COMPONENT_PROCEDURE_OUTPUTS, outputsString);       
            
            // add procedure to the allProcedures String
            allProcedures.append(componentString + "\n");
        }
        
        StringBuilder systemString = new StringBuilder(systemTemplate);
        replace(systemString, SYSTEM_COMPONENTS, allProcedures.toString());  
        
        responseTemplate = responseTemplate.replace(RESPONSE_PROCEDURES, systemString);
        
        return responseTemplate;
    }
    
    
    public static StringBuilder replace(StringBuilder builder,
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
