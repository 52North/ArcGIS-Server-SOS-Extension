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
package org.n52.util;

import java.io.IOException;
import java.util.Properties;

import org.n52.util.logging.Logger;

public class VersionInfo {

	private static final Logger LOGGER = Logger.getLogger(VersionInfo.class.getName());
	private Properties props;

	public VersionInfo() {
		this.props = new Properties();
		
		try {
			this.props.load(getClass().getResourceAsStream("/release_info.properties"));
		} catch (IOException e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(props.getProperty("name"));
		sb.append(System.getProperty("line.separator"));
		sb.append("Version: ");
		sb.append(props.getProperty("version"));
		sb.append(" (");
		sb.append(props.getProperty("date"));
		sb.append(")");
		sb.append(System.getProperty("line.separator"));
		sb.append(props.getProperty("sha"));
		
		return sb.toString();
	}
	
}
