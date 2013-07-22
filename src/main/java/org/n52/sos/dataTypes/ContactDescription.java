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

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class ContactDescription {
    
    /**
     * name of responsible individual
     */
    private String individualName; 
    
    /**
     * position of responsible individual
     */
    private String positionName;
    
    /**
     * phone number
     */
    private String phone;
    
    /**
     * fax number
     */
    private String facsimile;
    
    /**
     * delivery point (e.g. street & number) of the mailing ad-dress
     */
    private String deliveryPoint;
    
    /**
     * city of the mailing address
     */
    private String city;
    
    /**
     * administrative area of the mailing address
     */
    private String administrativeArea;
    
    /**
     * postal code of the mailing address
     */
    private String postalCode;
    
    /**
     * country of the mailing address
     */
    private String country;
    
    /**
     * e-mail address
     */
    private String electronicMailAddress;

    /**
     * @param individualName
     * @param positionName
     * @param phone
     * @param facsimile
     * @param deliveryPoint
     * @param city
     * @param administrativeArea
     * @param postalCode
     * @param country
     * @param electronicMailAddress
     */
    public ContactDescription(String individualName, String positionName, String phone, String facsimile, String deliveryPoint, String city, String administrativeArea, String postalCode,
            String country, String electronicMailAddress) {
        super();
        this.individualName = individualName;
        this.positionName = positionName;
        this.phone = phone;
        this.facsimile = facsimile;
        this.deliveryPoint = deliveryPoint;
        this.city = city;
        this.administrativeArea = administrativeArea;
        this.postalCode = postalCode;
        this.country = country;
        this.electronicMailAddress = electronicMailAddress;
    }

    public String getIndividualName()
    {
        return individualName;
    }

    public String getPositionName()
    {
        return positionName;
    }

    public String getPhone()
    {
        return phone;
    }

    public String getFacsimile()
    {
        return facsimile;
    }

    public String getDeliveryPoint()
    {
        return deliveryPoint;
    }

    public String getCity()
    {
        return city;
    }

    public String getAdministrativeArea()
    {
        return administrativeArea;
    }

    public String getPostalCode()
    {
        return postalCode;
    }

    public String getCountry()
    {
        return country;
    }

    public String getElectronicMailAddress()
    {
        return electronicMailAddress;
    }

    
    
}
