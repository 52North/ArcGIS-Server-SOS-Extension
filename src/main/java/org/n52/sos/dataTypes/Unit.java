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

import java.io.IOException;

import org.n52.sos.db.impl.SubField;

import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.interop.AutomationException;

public class Unit {

	private int pkUnit;
	private String id;
	private String label;
	private String notation;
	private String definition;
	private String resource;
	
	private Unit() {
		
	}

	public Unit(int pkUnit, String id, String label, String notation,
			String definition, String resource) {
		this.pkUnit = pkUnit;
		this.id = id;
		this.label = label;
		this.notation = notation;
		this.definition = definition;
		this.resource = resource;
	}

	public int getPkUnit() {
		return pkUnit;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String getNotation() {
		return notation;
	}

	public String getDefinition() {
		return definition;
	}

	public String getResource() {
		return resource;
	}

	public static Unit fromRow(IRow row) throws NumberFormatException, AutomationException, IOException {
		Unit result = new Unit();
		IFields fields = row.getFields();
		result.pkUnit = Integer.parseInt(row.getValue(fields.findField(SubField.UNIT_PK_UNIT)).toString());
		result.id = row.getValue(fields.findField(SubField.UNIT_ID)).toString();
		result.label = row.getValue(fields.findField(SubField.UNIT_LABEL)).toString();
		result.notation = row.getValue(fields.findField(SubField.UNIT_NOTATION)).toString();
		result.definition = row.getValue(fields.findField(SubField.UNIT_DEFINITION)).toString();
		result.resource = row.getValue(fields.findField(SubField.UNIT_RESOURCE)).toString();
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Unit {pkUnit=");
		sb.append(pkUnit);
		sb.append(", id=");
		sb.append(id);
		sb.append(", label=");
		sb.append(label);
		sb.append(", notation=");
		sb.append(notation);
		sb.append("}");
		return sb.toString();
	}

}
