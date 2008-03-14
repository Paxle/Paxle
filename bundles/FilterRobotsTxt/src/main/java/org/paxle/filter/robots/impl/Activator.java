package org.paxle.filter.robots.impl;

import java.io.File;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.filter.IFilter;

public class Activator implements BundleActivator {
	private static String DB_PATH = "robots-db";	
	
	private RobotsTxtManager robotsTxtManager = null;
	private Thread robotsTxtCleanupThread = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext bc) throws Exception {
		robotsTxtManager = new RobotsTxtManager(new File(DB_PATH));
		robotsTxtCleanupThread = new RobotsTxtCleanupThread(new File(DB_PATH), 10); 
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */		
		
		// register the protocol filter as service
		Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {"org.paxle.crawler.in","org.paxle.parser.out; pos=70;"});
		bc.registerService(IFilter.class.getName(), new RobotsTxtFilter(robotsTxtManager), filterProps);		
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
		robotsTxtManager.terminate();
		robotsTxtManager = null;
		robotsTxtCleanupThread.interrupt();
		robotsTxtCleanupThread = null;
	}
}