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
package org.n52.ows;

public class ResponseExceedsSizeLimitException extends ExceptionReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String CODE = "ResponseExceedsSizeLimit";
	private static final String TEXT = "The requested result set exceeds the response size limit of this service and thus cannot be delivered.";

	public ResponseExceedsSizeLimitException() {
		super(CODE, TEXT);
	}
	
	public ResponseExceedsSizeLimitException(int currentMaxCount) {
		super(CODE, TEXT.concat(" The current maximum record count is set to "+currentMaxCount));
	}

}
