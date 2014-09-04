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
    private List<String> featuresOfInterestList = new ArrayList<>();
    
    /**
     * the outputs of this procedure.
     */
    private List<Output> outputsList = new ArrayList<>();
    
    /**
     * the IDs of supported aggregationTypes of this procedure.
     */
    private List<String> aggregationTypeIdList = new ArrayList<>();
    

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
		return featuresOfInterestList;
	}

	public List<Output> getOutputs() 
	{
		return outputsList;
	}

	public List<String> getAggregationTypeIDs() 
    {
		return aggregationTypeIdList;
	}
	
	public void setFeaturesOfInterest(List<String> featuresOfInterest) {
		this.featuresOfInterestList = featuresOfInterest;
	}

	public void setOutputs(List<Output> outputs) {
		this.outputsList = outputs;
	}

	public void addOutput(String property, String propertyLabel, String unitNotation) {
		Output output = new Output(property, propertyLabel, unitNotation);
		if (! this.outputsList.contains(output)) {
			this.outputsList.add(output);
		}
	}
	
	/**
	 * @return the Output of this Procedure with the given propertyID and propertyLabel.
	 * It returns <code>null</code> if no Output with that propertyID and propertyLabel 
	 * is associated with this Procedure.
	 */
	public Output getOutput(String propertyID, String propertyLabel, String unitNotation) {
		int index = this.outputsList.indexOf(new Output(propertyID, propertyLabel, unitNotation));
		if (index != -1) {
			return this.outputsList.get(index);
		}
		else {
			return null;
		}
	}
	
	public void addFeatureOfInterest(String featureID) {
		if (! this.featuresOfInterestList.contains(featureID)) {
			this.featuresOfInterestList.add(featureID);
		}
	}
	
	public void addAggregationTypeID(String aggregationTypeID) {
		if (! aggregationTypeIdList.contains(aggregationTypeID)) {
			aggregationTypeIdList.add(aggregationTypeID);
		}
	}
	
	@Override
	public boolean equals(Object otherProcedure) {
		if (otherProcedure != null) {
			if (otherProcedure instanceof Procedure) {
				Procedure p = (Procedure) otherProcedure;
				if (this.getId().equalsIgnoreCase(p.getId())
						&& this.getResource().equalsIgnoreCase(p.getResource()) ) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
    public String toString() {
    	StringBuilder result = new StringBuilder();
    	result.append("[Procedure: ");
    	result.append(id);
    	result.append(" [features: ");
    	
    	if (this.getFeaturesOfInterest() != null) {
	    	for (String featureID : this.getFeaturesOfInterest()) {
				result.append(featureID + " ");
			}
    	}
    	result.append("] [outputs: ");
    	
    	if (this.getOutputs() != null) {
	    	for (Output output : this.getOutputs()) {
				result.append(output.toString() + " ");
			}
    	}
    	result.append("]");
    	result.append("]");
    	
    	return result.toString();
    }
}