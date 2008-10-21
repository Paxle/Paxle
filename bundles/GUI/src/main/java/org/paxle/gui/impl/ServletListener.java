/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.gui.impl;

import javax.servlet.Servlet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class ServletListener implements ServiceListener {
	/**
	 * A LDAP styled expression used for the service-listener
	 */
	public static final String FILTER = String.format("(%s=%s)",
			Constants.OBJECTCLASS, Servlet.class.getName());	

	/**
	 * A class to manage registered servlets
	 */
	private ServletManager servletManager = null;
	
	/**
	 * A class to manage menu entries
	 */
	private MenuManager menuManager = null;
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */	
	private BundleContext context = null;	

	public ServletListener(ServletManager servletManager, MenuManager menuManager, BundleContext context) throws InvalidSyntaxException {
		this.servletManager = servletManager;
		this.menuManager = menuManager;
		this.context = context;
		
		ServiceReference[] services = context.getServiceReferences(null,FILTER);
		if (services != null) for (ServiceReference service : services) serviceChanged(service, ServiceEvent.REGISTERED);	
	}

	/**
	 * @see ServiceListener#serviceChanged(ServiceEvent)
	 */
	public void serviceChanged(ServiceEvent event) {
		// getting the service reference
		ServiceReference reference = event.getServiceReference();
		this.serviceChanged(reference, event.getType());
	}	

	private void serviceChanged(ServiceReference reference, int eventType) {
		if (reference == null) return;		

		String path = (String)reference.getProperty("path");
		String name = (String)reference.getProperty("name");
		String menu = (String)reference.getProperty("menu");

		if (eventType == ServiceEvent.REGISTERED) {
			// getting a reference to the servlet
			Servlet servlet = (Servlet) this.context.getService(reference);
			
			// register servlet
			this.servletManager.addServlet(path, servlet);
			
			// registering menu
			if (menu != null && menu.length() > 0) {
				this.menuManager.addItem(path, menu);
			}
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// unregister servlet
			this.servletManager.removeServlet(path);
			
			// remove menu
			if (menu != null && menu.length() > 0) {
				this.menuManager.removeItem(path);
			}
			
		} else if (eventType == ServiceEvent.MODIFIED) {
		}	
	}
}
