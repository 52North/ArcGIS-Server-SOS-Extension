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
import java.util.List;
import java.util.Scanner;


public abstract class AbstractMetadataProvider implements OperationsMetadataProvider {

	private static final String OPERATION_NAME_KEY = "${operationName}";
	private static final String PARAMETERS_NAME_KEY = "${parameters}";
	private static final String GET_URL_KEY = "${getUrl}";
	private static final String templateFile = "template_operation.xml";
	private static String template;

	static {
		InputStream res = AbstractMetadataProvider.class.getResourceAsStream(templateFile);
		template = readResource(res);
	}
	
    public static String readResource(InputStream res) {
		Scanner sc = new Scanner(res);
		StringBuilder sb = new StringBuilder();
		while (sc.hasNext()) {
			sb.append(sc.nextLine());
			sb.append(System.getProperty("line.separator"));
		}
		sc.close();
		return sb.toString();
	}


	@Override
	public String createMarkup() {
		return template.replace(OPERATION_NAME_KEY, getOperationName()).
				replace(PARAMETERS_NAME_KEY, createParametersMarkup()).
				replace(GET_URL_KEY, getGetUrl());
	}

	private String createParametersMarkup() {
		List<Parameter> params = getParameters();
		
		if (params == null || params.size() == 0) return "";
		
		StringBuilder sb = new StringBuilder();
		for (Parameter parameter : params) {
			sb.append(parameter.createMarkup());
			sb.append(System.getProperty("line.separator"));
		}
		
		return sb.toString();
	}
	
	protected abstract String getGetUrl();

	protected abstract String getOperationName();

	protected abstract List<Parameter> getParameters();
	
}
