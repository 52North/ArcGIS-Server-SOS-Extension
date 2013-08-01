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
import java.util.Iterator;
import java.util.List;

import org.n52.sos.dataTypes.ContactDescription;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.dataTypes.ServiceDescription;
import org.n52.sos.handler.capabilities.OperationsMetadataProvider;
import org.n52.util.logging.Logger;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class OGCCapabilitiesEncoder extends AbstractEncoder {

    
	/*
     * definition of anchor variables of Capabilities template file:
     */
    private static final String SERVICE_TITLE = "@service-title@";
    private static final String SERVICE_DESCRIPTION = "@service-description@";
    private static final String SERVICE_KEYWORDS = "@service-keywords@";
    private static final String PROVIDER_NAME = "@provider-name@";
    private static final String PROVIDER_SITE = "@provider-site@";
    private static final String PROVIDER_PHONE = "@provider-phone@";
    private static final String PROVIDER_FAX = "@provider-fax@";
    private static final String PROVIDER_DELIVERY_POINT = "@provider-delivery-point@";
    private static final String PROVIDER_CITY = "@provider-city@";
    private static final String PROVIDER_POSTAL_CODE = "@provider-postal-code@";
    private static final String PROVIDER_COUNTRY = "@provider-country@";
    private static final String PROVIDER_EMAIL = "@provider-email@";
    private static final String CONTENTS_OFFERINGS = "@contents-offerings@";
    private static final String OFFERING_IDENTIFIER = "@offering-identifier@";
    private static final String OFFERING_PROCEDURE = "@offering-procedure@";
    private static final String OFFERING_OBSERVABLE_PROPERTIES = "@offering-observable-properties@";
    private static final String OFFERING_LOWER_CORNER = "@offering-lower-corner@";
    private static final String OFFERING_UPPER_CORNER = "@offering-upper-corner@";
    private static final String OFFERING_BEGIN_POSITION = "@offering-begin-position@";
    private static final String OFFERING_END_POSITION = "@offering-end-position@";
    private static final String OPERATIONS_METADATA = "@operations-metadata@";
    
    private static String template;
    private static String offeringTemplate;
    
    static {
    	try {
			template = readText(OGCCapabilitiesEncoder.class.getResourceAsStream("template_capabilities.xml"));
			offeringTemplate = readText(OGCCapabilitiesEncoder.class.getResourceAsStream("template_capabilities_offering.xml"));
		} catch (IOException e) {
			Logger.getLogger(OGCCapabilitiesEncoder.class.getName()).warn(e.getMessage(), e);
		}
    	
    }
    
    public String encodeCapabilities(ServiceDescription sd,
    		Collection<ObservationOffering> obsOfferings, List<OperationsMetadataProvider> operations) throws IOException {
        
        // replace variables in Capabilities document template:
        
        StringBuilder templateCapabilites = new StringBuilder(template);
        
        replace(templateCapabilites, SERVICE_TITLE, sd.getTitle());        
        replace(templateCapabilites, SERVICE_DESCRIPTION, sd.getDescription());
        
        String[] keywordArray = sd.getKeywordArray();
        String keywordElement = "<ows:Keywords>";
        for (int i = 0; i < keywordArray.length; i++) {
            keywordElement += "<ows:Keyword>" + keywordArray[i] + "</ows:Keyword>";
        }
        keywordElement += "</ows:Keywords>";
        replace(templateCapabilites, SERVICE_KEYWORDS, keywordElement);
        
        replace(templateCapabilites, PROVIDER_NAME, sd.getProviderName());
        replace(templateCapabilites, PROVIDER_SITE, sd.getProviderSite());
        
        ContactDescription[] contactsArray = sd.getServiceContacts();
        replace(templateCapabilites, PROVIDER_PHONE, contactsArray[0].getPhone());
        replace(templateCapabilites, PROVIDER_FAX, contactsArray[0].getFacsimile());
        replace(templateCapabilites, PROVIDER_DELIVERY_POINT, contactsArray[0].getDeliveryPoint());
        replace(templateCapabilites, PROVIDER_CITY, contactsArray[0].getCity());
        replace(templateCapabilites, PROVIDER_POSTAL_CODE, contactsArray[0].getPostalCode());
        replace(templateCapabilites, PROVIDER_COUNTRY, contactsArray[0].getCountry());
        replace(templateCapabilites, PROVIDER_EMAIL, contactsArray[0].getElectronicMailAddress());
        
        
        // replace variables in ObservationOffering template and add to Capabilities document:
        
        StringBuilder allOfferings = new StringBuilder();
        StringBuilder templateOffering = new StringBuilder(offeringTemplate);
        
        for (Iterator<ObservationOffering> iterator = obsOfferings.iterator(); iterator.hasNext();) {
            //LOGGER.info("Offering: " + j++);

            StringBuilder offering = templateOffering;
            
            ObservationOffering obsOff = (ObservationOffering) iterator.next();
            
            replace(offering, OFFERING_IDENTIFIER, obsOff.getName());
            replace(offering, OFFERING_PROCEDURE, obsOff.getProcedureIdentifier()); // TODO replace with URL to procedure
            
            String[] obsPropArray = obsOff.getObservedProperties();
            String obsPropElements = "";
            for (int i = 0; i < obsPropArray.length; i++) {
                obsPropElements += "<swes:observableProperty>" + obsPropArray[i] + "</swes:observableProperty>";
            }
            replace(offering, OFFERING_OBSERVABLE_PROPERTIES, obsPropElements);
            
            // e.g.: <gml:lowerCorner>50.7167 7.76667</gml:lowerCorner>
            if (!obsOff.getObservedArea().isEmpty()) {
                double lowerX = obsOff.getObservedArea().getLowerLeft().getX();
                double lowerY = obsOff.getObservedArea().getLowerLeft().getY();
                replace(offering, OFFERING_LOWER_CORNER, lowerY + " " + lowerX); 
                
                double upperX = obsOff.getObservedArea().getUpperRight().getX();
                double upperY = obsOff.getObservedArea().getUpperRight().getY();
                replace(offering, OFFERING_UPPER_CORNER, upperY + " " + upperX);    
            }
            
            // e.g.: <gml:beginPosition>2009-01-11T16:22:25.00Z</gml:beginPosition>
            
            if (obsOff.getTimeExtent() != null) {
                String beginPos = obsOff.getTimeExtent().getStart().toISO8601Format();
                replace(offering, OFFERING_BEGIN_POSITION, beginPos);
                
                String endPos = obsOff.getTimeExtent().getEnd().toISO8601Format();
                replace(offering, OFFERING_END_POSITION, endPos);
            }
            
            // add offering to the allOfferings String
            allOfferings.append(offering).append("\n");
        }
        
        // add the offerings to the Capabilities document:
        replace(templateCapabilites, CONTENTS_OFFERINGS, allOfferings.toString());
        
        replace(templateCapabilites, OPERATIONS_METADATA, createOperationsMetadataMarkup(operations));
        
        // LOGGER.info("generated Capabilities: " + templateCapabilites);
        
        return templateCapabilites.toString();
    }
    
    
//    public static StringBuilder replaceFirst(StringBuilder builder,
//            String replaceWhat,
//            String replaceWith)
//    {
//        return builder.replace(builder.indexOf(replaceWhat), builder.indexOf(replaceWhat) + replaceWhat.length(), replaceWith);
//    }
    
    private String createOperationsMetadataMarkup(
			List<OperationsMetadataProvider> operations) {
    	if (operations == null || operations.size() == 0) return "";
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append("<ows:OperationsMetadata>");
    	sb.append(System.getProperty("line.separator"));
    	
    	for (OperationsMetadataProvider omp : operations) {
			sb.append(omp.createMarkup());
			sb.append(System.getProperty("line.separator"));
		}
    	
    	sb.append("</ows:OperationsMetadata>");
    	sb.append(System.getProperty("line.separator"));
    	
    	return sb.toString();
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
