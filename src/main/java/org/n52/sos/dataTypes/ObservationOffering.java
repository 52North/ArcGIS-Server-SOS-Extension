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
package org.n52.sos.dataTypes;

import org.n52.oxf.valueDomains.time.ITimePeriod;
import org.n52.sos.cache.CacheEntity;


/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class ObservationOffering implements CacheEntity {

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
    private EnvelopeWrapper observedArea;
    
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
    public ObservationOffering(String id, String name, String[] observedProperties, String procedureIdentifier, EnvelopeWrapper observedArea, ITimePeriod timeExtent) {
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
    
	@Override
	public String getItemId() {
		return getId();
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

    public EnvelopeWrapper getObservedArea()
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

    public void setObservedArea(EnvelopeWrapper observedArea)
    {
        this.observedArea = observedArea;
    }

    public void setTimeExtent(ITimePeriod timeExtent)
    {
        this.timeExtent = timeExtent;
    }
    
    
}
