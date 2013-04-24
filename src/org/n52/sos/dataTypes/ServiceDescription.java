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

package org.n52.sos.dataTypes;

import java.util.List;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class ServiceDescription {

    /**
     * title of the SOSExt service
     */
    private String title;
    
    /**
     * abstract description
     */
    private String description;
    
    /**
     * keywords describing the SOSExt service
     */
    private String[] keywordArray;

    /**
     * name of the service provider organization
     */
    private String providerName;

    /**
     * URL to web site of the service provider
     */
    private String providerSite;

    /**
     * contact details of the service provider
     */
    private ContactDescription[] serviceContacts;

    /**
     * procedures associated with the SOSExt
     */
    private List<String> procedureIdList;
    
    /**
     * 
     */
    public ServiceDescription(String title, String description, String[] keywordArray, String providerName, String providerSite, ContactDescription[] serviceContacts, List<String> procedureIdList) {
        super();
        this.title = title;
        this.description = description;
        this.keywordArray = keywordArray;
        this.providerName = providerName;
        this.providerSite = providerSite;
        this.serviceContacts = serviceContacts;
        this.procedureIdList = procedureIdList;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public String[] getKeywordArray()
    {
        return keywordArray;
    }

    public String getProviderName()
    {
        return providerName;
    }

    public String getProviderSite()
    {
        return providerSite;
    }

    public ContactDescription[] getServiceContacts()
    {
        return serviceContacts;
    }

    public List<String> getProcedureIdList()
    {
        return procedureIdList;
    }

}
