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

/**
 * Represents the output of a Procedure.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class Output {

	private String observedPropertyID;

	private String observedPropertyLabel;

	private String unitNotation;

	public Output(String observedProperty, String propertyLabel,
			String unitNotation) {
		this.observedPropertyID = observedProperty;
		this.observedPropertyLabel = propertyLabel;
		this.unitNotation = unitNotation;
	}

	public String getUnit() {
		return this.unitNotation;
	}

	public String getObservedPropertyID() {
		return this.observedPropertyID;
	}

	public String getObservedPropertyLabel() {
		return this.observedPropertyLabel;
	}

	@Override
	public boolean equals(Object otherOutput) {
		if (otherOutput != null) {
			if (otherOutput instanceof Output) {
				Output p = (Output) otherOutput;

				if ((this.getObservedPropertyID() == null && p.getObservedPropertyID() == null) ||
						this.getObservedPropertyID() != null && p.getObservedPropertyID() != null
						&& this.getObservedPropertyID().equalsIgnoreCase(p.getObservedPropertyID())) {
					if ((this.getObservedPropertyLabel() == null && p.getObservedPropertyLabel() == null) ||
							this.getObservedPropertyLabel() != null && p.getObservedPropertyLabel() != null
							&& this.getObservedPropertyLabel().equalsIgnoreCase(p.getObservedPropertyLabel())) {
						if ((this.getUnit() == null && p.getUnit() == null) ||
								this.getUnit() != null && p.getUnit() != null
								&& this.getUnit().equalsIgnoreCase(p.getUnit())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "[Output: " + observedPropertyLabel + ", " + observedPropertyID
				+ ", " + unitNotation + "]";
	}
}
