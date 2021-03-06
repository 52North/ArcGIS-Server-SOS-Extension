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
package org.n52.sos.handler.capabilities;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.n52.util.CommonUtilities;


public class Parameter {
	
	private static final String NAME_KEY = "${name}";
	private static final String VALUES_KEY = "${values}";
	private static String template;
	
	static {
		InputStream stream = Parameter.class.getResourceAsStream("template_parameter.xml");
		template = CommonUtilities.readResource(stream);
	}
	
	private String name;
	private List<String> values;
	
	
	public Parameter() {
	}
	
	public Parameter(String name) {
		this.name = name;
	}
	
	public Parameter(String name, List<String> values) {
		this(name);
		this.values = values;
	}

	public String createMarkup() {
		return template.replace(NAME_KEY, getName()).replace(VALUES_KEY, createValuesMarkup());
	}

	private String createValuesMarkup() {
		List<String> values = getValues();
		
		if (values == null || values.size() == 0) return "";
		
		StringBuilder sb = new StringBuilder();
		for (String string : values) {
			sb.append(string);
			sb.append(CommonUtilities.NEW_LINE_CHAR);
		}
		return sb.toString();
	}

	public List<String> getValues() {
		return values;
	}
	
	public void addValue(String val) {
		this.values.add(val);
	}

	public String getName() {
		return name;
	}



	public static class AnyValueParameter extends Parameter {

		private static final String ANY_VALUE = "<ows:Value>ANY</ows:Value>";

		public AnyValueParameter(String name) {
			super(name);
		}
		
		@Override
		public List<String> getValues() {
			return Collections.singletonList(ANY_VALUE);
		}
	}
}