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
import java.util.logging.Logger;

import org.n52.sos.dataTypes.ContactDescription;
import org.n52.sos.dataTypes.ObservationOffering;
import org.n52.sos.dataTypes.ObservedProperty;
import org.n52.sos.dataTypes.ServiceDescription;
import org.n52.util.Utilities;
import org.n52.util.logging.Log;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class OGCCapabilitiesEncoder {

    private static Logger LOGGER = Logger.getLogger(OGCCapabilitiesEncoder.class.getName());

    
    /*
     * definition of anchor variables of Capabilities template file:
     */
    private static String SERVICE_TITLE = "@service-title@";
    private static String SERVICE_DESCRIPTION = "@service-description@";
    private static String SERVICE_KEYWORDS = "@service-keywords@";
    private static String PROVIDER_NAME = "@provider-name@";
    private static String PROVIDER_SITE = "@provider-site@";
    private static String PROVIDER_PHONE = "@provider-phone@";
    private static String PROVIDER_FAX = "@provider-fax@";
    private static String PROVIDER_DELIVERY_POINT = "@provider-delivery-point@";
    private static String PROVIDER_CITY = "@provider-city@";
    private static String PROVIDER_POSTAL_CODE = "@provider-postal-code@";
    private static String PROVIDER_COUNTRY = "@provider-country@";
    private static String PROVIDER_EMAIL = "@provider-email@";
    private static String CONTENTS_OFFERINGS = "@contents-offerings@";
    private static String OFFERING_IDENTIFIER = "@offering-identifier@";
    private static String OFFERING_PROCEDURE = "@offering-procedure@";
    private static String OFFERING_OBSERVABLE_PROPERTIES = "@offering-observable-properties@";
    private static String OFFERING_LOWER_CORNER = "@offering-lower-corner@";
    private static String OFFERING_UPPER_CORNER = "@offering-upper-corner@";
    private static String OFFERING_BEGIN_POSITION = "@offering-begin-position@";
    private static String OFFERING_END_POSITION = "@offering-end-position@";
    
    
    public static String encodeCapabilities(ServiceDescription sd, Collection<ObservationOffering> obsOfferings) throws IOException {
        
        // replace variables in Capabilities document template:
        
        String templateCapabilites = Utilities.readText(OGCCapabilitiesEncoder.class.getResourceAsStream("template_capabilities.xml"));
        
        templateCapabilites = templateCapabilites.replace(SERVICE_TITLE, sd.getTitle());        
        templateCapabilites = templateCapabilites.replace(SERVICE_DESCRIPTION, sd.getDescription());
        
        String[] keywordArray = sd.getKeywordArray();
        String keywordElement = "<ows:Keywords>";
        for (int i = 0; i < keywordArray.length; i++) {
            keywordElement += "<ows:Keyword>" + keywordArray[i] + "</ows:Keyword>";
        }
        keywordElement += "</ows:Keywords>";
        templateCapabilites = templateCapabilites.replace(SERVICE_KEYWORDS, keywordElement);
        
        templateCapabilites = templateCapabilites.replace(PROVIDER_NAME, sd.getProviderName());
        templateCapabilites = templateCapabilites.replace(PROVIDER_SITE, sd.getProviderSite());
        
        ContactDescription[] contactsArray = sd.getServiceContacts();
        templateCapabilites = templateCapabilites.replace(PROVIDER_PHONE, contactsArray[0].getPhone());
        templateCapabilites = templateCapabilites.replace(PROVIDER_FAX, contactsArray[0].getFacsimile());
        templateCapabilites = templateCapabilites.replace(PROVIDER_DELIVERY_POINT, contactsArray[0].getDeliveryPoint());
        templateCapabilites = templateCapabilites.replace(PROVIDER_CITY, contactsArray[0].getCity());
        templateCapabilites = templateCapabilites.replace(PROVIDER_POSTAL_CODE, contactsArray[0].getPostalCode());
        templateCapabilites = templateCapabilites.replace(PROVIDER_COUNTRY, contactsArray[0].getCountry());
        templateCapabilites = templateCapabilites.replace(PROVIDER_EMAIL, contactsArray[0].getElectronicMailAddress());
        
        
        // replace variables in ObservationOffering template and add to Capabilities document:
        
        String allOfferings = "";
        String templateOffering = Utilities.readText(OGCCapabilitiesEncoder.class.getResourceAsStream("template_capabilities_offering.xml"));
        for (Iterator<ObservationOffering> iterator = obsOfferings.iterator(); iterator.hasNext();) {
            String offering = templateOffering;
            
            ObservationOffering obsOff = (ObservationOffering) iterator.next();
            
            offering = offering.replace(OFFERING_IDENTIFIER, obsOff.getName());
            offering = offering.replace(OFFERING_PROCEDURE, obsOff.getProcedureIdentifier()); // TODO replace with URL to procedure
            
            String[] obsPropArray = obsOff.getObservedProperties();
            String obsPropElements = "";
            for (int i = 0; i < obsPropArray.length; i++) {
                obsPropElements += "<swes:observableProperty>" + obsPropArray[i] + "</swes:observableProperty>";
            }
            offering = offering.replace(OFFERING_OBSERVABLE_PROPERTIES, obsPropElements);
            
            // e.g.: <gml:lowerCorner>50.7167 7.76667</gml:lowerCorner>
            if (!obsOff.getObservedArea().isEmpty()) {
                double lowerX = obsOff.getObservedArea().getLowerLeft().getX();
                double lowerY = obsOff.getObservedArea().getLowerLeft().getY();
                offering = offering.replace(OFFERING_LOWER_CORNER, lowerY + " " + lowerX); 
                
                double upperX = obsOff.getObservedArea().getUpperRight().getX();
                double upperY = obsOff.getObservedArea().getUpperRight().getY();
                offering = offering.replace(OFFERING_UPPER_CORNER, upperY + " " + upperX);    
            }
            
            // e.g.: <gml:beginPosition>2009-01-11T16:22:25.00Z</gml:beginPosition>
            
            if (obsOff.getTimeExtent() != null) {
                String beginPos = obsOff.getTimeExtent().getStart().toISO8601Format();
                offering = offering.replace(OFFERING_BEGIN_POSITION, beginPos);
                
                String endPos = obsOff.getTimeExtent().getEnd().toISO8601Format();
                offering = offering.replace(OFFERING_END_POSITION, endPos);
            }
            
            // add offering to the allOfferings String
            allOfferings += offering + "\n";
        }
        
        // add the offerings to the Capabilities document:
        templateCapabilites = templateCapabilites.replace(CONTENTS_OFFERINGS, allOfferings);
        
        LOGGER.info("generated Capabilities: " + templateCapabilites);
        
        return templateCapabilites;
    }
}
