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
package org.paxle.gui.impl.servlets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.service.monitor.MonitorAdmin;
import org.osgi.service.monitor.MonitoringJob;
import org.paxle.gui.ALayoutServlet;

public class MonitorableView extends ALayoutServlet {	
	private static final long serialVersionUID = 1L;

	@Override
	public Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {
		context.put("monitorableView", this);
		
		String format = request.getParameter("format");
		if (format != null && format.equals("json")) {
			context.put("layout", "plain.vm");
			response.setContentType("application/json");
			return this.getTemplate("/resources/templates/MonitorableViewJson.vm");
		} else {
			return this.getTemplate("/resources/templates/MonitorableView.vm");
		}
	}   
	
	/**
	 * We need to do this because the {@link MonitorAdmin} service is implemented
	 * using a {@link ServiceFactory} and therefore just returns the job-list for
	 * the bundle requested the service. 
	 * 
	 * @return
	 */
	public Map<Bundle,List<MonitoringJob>> getRunningJobs() {
		HashMap<Bundle,List<MonitoringJob>> jobs = new HashMap<Bundle, List<MonitoringJob>>(); 
		
		for(Bundle bundle : this.getServiceManager().getBundles()) {
			BundleContext context = bundle.getBundleContext();
			if (context == null) continue;
			
			ServiceReference ref = null;
			try {
				ref = context.getServiceReference(MonitorAdmin.class.getName());
				if (ref == null) continue;
				
				MonitorAdmin ma = (MonitorAdmin) context.getService(ref);
				if (ma == null) continue;
				
				MonitoringJob[] runningJobs = ma.getRunningJobs();
				if (runningJobs != null && runningJobs.length > 0) {
					jobs.put(bundle, Arrays.asList(runningJobs));
				}
			} finally {
				if (ref != null) context.ungetService(ref);
			}
		}
		
		return jobs;
	}
	
	public String getMonitorableID(String fullqVariableName) {
		return fullqVariableName.split("/")[0];
	}
	
	public String getVariableID(String fullqVariableName) {
		return fullqVariableName.split("/")[1];
	}
}
