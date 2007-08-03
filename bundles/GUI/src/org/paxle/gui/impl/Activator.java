package org.paxle.gui.impl;

import java.util.Hashtable;
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
		
		// getting a reference to the osgi http service
		ServiceReference sr = bc.getServiceReference(HttpService.class.getName());
		if(sr != null) {
			// getting the http service
			http = (HttpService)bc.getService(sr);
			if(http != null) {				
				Hashtable<String, String> props = new Hashtable<String, String>();
				props.put("org.apache.velocity.properties", "/resources/velocity.properties");
				props.put("bundle.location",context.getBundle().getLocation());
				
				// registering the servlet which will be accessible using 
				http.registerServlet("/status", new StatusView(manager), props, null);
                http.registerServlet("/crawler", new CrawlerView(manager), props, null);
                http.registerServlet("/bundle", new BundleView(manager), props, null);
                http.registerServlet("/log", new LogView(manager), props, null);
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