package org.paxle.crawler.http.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.crawler.ISubCrawler;

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
		
		// register this crawler as subcrawler
		HttpCrawler crawler = new HttpCrawler();
		Hashtable<String,String> props = new Hashtable<String, String>();
		props.put(ISubCrawler.PROP_PROTOCOL, crawler.getProtocol());	  
		bc.registerService(ISubCrawler.class.getName(), crawler, props);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		
		// cleanup
		bc = null;
	}
}