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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.core.IMWComponent;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.ServiceManager;

/**
 * @scr.component immediate="true" metatype="false"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="path" value="/queue"
 * @scr.property name="menu" value="Info/Queues"
 * @scr.property name="doUserAuth" value="false" type="Boolean"
 */
public class QueueView extends ALayoutServlet {
	
	private static final long serialVersionUID = 1L;
    
	@Override
	protected void fillContext(Context context, HttpServletRequest request) {
		try {
			ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);

			String queue = request.getParameter("queue");
			if (queue != null && queue.length() > 0) {
				Object[] services = manager.getServices("org.paxle.core.IMWComponent","(component.ID="+queue+")");
				if (services != null && services.length == 1 && services[0] instanceof IMWComponent) {
					List<?> activeJobs = ((IMWComponent<?>)services[0]).getActiveJobs();
					context.put("activeJobs", activeJobs);
					List<?> enqueuedJobs = ((IMWComponent<?>)services[0]).getEnqueuedJobs();
					context.put("enqueuedJobs", enqueuedJobs);
				}
			}

			if (request.getParameter("reload") != null) {
				// we don't want full html 
				context.put("layout", "plain.vm");
			}

		} catch (Exception e) {
			this.logger.error("Error",e);
		}
	}
	
	/**
	 * Choosing the template to use 
	 */
	@Override
	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
		String reload = request.getParameter("reload");
		if (reload == null) {
			return this.getTemplate("/resources/templates/QueueView.vm");
		} else if (reload.equals("queueList")) {
			return getTemplate("/resources/templates/QueueViewLists.vm");
		} else if (reload.equals("overview")) {
			return getTemplate("/resources/templates/QueueViewOverview.vm");
		}
		return null;
	}
}
