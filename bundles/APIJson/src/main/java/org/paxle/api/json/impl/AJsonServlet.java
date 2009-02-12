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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.useradmin.UserAdmin;

/**
 * @scr.component abstract="true"
 * @scr.property name="doUserAuth" type="Boolean" value="false"
 */
public class AJsonServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/**
	 * The OSGi HTTP-service required to register our {@link HttpServlet servlets} 
	 * @scr.reference 
	 */
	protected HttpService httpService;
	
	/**
	 * The OSGi UserAdmin service, required for http-authentication if required by the servlet
	 * @scr.reference cardinality="0..1"
	 */
	protected UserAdmin userService;
	
	/**
	 * The context of this component
	 */
	protected ComponentContext ctx;
	
	protected void activate(ComponentContext ctx) throws ServletException, NamespaceException {
		this.ctx = ctx;
		
		String servletPath = (String)ctx.getProperties().get("path");
		Boolean authRequired = (Boolean) ctx.getProperties().get("doUserAuth");		
		
		if (servletPath != null) {
			this.httpService.registerServlet(
					// the http-path to access the servlet
					servletPath, 
					// the servlet itself
					this, 
					// config properties
					null,
					// authenticator
					(authRequired==null || !authRequired.booleanValue())?null:new HttpContextAuth(this.userService)
			);
		}
	}
	
	protected void deactivate(ComponentContext ctx) {
		this.ctx = ctx;
		
		String servletPath = (String)ctx.getProperties().get("path");
		if (servletPath != null) {
			this.httpService.unregister(servletPath);
		}
	}
}
