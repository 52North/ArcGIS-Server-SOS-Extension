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
package org.n52.sos.handler.capabilities;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;


public class Parameter {
	
	private static final String NAME_KEY = "${name}";
	private static final String VALUES_KEY = "${values}";
	private static String template;
	
	static {
		InputStream stream = Parameter.class.getResourceAsStream("template_parameter.xml");
		template = readResource(stream);
	}
	
	private static String readResource(InputStream res) {
		Scanner sc = new Scanner(res);
		StringBuilder sb = new StringBuilder();
		while (sc.hasNext()) {
			sb.append(sc.nextLine());
			sb.append(System.getProperty("line.separator"));
		}
		sc.close();
		return sb.toString();
	}
	
	private String name;
	private List<String> values;
	
	public Parameter() {
	}
	
	public Parameter(String name) {
		this.name = name;
	}
	
	
	public Parameter(String name, List<String> vals) {
		this(name);
		this.values = vals;
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
			sb.append(System.getProperty("line.separator"));
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