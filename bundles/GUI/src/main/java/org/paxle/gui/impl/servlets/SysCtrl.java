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

package org.paxle.gui.impl.servlets;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.IServiceManager;

@Component(metatype=false, immediate=true)
@Service(Servlet.class)
@Properties({
	@Property(name="org.paxle.servlet.path", value="/sysctrl"),
	@Property(name="org.paxle.servlet.doUserAuth", boolValue=true),
	@Property(name="org.paxle.servlet.menu", value="%menu.administration/%menu.system/%menu.system.shutdown"), 
	@Property(name="org.paxle.servlet.menu.icon", value="/resources/images/shutdown_16.png")
})
public class SysCtrl extends ALayoutServlet {

	private static final long serialVersionUID = 1L;

	public static final String SHUTDOWN = "shutdown";
	public static final String RESTART = "restart";
	
	public Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {

		context.put("sdc", SHUTDOWN);
		context.put("rsc", RESTART);
		
		try {
			final IServiceManager manager = (IServiceManager) context.get(IServiceManager.SERVICE_MANAGER);
			int shutdownDelay = 5;

			if (request.getParameter(SHUTDOWN) != null) {
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) return null;
				else {
					manager.shutdownFrameworkDelayed(shutdownDelay);
					context.put("action", SHUTDOWN);
				}
			} else if (request.getParameter(RESTART) != null) {
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) return null;
				else {
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
