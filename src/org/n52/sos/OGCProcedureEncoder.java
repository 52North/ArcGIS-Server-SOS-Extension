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

import org.n52.sos.dataTypes.Procedure;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class OGCProcedureEncoder extends AbstractEncoder {

    /*
     * definition of anchor variables within template files:
     */
    private static String PROCEDURES     = "@procedures@";
    private static String PROCEDURE_ID   = "@procedure-id@";
    private static String PROCEDURE_NAME = "@procedure-name@";
    private static String PROCEDURE_DESC = "@procedure-desc@";
    private static String PROCEDURE_TYPE = "@procedure-type@";
    private static String PROCEDURE_APP  = "@procedure-app@";
    private static String PROCEDURE_LOCATION_LOWER = "@procedure-location-lowerCorner@";
    private static String PROCEDURE_LOCATION_UPPER = "@procedure-location-upperCorner@";
    private static String PROCEDURE_OUTPUTS = "@procedure-outputs@";
    
    private static String CONTACT_NAME    = "@contact-name@";
    private static String CONTACT_POS     = "@contact-posistion@";
    private static String CONTACT_STREET  = "@contact-street@";
    private static String CONTACT_CITY    = "@contact-city@";
    private static String CONTACT_COUNTRY = "@contact-country@";
    private static String CONTACT_EMAIL   = "@contact-email@";
    
    public String encodeProceduresAsSensorML20(Collection<Procedure> procedureCollection) throws IOException {
        String responseTemplate = readText(OGCProcedureEncoder.class.getResourceAsStream("template_describesensor_response.xml"));
        
        String procedureTemplate = readText(OGCProcedureEncoder.class.getResourceAsStream("template_sensor.xml"));
        
        return encodeProcedures(procedureCollection, responseTemplate, procedureTemplate);
    }
    
    public String encodeProceduresAsSensorML101(Collection<Procedure> procedureCollection) throws IOException {
        String responseTemplate = readText(OGCProcedureEncoder.class.getResourceAsStream("template_describesensor_response101.xml"));
        
        String procedureTemplate = readText(OGCProcedureEncoder.class.getResourceAsStream("template_sensor101.xml"));
        
        return encodeProcedures(procedureCollection, responseTemplate, procedureTemplate);
    }
    
    public String encodeProcedures(Collection<Procedure> procedureCollection, String responseTemplate, String procedureTemplate) throws IOException {
        
    	throw new UnsupportedOperationException();
    	
    	// TODO: adjust old encoding to new AQ e-Reporting schemas  
    	
//    	String allProcedures = "";
//        
//        for (Procedure procedure : procedureCollection) {
//            
//            String procedureString = procedureTemplate;
//            
//            procedureString = procedureString.replace(PROCEDURE_ID, procedure.getId());
//            procedureString = procedureString.replace(PROCEDURE_NAME, procedure.getName());
//            if (procedure.getDescription() != null) {
//                procedureString = procedureString.replace(PROCEDURE_DESC, procedure.getDescription());
//            } else {
//                procedureString = procedureString.replace(PROCEDURE_DESC, "");
//            }
//            procedureString = procedureString.replace(PROCEDURE_APP, procedure.getIntendedApplication());
//            procedureString = procedureString.replace(PROCEDURE_TYPE, procedure.getSensorType());
//            
//            IEnvelope e = procedure.getObservedArea().getEnvelope();
//            procedureString = procedureString.replace(PROCEDURE_LOCATION_LOWER, e.getLowerLeft().getX() + " " + e.getLowerLeft().getY());
//            procedureString = procedureString.replace(PROCEDURE_LOCATION_UPPER, e.getUpperRight().getX() + " " + e.getUpperRight().getY());
//            
//            ContactDescription c = procedure.getContact();
//            procedureString = procedureString.replace(CONTACT_NAME, c.getIndividualName());
//            procedureString = procedureString.replace(CONTACT_POS, c.getPositionName());
//            procedureString = procedureString.replace(CONTACT_STREET, c.getDeliveryPoint());
//            procedureString = procedureString.replace(CONTACT_CITY, c.getCity());
//            procedureString = procedureString.replace(CONTACT_COUNTRY, c.getCountry());
//            procedureString = procedureString.replace(CONTACT_EMAIL, c.getElectronicMailAddress());
//            
//            ObservedProperty[] obsProps = procedure.getOutputs();
//            String outputsString = "";
//            for (int i = 0; i < obsProps.length; i++) {
//                if (obsProps[i].getDataType().equalsIgnoreCase("numeric")) {
//                    outputsString += "<output name=\"output-" + i + "\">";
//                    outputsString += "<swe:Quantity definition=\""+ obsProps[i].getDescription() +"\">";
//                    outputsString += "<swe:uom code=\"" + obsProps[i].getUnitOfMeasurement() +"\"/>";
//                    outputsString += "</swe:Quantity>";
//                    outputsString += "</output>";
//                }
//                else {
//                    throw new IllegalArgumentException("Output Data Type '" + obsProps[i].getDataType() + "' not yet supported.");
//                }
//            }
//            procedureString = procedureString.replace(PROCEDURE_OUTPUTS, outputsString);
//            
//            // add procedure to the allProcedures String
//            allProcedures += procedureString + "\n";
//        }
//        
//        responseTemplate = responseTemplate.replace(PROCEDURES, allProcedures);
//        
//        return responseTemplate;
    }
}
