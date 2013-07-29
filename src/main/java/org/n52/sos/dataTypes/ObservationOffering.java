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

import org.n52.oxf.valueDomains.time.ITimePeriod;

import com.esri.arcgis.geometry.Envelope;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class ObservationOffering {

    /**
     * identifier of the offering
     */
    private String id; 
    
    /**
     * human-readable name of the offering
     */
    private String name; 
    
    /**
     * identifiers of observed properties.
     */
    private String[] observedProperties;
    
    /**
     * identifier of the procedure of this offering.
     */
    private String procedureIdentifier;
    
    /**
     * the envelope (e.g., a bbox) containing all features associated with this observation offering.
     */
    private Envelope observedArea;
    
    /**
     * the time extent containing all observation timestamps.
     */
    private ITimePeriod timeExtent;

    /**
     * @param id
     * @param name
     * @param observedProperties
     * @param procedureIdentifier
     * @param observedArea
     * @param timeExtent
     */
    public ObservationOffering(String id, String name, String[] observedProperties, String procedureIdentifier, Envelope observedArea, ITimePeriod timeExtent) {
        this.id = id;
        this.name = name;
        this.observedProperties = observedProperties;
        this.procedureIdentifier = procedureIdentifier;
        this.observedArea = observedArea;
        this.timeExtent = timeExtent;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String[] getObservedProperties()
    {
        return observedProperties;
    }

    public String getProcedureIdentifier()
    {
        return procedureIdentifier;
    }

    public Envelope getObservedArea()
    {
        return observedArea;
    }

    public ITimePeriod getTimeExtent()
    {
        return timeExtent;
    }

    public void setObservedProperties(String[] observedProperties)
    {
        this.observedProperties = observedProperties;
    }

    public void setObservedArea(Envelope observedArea)
    {
        this.observedArea = observedArea;
    }

    public void setTimeExtent(ITimePeriod timeExtent)
    {
        this.timeExtent = timeExtent;
    }
    
    
}
