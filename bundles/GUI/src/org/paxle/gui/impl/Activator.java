package org.paxle.gui.impl;

import java.util.Properties;

import org.apache.velocity.app.Velocity;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

public class Activator implements BundleActivator {

	private static BundleContext bc;
	private static HttpService http;
	private static ServiceManager manager = null;

	public void start(BundleContext context) throws Exception {
		bc = context;		
		manager = new ServiceManager(bc);
		
		// initialize Velocity
		// ATTENTION: it's important to do this before registering the servlet!
		// Otherwise velocity is initialized with default properties and will
		// not be able to load templates that are embedded in our jar file		
		Properties velocityConfig = new Properties();
		velocityConfig.load(Activator.class.getResourceAsStream("/resources/velocity.properties"));				
		Velocity.init(velocityConfig);				
		
		// getting a reference to the osgi http service
		ServiceReference sr = bc.getServiceReference(HttpService.class.getName());
		if(sr != null) {
			// getting the http service
			http = (HttpService)bc.getService(sr);
			if(http != null) {				
				// registering the servlet which will be accessible using 
				http.registerServlet("/status", new StatusView(manager), null, null);
			}
		}		
	}


	public void stop(BundleContext context) throws Exception {
		// unregister servlet
		http.unregister("/status");		
		
		// cleanup
		manager = null;
		http = null;
		bc = null;
	}
}