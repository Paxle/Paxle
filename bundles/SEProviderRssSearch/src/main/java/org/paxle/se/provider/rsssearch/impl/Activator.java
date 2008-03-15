package org.paxle.se.provider.rsssearch.impl;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.se.search.ISearchProvider;

public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */	
	public static BundleContext bc = null;

	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		ISearchProvider deliciousProvider = new RssSearchProvider("http://del.icio.us/rss/tag/%s");
		bc.registerService(ISearchProvider.class.getName(), deliciousProvider, new Hashtable<String,String>());
		ISearchProvider mrwongProvider = new RssSearchProvider("http://www.mister-wong.com/rss/tags/%s");
		bc.registerService(ISearchProvider.class.getName(), mrwongProvider, new Hashtable<String,String>());
        
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}
