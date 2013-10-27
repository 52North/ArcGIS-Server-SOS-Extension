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

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class Procedure {

    /**
     * unique identifier of the procedure
     */
    private String id;

    /**
     * resource link of the procedure
     */
    private String resource;
    
    /**
     * the features of interest observed by this procedure.
     */
    private List<String> featuresOfInterest;
    
    /**
     * the outputs of this procedure.
     */
    private List<Output> outputs;
    

    /**
     * 
     */
    public Procedure(String id, String resource) {
        super();
        this.id = id;
        this.resource = resource;
    }

    public String getId()
    {
        return id;
    }

    public String getResource()
    {
        return resource;
    }
    
    public List<String> getFeaturesOfInterest() 
    {
		return featuresOfInterest;
	}

	public List<Output> getOutputs() 
	{
		return outputs;
	}

	public void setFeaturesOfInterest(List<String> featuresOfInterest) {
		this.featuresOfInterest = featuresOfInterest;
	}

	public void setOutputs(List<Output> outputs) {
		this.outputs = outputs;
	}

	public void addOutput(String property, String unit) {
		if (this.outputs == null) {
			this.outputs = new ArrayList<Output>();
		}
		this.outputs.add(new Output(property, unit));
	}
	
	public void addFeatureOfInterest(String featureID) {
		if (this.featuresOfInterest == null) {
			this.featuresOfInterest = new ArrayList<String>();
		}
		this.featuresOfInterest.add(featureID);
	}
	
	@Override
    public String toString() {
    	StringBuilder result = new StringBuilder("[Procedure: " + id + " [features: ");
    	
    	for (String featureID : this.getFeaturesOfInterest()) {
			result.append(featureID + " ");
		}
    	result.append("] [outputs: ");
    	
    	for (Output output : this.getOutputs()) {
			result.append(output.toString() + " ");
		}
    	result.append("]");
    	result.append("]");
    	
    	return result.toString();
    }

    /**
     * Represents the output of a Procedure.
     * 
     * @author Arne
     */
    class Output {
    	
    	private String unitNotation;
    	
    	private String observedPropertyID;

    	public Output(String observedProperty, String unit) {
    		this.unitNotation = unit;
    		this.observedPropertyID = observedProperty;
    	}
    	
    	public String getUnit() {
    		return unitNotation;
    	}

    	public String getObservedProperty() {
    		return observedPropertyID;
    	}
    	
    	@Override
        public String toString() {
        	return "[Output: " + observedPropertyID + ", " + unitNotation + "]";
        }
    }
}