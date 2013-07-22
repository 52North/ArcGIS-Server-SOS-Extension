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

package org.n52.om.observation;

import org.n52.gml.Identifier;
import org.n52.om.result.IResult;
import org.n52.oxf.valueDomains.time.ITime;

/**
 * Abstract super class for all observation types
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 * 
 */
public abstract class AbstractObservation {

    protected Identifier identifier;

    protected String procedure;

    protected String observedProperty;

    protected String featureOfInterest;

    protected String unit;

    protected ITime resultTime;

    public AbstractObservation(Identifier identifier, String procedure, String observedProperty, String featureOfInterest, String unit, ITime resultTime) {
        this.identifier = identifier;
        this.procedure = procedure;
        this.observedProperty = observedProperty;
        this.featureOfInterest = featureOfInterest;
        this.unit = unit;
        this.resultTime = resultTime;
    }

    // /////////////////////////////////////////////////
    // abstract Methods
    /**
     * @return Returns the result of the observation
     */
    public abstract IResult getResult();

    /**
     * @return the name of the observation TYPE.
     */
    public abstract String getName();

    // /////////////////////////////////////////////////
    // getters
    /**
     * @return the gmlId
     */
    public Identifier getIdentifier()
    {
        return identifier;
    }

    /**
     * @return the resultTime
     */
    public ITime getResultTime()
    {
        return resultTime;
    }

    /**
     * @return the procedure
     */
    public String getProcedure()
    {
        return procedure;
    }

    /**
     * @return the observedProperty
     */
    public String getObservedProperty()
    {
        return observedProperty;
    }

    /**
     * @return the featureOfInterest
     */
    public String getFeatureOfInterest()
    {
        return featureOfInterest;
    }

    /**
     * @return the unit of measure
     */
    public String getUnit()
    {
        return unit;
    }

}
