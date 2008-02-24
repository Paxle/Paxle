
package org.paxle.se.provider.google.impl;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.se.search.ISearchProvider;

public class Activator implements BundleActivator {
	private Log logger = LogFactory.getLog(this.getClass());

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

		String googleKey = System.getProperty("org.paxle.se.provider.google.key");
		if (googleKey != null) {
			String serviceUrl = System.getProperty("org.paxle.se.provider.google.serviceURL");
			ISearchProvider provider = new GoogleSoapSearchProvider(googleKey,serviceUrl);
			bc.registerService(ISearchProvider.class.getName(), provider, new Hashtable<String,String>());
		} else {
			this.logger.warn("GoogleSearch provider will not be registered. Google-key is missing.");
		}
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}