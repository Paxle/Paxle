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
package org.paxle.gui.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

public class HttpServiceListener implements ServiceListener {
	/**
	 * A LDAP styled expression used for the service-listener
	 */
	public static final String FILTER = String.format("(%s=%s)",
			Constants.OBJECTCLASS, HttpService.class.getName());	

	/**
	 * A class to manage registered servlets
	 */
	private ServletManager servletManager = null;	
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */	
	private BundleContext context = null;	

	public HttpServiceListener(ServletManager servletManager, BundleContext context) throws InvalidSyntaxException {
		this.servletManager = servletManager;
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

		if (eventType == ServiceEvent.REGISTERED) {
			this.servletManager.setHttpService((HttpService) this.context.getService(reference));
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			this.servletManager.setHttpService(null);
		} else if (eventType == ServiceEvent.MODIFIED) {
			// ignore this
		}	
	}	
}
