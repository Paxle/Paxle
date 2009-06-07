package org.paxle.se.provider.tools.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.se.search.ISearchProvider;

public class Activator implements BundleActivator {
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext bc) throws Exception {
		ISearchProvider provider = new DictSearchProvider();
		bc.registerService(ISearchProvider.class.getName(), provider, new Hashtable<String,String>());
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		// cleanup
	}
}
