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

import java.util.ArrayList;
import java.util.List;

import org.n52.om.observation.AbstractObservation;
import org.n52.oxf.valueDomains.time.ITimePosition;

/**
 * Simple data structure to group Observations regarding certain properties.
 * 
 * @author Arne
 */
public class ObservationGroup {

    protected String featureOfInterest;
    protected String observedProperty;
    protected String procedure;
    protected boolean multi;
    
    private int count;
    
    protected List<AbstractObservation> observationList;

    /** the start date of all contained phenomenon times; initially null */
    private ITimePosition phenTimeStart = null;

    /** the end date of all contained phenomenon times; initially null */
    private ITimePosition phenTimeEnd = null;
    
    /**
     * @param offering
     * @param featureOfInterest
     * @param procedure
     * @param observedProperty
     */
    public ObservationGroup(String featureOfInterest, String observedProperty, String procedure, boolean multi) {
        this.featureOfInterest = featureOfInterest;
        this.observedProperty = observedProperty;
        this.procedure = procedure;
        this.multi = multi;
    }

    public String getFeatureOfInterest()
    {
        return featureOfInterest;
    }

    public String getProcedure()
    {
        return procedure;
    }

    public String getObservedProperty()
    {
        return observedProperty;
    }

    public List<AbstractObservation> getObservationList()
    {
        return observationList;
    }

    public ITimePosition getPhenTimeStart()
    {
        return phenTimeStart;
    }

    public ITimePosition getPhenTimeEnd()
    {
        return phenTimeEnd;
    }

    public void addObservation(AbstractObservation observation)
    {
        if (observationList == null) {
            observationList = new ArrayList<AbstractObservation>();
            count = 0;
        }
        
        if (observation.getPhenomenonTime() instanceof ITimePosition) {
            ITimePosition timePos = (ITimePosition)observation.getPhenomenonTime();
            
            if (phenTimeStart == null || phenTimeStart.after(timePos)) {
                phenTimeStart = timePos;
            }
            if (phenTimeEnd == null || phenTimeEnd.before(timePos)) {
                phenTimeEnd = timePos;
            }
        }
        else{
            throw new RuntimeException("Observation has unsupported phenomenonTime type.");
        }
        
        observationList.add(observation);
        count = count + 1;
    }
    
    public int size() {
        return count;
    }
}
