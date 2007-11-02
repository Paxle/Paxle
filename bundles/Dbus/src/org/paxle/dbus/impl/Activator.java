package org.paxle.dbus.impl;


import org.freedesktop.dbus.DBusConnection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

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
	 * The connection to the dbus
	 */
	public static DBusConnection conn = null; 
	
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
		bc.addServiceListener(new CrawlerListener(bc,new NetworkManagerMonitor()),CrawlerListener.FILTER);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext arg0) throws Exception {
		// cleanup
		bc = null;
	}
}