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
