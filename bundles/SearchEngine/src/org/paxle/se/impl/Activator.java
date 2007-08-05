package org.paxle.se.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.paxle.se.ISearchEngine;
import org.paxle.se.index.impl.IndexListener;
import org.paxle.se.index.impl.SEWrapper;

public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	public static SEWrapper searchEngine = null;
	
	private static IndexListener ilistener = null;
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		// create and publish new wrapper instance
		searchEngine = new SEWrapper();
		bc.registerService(ISearchEngine.class.getName(), searchEngine, new Hashtable<String,String>());
		
		// register index listener
		ilistener = new IndexListener(searchEngine, bc);
		bc.addServiceListener(ilistener, IndexListener.FILTER);
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		searchEngine.close();
		bc.removeServiceListener(ilistener);
		searchEngine = null;
		ilistener = null;
		bc = null;
	}
}