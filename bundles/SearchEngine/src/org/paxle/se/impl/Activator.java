package org.paxle.se.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.paxle.se.search.ISearchProviderManager;
import org.paxle.se.search.impl.SearchProviderListener;
import org.paxle.se.search.impl.SearchProviderManager;

public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	
	public static SearchProviderManager searchProviderManager = null;
	public static SearchProviderListener searchProviderListener = null;
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		searchProviderManager = new SearchProviderManager();
		bc.registerService(ISearchProviderManager.class.getName(), searchProviderManager, null);
		
		searchProviderListener = new SearchProviderListener(searchProviderManager, bc);
		bc.addServiceListener(searchProviderListener, SearchProviderListener.FILTER);
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		bc.removeServiceListener(searchProviderListener);
		searchProviderManager.shutdown();
		searchProviderListener = null;
		searchProviderManager = null;
		bc = null;
	}
}
