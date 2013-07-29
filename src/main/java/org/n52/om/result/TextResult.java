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
 * Result representing a text value
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 * 
 */
public class TextResult implements IResult {

	private String value;

        private ITimePosition dateTimeBegin;

        private ITimePosition dateTimeEnd;
        
	/**
	 * Constructor
	 * 
	 * @param v
	 *            text value of this result
	 */
	public TextResult(ITimePosition dateTimeBegin,
                ITimePosition dateTimeEnd, String v) {
		this.value = v;
		this.dateTimeBegin = dateTimeBegin;
                this.dateTimeEnd = dateTimeEnd;
	}

	// specific getter and setter
	public String getTextValue() {
		return value;
	}

	public void setTextValue(String v) {
		this.value = v;
	}
	
	public ITimePosition getDateTimeBegin() {
            return this.dateTimeBegin;
        }
    
        public ITimePosition getDateTimeEnd() {
                return this.dateTimeEnd;
        }

	// generic getter and setter
	public Object getValue() {
		return getTextValue();
	}

	public void setValue(Object v) {
		setTextValue(v.toString());
	}

}
