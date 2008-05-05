package org.paxle.dbus.impl;


import java.util.ArrayList;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.dbus.IDbusService;
import org.paxle.dbus.impl.networkmonitor.NetworkManagerMonitor;
import org.paxle.dbus.impl.search.tracker.TrackerSearchProvider;
import org.paxle.se.search.ISearchProvider;

/**
 * required dynamic imports
 * <ul>
 * 	<li><tt>com.sun.security.auth.module</tt></li>
 * </ul>
 */
public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	public static BundleContext bc;	
		
	/**
	 * A list of services connected to dbus
	 */
	public ArrayList<IDbusService> services = new ArrayList<IDbusService>();
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception { 
		bc = context;
		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */		
		// registering a service listener to notice if a new sub-crawler was (un)deployed
		NetworkManagerMonitor nmm = new NetworkManagerMonitor(context);
		this.services.add(nmm);
		
		/* ==========================================================
		 * Register Services
		 * ========================================================== */	
		/*
		TrackerSearchProvider tsp = new TrackerSearchProvider();
		services.add(tsp);
		bc.registerService(ISearchProvider.class.getName(), tsp, new Hashtable<String,String>());
		*/
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext arg0) throws Exception {
		// cleanup
		bc = null;
		
		// loop through the registered services and disconnect
		// them from DBus
		for (IDbusService service : this.services) {
			service.terminate();
		}
	}
}