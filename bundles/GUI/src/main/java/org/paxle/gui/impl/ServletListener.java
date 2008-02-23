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

		if (eventType == ServiceEvent.REGISTERED) {
			// getting a reference to the servlet
			Servlet servlet = (Servlet) this.context.getService(reference);
			
			// register servlet
			this.servletManager.addServlet(path, servlet);
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// unregister servlet
			this.servletManager.removeServlet(path);
		} else if (eventType == ServiceEvent.MODIFIED) {
		}	
	}
}