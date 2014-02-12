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
package org.n52.om.observation;

public class ValueMappingMatcher {

	private MatcherEvaluation evaluation;
	private String mappedValue;

	public ValueMappingMatcher(MatcherEvaluation eval, String mappedValue) {
		this.evaluation = eval;
		this.mappedValue = mappedValue;
	}
	
	public boolean matches(String value) {
		return this.evaluation.matches(value);
	}
	
	public String getMappedValue() {
		return this.mappedValue;
	}

	public abstract static class MatcherEvaluation {

		public abstract boolean matches(String value);
		
	}
	
}
