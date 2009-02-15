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
package org.paxle.api.jaxrs.monitorable;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.osgi.service.monitor.MonitorAdmin;

public class MonitorableResource {
	private final MonitorAdmin monitorAdmin;
	private final String monitorableId;
	
	public MonitorableResource(MonitorAdmin monitorAdmin, String monitorableId) {
		this.monitorAdmin = monitorAdmin;
		this.monitorableId = monitorableId;
	}
	
	public String getId() {
		return monitorableId;
	}
	
	public List<StatusVariableResource> getVariables() {
		List<StatusVariableResource> variablesMap = new ArrayList<StatusVariableResource>(); 
		
		String[] variables = monitorAdmin.getStatusVariableNames(this.monitorableId);
		if (variables != null) {
			for (String variableId : variables) {
				variablesMap.add(new StatusVariableResource(this.monitorAdmin, this.monitorableId,variableId));
			}
		}
		
		return variablesMap;
	}
	
	@GET
	public MonitorableResource returnThis() {
		return this;
	}
	
	@Path("{variableId}")
	public StatusVariableResource getVariable(@PathParam("variableId") String variableId) {
		return new StatusVariableResource(this.monitorAdmin, this.monitorableId, variableId);
	}
}
