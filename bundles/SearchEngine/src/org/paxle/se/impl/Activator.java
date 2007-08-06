package org.paxle.se.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceListener;

import org.paxle.se.ISearchEngine;
import org.paxle.se.index.impl.IndexListenerFactory;
import org.paxle.se.index.impl.SEWrapper;

public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	public static SEWrapper searchEngine = null;
	public static SearchPluginManager spManager = null;
	
	private static ServiceListener mlistener = null;
	private static ServiceListener slistener = null;
	private static ServiceListener wlistener = null;
	private static ServiceListener tlistener = null;
	private static SearchPluginListener plistener = null;
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		// create and publish new wrapper instance
		searchEngine = new SEWrapper();
		bc.registerService(ISearchEngine.class.getName(), searchEngine, new Hashtable<String,String>());
		
		// register index listener
		final IndexListenerFactory ilf = new IndexListenerFactory(searchEngine, bc);
		bc.addServiceListener(mlistener = ilf.getModifierListener(), IndexListenerFactory.MODIFIER_FILTER);
		bc.addServiceListener(slistener = ilf.getSearcherListener(), IndexListenerFactory.SEARCHER_FILTER);
		bc.addServiceListener(wlistener = ilf.getWriterListener(), IndexListenerFactory.WRITER_FILTER);
		bc.addServiceListener(tlistener = ilf.getTokenFactoryListener(), IndexListenerFactory.TFACTORY_FILTER);
		
		// register serach plugin listener
		spManager = new SearchPluginManager();
		plistener = new SearchPluginListener(spManager, bc);
		bc.addServiceListener(plistener, SearchPluginListener.FILTER);
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		searchEngine.close();
		bc.removeServiceListener(mlistener);
		bc.removeServiceListener(slistener);
		bc.removeServiceListener(wlistener);
		bc.removeServiceListener(tlistener);
		bc.removeServiceListener(plistener);
		mlistener = null;
		slistener = null;
		wlistener = null;
		tlistener = null;
		plistener = null;
		searchEngine = null;
		bc = null;
	}
}