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

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializable;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
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
		if (req.getParameter("debug") == null) {
			resp.setContentType("application/json");
		}
		
		// getting the names of all monitorables to serialize
		String monitorableNames[] = null;
		if (req.getParameter("monitorableId") != null) {
			monitorableNames = req.getParameterValues("monitorableId");
		} else {
			monitorableNames = this.monitorAdmin.getMonitorableNames();
		}
		
		// building a helper structure		
		Map<String, MonitorableProxy> monitorableMap = new HashMap<String, MonitorableProxy>();
		if (monitorableNames != null) {
			for (String monitorableName : monitorableNames) {
				monitorableMap.put(monitorableName, new MonitorableProxy(monitorableName));
			}
		}

		// write json string out
		ObjectMapper jtm = new ObjectMapper();
		jtm.writeValue(resp.getOutputStream(), monitorableMap);
	}
	
	class MonitorableProxy implements JsonSerializable {
		private String monitorableId;
		
		public MonitorableProxy(String monitorableId) {
			this.monitorableId = monitorableId;
		}
		
		public Map<String, StatusVariableProxy> getVariables() {
			Map<String, StatusVariableProxy> variablesMap = new HashMap<String, StatusVariableProxy>(); 
			
			String[] variables = monitorAdmin.getStatusVariableNames(this.monitorableId);
			if (variables != null) {
				for (String variableId : variables) {
					variablesMap.put(variableId, new StatusVariableProxy(this.monitorableId,variableId));
				}
			}
			
			return variablesMap;
		}

		public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
			provider.findValueSerializer(Map.class).serialize(this.getVariables(), jgen, provider);
		}
	}
	
	class StatusVariableProxy implements JsonSerializable{
		private final String monitorableId;
		private final String variableId;
		private final StatusVariable variable;
		
		public StatusVariableProxy(String monitorableId, String variableId) {
			this.monitorableId = monitorableId;
			this.variableId = variableId;
			this.variable = monitorAdmin.getStatusVariable(this.monitorableId + "/" + this.variableId);
		}

		public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
			STATUSVAR_TYPE varType = STATUSVAR_TYPE.values()[variable.getType()];
			
			jgen.writeStartObject();
			
			jgen.writeStringField("id", variable.getID());
			jgen.writeStringField("type", varType.toString());
			jgen.writeNumberField("timestamp", new Long(variable.getTimeStamp().getTime()));
			jgen.writeStringField("description", monitorAdmin.getDescription(this.monitorableId + "/" + this.variableId));			
			switch (varType) {
				case Integer:
					jgen.writeNumberField("value",new Integer(variable.getInteger()));
					break;
				
				case Float:
					jgen.writeNumberField("value",new Float(variable.getFloat()));
					break;
					
				case String:
					jgen.writeStringField("value",variable.getString());
					break;
					
				case Boolean:
					jgen.writeBooleanField("value",new Boolean(variable.getBoolean()));
					break;
					
				default:
					break;
			}
			
			jgen.writeEndObject();	
		}
	}
}
