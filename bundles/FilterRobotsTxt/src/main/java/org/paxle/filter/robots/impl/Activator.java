package org.paxle.filter.robots.impl;

import java.io.File;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.filter.IFilter;

public class Activator implements BundleActivator {
	private static String DB_PATH = "robots-db";
	
	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	public static BundleContext bc;	
	
	private static RobotsTxtManager robotsTxtManager = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception {
		bc = context;
		robotsTxtManager = new RobotsTxtManager(new File(DB_PATH));
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */		
		
		// register the protocol filter as service
		Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {"org.paxle.crawler.in","org.paxle.parser.out"});
		bc.registerService(IFilter.class.getName(), new RobotsTxtFilter(robotsTxtManager), filterProps);		
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
		robotsTxtManager = null;
		bc = null;
	}
}