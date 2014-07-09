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
package org.n52.ows;

public class ResponseExceedsSizeLimitException extends ExceptionReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String CODE = "ResponseExceedsSizeLimit";
	private static final String TEXT = "The requested result set exceeds the response size limit of this "
			+ "service and thus cannot be delivered. Try defining a more strict filter.";

	public ResponseExceedsSizeLimitException() {
		super(CODE, TEXT);
	}
	
	public ResponseExceedsSizeLimitException(int currentMaxCount, int actualValue) {
		super(CODE, TEXT.concat(" The current maximum record count is set to "+currentMaxCount+"; the resulting value would have been "+actualValue));
	}

}
