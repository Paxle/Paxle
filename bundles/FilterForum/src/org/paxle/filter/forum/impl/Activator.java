package org.paxle.filter.forum.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.filter.IFilter;

public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	public static BundleContext bc;	

	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception {
		bc = context;
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */		
		
		// register the protocol filter as service
//		Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
//		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {"org.paxle.parser.out"});
//		bc.registerService(IFilter.class.getName(), new ForumFilter(), filterProps);			
	}

	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}