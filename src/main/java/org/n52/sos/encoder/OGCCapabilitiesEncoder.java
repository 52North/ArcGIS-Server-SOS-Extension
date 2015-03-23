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

import org.n52.sos.Constants;
import org.n52.sos.dataTypes.ContactDescription;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.dataTypes.ServiceDescription;
import org.n52.sos.handler.capabilities.OperationsMetadataProvider;
import org.n52.util.CommonUtilities;

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
    private static final String CONTENTS_RESPONSE_FORMATS = "@contents-response-formats@"; 
    private static final String OFFERING_IDENTIFIER = "@offering-identifier@";
    private static final String OFFERING_PROCEDURE = "@offering-procedure@";
    private static final String OFFERING_OBSERVABLE_PROPERTIES = "@offering-observable-properties@";
    private static final String OFFERING_OBSERVED_AREA = "@offering-observed-area@";
    private static final String OFFERING_BEGIN_POSITION = "@offering-begin-position@";
    private static final String OFFERING_END_POSITION = "@offering-end-position@";
    private static final String OPERATIONS_METADATA = "@operations-metadata@";
    
    private static String template;
    private static String offeringTemplate;
    
    public OGCCapabilitiesEncoder() throws IOException {
    	super();
    	
		template = readText(OGCCapabilitiesEncoder.class.getResourceAsStream("template_capabilities.xml"));
		offeringTemplate = readText(OGCCapabilitiesEncoder.class.getResourceAsStream("template_capabilities_offering.xml"));
    }
    
    public String encodeCapabilities(ServiceDescription sd,
    		Collection<ObservationOffering> obsOfferings, List<OperationsMetadataProvider> operations) throws IOException {
        
        // replace variables in Capabilities document template:
        
    	StringBuilder templateCapabilites = new StringBuilder();
    	templateCapabilites.append(template);
        
        replace(templateCapabilites, SERVICE_TITLE, sd.getTitle());        
        replace(templateCapabilites, SERVICE_DESCRIPTION, sd.getDescription());
        
        String[] keywordArray = sd.getKeywordArray();
        String keywordElement = "";
        for (int i = 0; i < keywordArray.length; i++) {
            keywordElement += "<ows:Keyword>" + keywordArray[i].trim() + "</ows:Keyword>";
        }
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
        StringBuilder templateOffering = new StringBuilder();
        templateOffering.append(offeringTemplate);
        
        for (ObservationOffering obsOff : obsOfferings){
            StringBuilder offeringString = new StringBuilder();
            offeringString.append(templateOffering);
            
            replace(offeringString, OFFERING_IDENTIFIER, obsOff.getName());
            replace(offeringString, OFFERING_PROCEDURE, obsOff.getProcedureIdentifier()); // TODO replace with URL to procedure; e.g. DescribeSensor request to this procedure
            
            String[] obsPropArray = obsOff.getObservedProperties();
            String obsPropElements = "";
            for (int i = 0; i < obsPropArray.length; i++) {
                obsPropElements += "<swes:observableProperty>" + obsPropArray[i] + "</swes:observableProperty>";
            }
            replace(offeringString, OFFERING_OBSERVABLE_PROPERTIES, obsPropElements);
            
            // e.g.: <gml:lowerCorner>50.7167 7.76667</gml:lowerCorner>
            if (!obsOff.getObservedArea().isEmpty()) {
                double lowerX = obsOff.getObservedArea().getLowerLeft().getX();
                double lowerY = obsOff.getObservedArea().getLowerLeft().getY();
                double upperX = obsOff.getObservedArea().getUpperRight().getX();
                double upperY = obsOff.getObservedArea().getUpperRight().getY();
            
                String observedArea = "<sos:observedArea>" +
                		"<gml:Envelope srsName=\"urn:ogc:def:crs:EPSG::3857\">" +
                		"<gml:lowerCorner>" + lowerY + " " + lowerX + "</gml:lowerCorner>" +
                		"<gml:upperCorner>" + upperY + " " + upperX + "</gml:upperCorner>" +
                		"</gml:Envelope>" +
                		"</sos:observedArea>";
                
                replace(offeringString, OFFERING_OBSERVED_AREA, observedArea);    
            }
            else {
            	replace(offeringString, OFFERING_OBSERVED_AREA, "");
            }
            
            // e.g.: <gml:beginPosition>2009-01-11T16:22:25.00Z</gml:beginPosition>
            
            if (obsOff.getTimeExtent() != null) {
                String beginPos = obsOff.getTimeExtent().getStart().toISO8601Format();
                replace(offeringString, OFFERING_BEGIN_POSITION, beginPos);
                
                String endPos = obsOff.getTimeExtent().getEnd().toISO8601Format();
                replace(offeringString, OFFERING_END_POSITION, endPos);
            }
            
            // add offering to the allOfferings String
            allOfferings.append(offeringString).append("\n");
        }
        
        // add the offerings to the Capabilities document:
        replace(templateCapabilites, CONTENTS_OFFERINGS, allOfferings.toString());
        
        replace(templateCapabilites, OPERATIONS_METADATA, createOperationsMetadataMarkup(operations));
        
        // add the supported response formats:
        String[] responseFormats = new String[] {
        		Constants.RESPONSE_FORMAT_OM, 
        		Constants.RESPONSE_FORMAT_AQ
        };
        replace(templateCapabilites, CONTENTS_RESPONSE_FORMATS, createResponseFormats(responseFormats));
        
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
    	String sep = CommonUtilities.NEW_LINE_CHAR;
    	sb.append("<ows:OperationsMetadata>");
    	sb.append(sep);
    	
    	for (OperationsMetadataProvider omp : operations) {
			sb.append(omp.createMarkup());
			sb.append(sep);
		}
    	
    	sb.append("</ows:OperationsMetadata>");
    	sb.append(sep);
    	
    	return sb.toString();
	}

    private String createResponseFormats (String[] responseFormats) {
    	StringBuilder sb = new StringBuilder();
    	
    	for (int i = 0; i < responseFormats.length; i++) {
			sb.append("<sos:responseFormat>" + responseFormats[i] + "</sos:responseFormat>");
		}
    	
    	return sb.toString();
    }
    
}
