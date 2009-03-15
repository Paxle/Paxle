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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IServiceManager;

/**
 * @scr.component immediate="true" metatype="false"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="path" value="/sysctrl"
 * @scr.property name="menu" value="%menu.administration/%menu.system/Shutdown"
 * @scr.property name="doUserAuth" value="true" type="Boolean"
 */
public class SysCtrl extends ALayoutServlet {

	private static final long serialVersionUID = 1L;

	public static final String SHUTDOWN = "shutdown";
	public static final String RESTART = "restart";
	
	public Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {

		context.put("sdc", SHUTDOWN);
		context.put("rsc", RESTART);
		
		try {
			final IServiceManager manager = (IServiceManager) context.get(SERVICE_MANAGER);
			int shutdownDelay = 5;

			if (request.getParameter(SHUTDOWN) != null) {
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) {
					response.sendRedirect("/login");
				} else {
					manager.shutdownFrameworkDelayed(shutdownDelay);
					context.put("action", SHUTDOWN);
				}
			} else if (request.getParameter(RESTART) != null) {
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) {
					response.sendRedirect("/login");
				} else {
					manager.restartFrameworkDelayed(shutdownDelay);
					context.put("action", RESTART);
				}
			}
		} catch (Throwable e) {
			this.logger.error(e);
		}	

		return this.getTemplate(null, null);
	}

	/**
	 * Choosing the template to use 
	 */
	@Override
	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
		return this.getTemplate("/resources/templates/SysCtrl.vm");
	}
}
