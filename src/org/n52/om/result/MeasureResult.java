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

package org.n52.om.result;

import org.n52.oxf.valueDomains.time.ITimePosition;

/**
 * Result representing a measured value.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 * 
 */
public class MeasureResult implements IResult {

	private String validity;

	private String verification;

	private double value;

	private ITimePosition dateTimeBegin;

	private ITimePosition dateTimeEnd;

	/**
	 * 
	 */
	public MeasureResult(ITimePosition dateTimeBegin,
			ITimePosition dateTimeEnd, String validity, String verification,
			double value) {
		this.validity = validity;
		this.verification = verification;
		this.value = value;
		this.dateTimeBegin = dateTimeBegin;
		this.dateTimeEnd = dateTimeEnd;
	}

	public ITimePosition getDateTimeBegin() {
		return this.dateTimeBegin;
	}

	public ITimePosition getDateTimeEnd() {
		return this.dateTimeEnd;
	}

	public String getValidity() {
		return this.validity;
	}

	public String getVerification() {
		return this.verification;
	}

	public Double getValue() {
		return new Double(value);
	}

}
