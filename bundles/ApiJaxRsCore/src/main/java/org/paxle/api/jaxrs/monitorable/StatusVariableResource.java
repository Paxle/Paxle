/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.api.jaxrs.monitorable;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;

import org.osgi.service.monitor.MonitorAdmin;
import org.osgi.service.monitor.StatusVariable;

public class StatusVariableResource {
	public static enum STATUSVAR_TYPE {
		Integer,
		Float,
		String,
		Boolean
	}	
	
	private final MonitorAdmin monitorAdmin;
	private final String monitorableId;
	private final String variableId;
	private final StatusVariable variable;
	
	public StatusVariableResource(MonitorAdmin monitorAdmin, String monitorableId, String variableId) {
		this.monitorAdmin = monitorAdmin;
		this.monitorableId = monitorableId;
		this.variableId = variableId;
		this.variable = monitorAdmin.getStatusVariable(this.monitorableId + "/" + this.variableId);
	}
	
	@GET
	public StatusVariableResource returnThis() {
		return this;
	}
	
	@DELETE
	public void resetStatus() {
		this.monitorAdmin.resetStatusVariable(this.monitorableId + "/" + this.variableId);
	}
	
	public String getId() {
		return this.variable.getID();
	}
	
	public String getType() {
		STATUSVAR_TYPE varType = STATUSVAR_TYPE.values()[variable.getType()];
		return varType.toString();
	}
	
	public Long getTimestamp() {
		return variable.getTimeStamp().getTime();
	}
	
	public String getDescription() {
		return monitorAdmin.getDescription(this.monitorableId + "/" + this.variableId);
	}
	
	public Object getValue() {
		STATUSVAR_TYPE varType = STATUSVAR_TYPE.values()[variable.getType()];
		switch (varType) {
			case Integer: return Integer.valueOf(variable.getInteger());
			case Float:   return Float.valueOf(variable.getFloat());
			case String:  return variable.getString();
			case Boolean: return Boolean.valueOf(variable.getBoolean());
			default:      return null;
		}
	}
}
