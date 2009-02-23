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
package org.paxle.api.jaxrs.monitorable.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.osgi.service.monitor.MonitorAdmin;
import org.paxle.api.jaxrs.monitorable.MonitorableResource;

/**
 * @scr.component 
 * @scr.service interface="java.lang.Object"
 * @scr.property name="javax.ws.rs" type="Boolean" value="true" private="true"
 */  
@Path("/monitorables")
public class Monitorables {
	
	/** @scr.reference */
	private MonitorAdmin monitorAdmin;
	
	@GET
	public List<MonitorableResource> getMonitorables(@QueryParam("monitorableId") List<String> monitorableIds) {
		if (monitorableIds == null || monitorableIds.size() == 0) {
			monitorableIds = Arrays.asList(this.monitorAdmin.getMonitorableNames());
		}
		
		// building a helper structure		
		List<MonitorableResource> monitorableMap = new ArrayList<MonitorableResource>();
		for (String monitorableName : monitorableIds) {
			monitorableMap.add(new MonitorableResource(this.monitorAdmin, monitorableName));
		}
		return monitorableMap;
	}
	
    @Path("{monitorableId}")
    public MonitorableResource getMonitorableVariable(@PathParam("monitorableId") String monitorableId) {
    	HashSet<String> monitorableIds = new HashSet<String>(Arrays.asList(this.monitorAdmin.getMonitorableNames()));
    	if (!monitorableIds.contains(monitorableId)) throw new WebApplicationException(Status.NOT_FOUND);
    	return new MonitorableResource(this.monitorAdmin, monitorableId);
	}  
}
