package org.paxle.gui.impl;

import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

public class Activator implements BundleActivator {

	private static BundleContext bc;
	private static HttpService http;
	private static ServiceManager manager = null;
	private static VelocityEngine velocity = null;

	public void start(BundleContext context) throws Exception {
		Class class1 = Class.forName("sun.reflect.MethodAccessorImpl");
		
		bc = context;		
		manager = new ServiceManager(bc);
		
		// initialize Velocity
		// ATTENTION: it's important to do this before registering the servlet!
		// Otherwise velocity is initialized with default properties and will
		// not be able to load templates that are embedded in our jar file		
		Properties velocityConfig = new Properties();
		velocityConfig.load(Activator.class.getResourceAsStream("/resources/velocity.properties"));
		velocityConfig.setProperty("jar.resource.loader.path", "jar:" + context.getBundle().getLocation());		
		velocity = new VelocityEngine();
		velocity.init(velocityConfig);		
		
		// getting a reference to the osgi http service
		ServiceReference sr = bc.getServiceReference(HttpService.class.getName());
		if(sr != null) {
			// getting the http service
			http = (HttpService)bc.getService(sr);
			if(http != null) {				
				// registering the servlet which will be accessible using 
				http.registerServlet("/status", new StatusView(manager, velocity), null, null);
                http.registerServlet("/crawler", new CrawlerView(manager, velocity), null, null);
                http.registerServlet("/bundle", new BundleView(manager, velocity), null, null);
                http.registerServlet("/log", new LogView(manager, velocity), null, null);
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
		velocity = null;
	}
}