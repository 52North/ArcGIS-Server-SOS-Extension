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
import org.n52.om.result.MeasureResult;
import org.n52.oxf.valueDomains.time.ITime;

/**
 * Measurement contains double result and inherits other properties from
 * AbstractObservation
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 * 
 */
public class Measurement extends AbstractObservation {

    /** measure result of the observation */
    private MeasureResult result;

    /** type name of this observation */
    public static final String NAME = "OM_Measurement";

    /**
     * Constructor with mandatory attributes
     * 
     * @param procedure
     *            procedure property
     * @param observedProperty
     *            observed property property
     * @param featureOfInterest
     *            feature of interest property
     * @param resultTime
     *            result time property
     * @param result
     *            result
     * @throws Exception
     */
    public Measurement(Identifier identifier, String procedure, String observedProperty, String featureOfInterest, String unit, ITime resultTime, MeasureResult result)
            throws Exception {
        super(identifier, procedure, observedProperty, featureOfInterest, unit, resultTime);
        setResult(result);
    }

    @Override
    public MeasureResult getResult()
    {
        return result;
    }

    public void setResult(IResult result) throws IllegalArgumentException
    {
        if (result instanceof MeasureResult) {
            this.result = (MeasureResult) result;
        } else
            throw new IllegalArgumentException("Result type of MeasurementObservation has to be Measurement!");
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}
