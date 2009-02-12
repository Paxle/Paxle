/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.api.json.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.JavaTypeMapper;
import org.osgi.service.monitor.MonitorAdmin;
import org.osgi.service.monitor.StatusVariable;

/**
 * @scr.component
 * @scr.property name="path" value="/json/monitorables"
 */
public class MonitorableServlet extends AJsonServlet {
	private static final long serialVersionUID = 1L;

	public static enum STATUSVAR_TYPE {
		Integer,
		Float,
		String,
		Boolean
	}
	
	/** @scr.reference */
	private MonitorAdmin monitorAdmin;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		
		// getting the names of all monitorables to serialize
		String monitorableNames[] = null;
		if (req.getParameter("monitorableId") != null) {
			monitorableNames = req.getParameterValues("monitorableId");
		} else {
			monitorableNames = this.monitorAdmin.getMonitorableNames();
		}
		
		// building a helper structure
		Map<String, Map<String, Map<String, ?>>> monitorableMap = new HashMap<String, Map<String, Map<String, ?>>>();
		if (monitorableNames != null) {
			for (String monitorableName : monitorableNames) {
				StatusVariable[] variables = this.monitorAdmin.getStatusVariables(monitorableName);
				if (variables != null) {
					Map<String, Map<String, ?>> variableMap = new HashMap<String, Map<String,?>>();
					monitorableMap.put(monitorableName, variableMap);
					
					for (StatusVariable variable : variables) {						
						Map<String, Object> valueMap = new HashMap<String, Object>();
						
						String variableID = variable.getID();
						STATUSVAR_TYPE varType = STATUSVAR_TYPE.values()[variable.getType()];
						
						valueMap.put("id", variableID);
						valueMap.put("type", varType.toString());
						valueMap.put("timestamp", new Long(variable.getTimeStamp().getTime()));
						valueMap.put("description", this.monitorAdmin.getDescription(monitorableName + "/" + variableID));
						valueMap.put("value", this.getValue(varType, variable));
						
						variableMap.put(variableID, valueMap);
					}
				}
			}
		}

		// writ json string out
		JavaTypeMapper jtm=new JavaTypeMapper();
		jtm.writeValue(resp.getOutputStream(), monitorableMap);
	}
	
	private Object getValue(STATUSVAR_TYPE varType, StatusVariable variable) {
		Object value = null;
		
		switch (varType) {
			case Integer:
				value = new Integer(variable.getInteger());
				break;
			
			case Float:
				value = new Float(variable.getFloat());
				break;
				
			case String:
				value = variable.getString();
				break;
				
			case Boolean:
				value = new Boolean(variable.getBoolean());
				break;
				
			default:
				break;
		}		
		return value;
	}
}
