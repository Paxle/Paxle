package org.paxle.se.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.paxle.se.index.IFieldManager;
import org.paxle.se.index.impl.FieldListener;
import org.paxle.se.index.impl.FieldManager;
import org.paxle.se.query.PaxleQueryParser;
import org.paxle.se.query.impl.InternalSearchPluginListener;
import org.paxle.se.query.impl.InternalSearchPluginManager;
import org.paxle.se.search.ISearchProviderManager;
import org.paxle.se.search.impl.SearchProviderListener;
import org.paxle.se.search.impl.SearchProviderManager;

public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	
	public static FieldListener fieldListener = null;
	public static FieldManager fieldManager = null;
	
	public static InternalSearchPluginListener internalPluginListener = null;
	public static InternalSearchPluginManager internalPluginManager = null;
	
	public static SearchProviderManager searchProviderManager = null;
	public static SearchProviderListener searchProviderListener = null;
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		fieldManager = new FieldManager();
		bc.registerService(IFieldManager.class.getName(), fieldManager, null);
		fieldListener = new FieldListener(bc, fieldManager);
		bc.addServiceListener(fieldListener, FieldListener.FILTER);
		
		internalPluginManager = new InternalSearchPluginManager();
		PaxleQueryParser.manager = internalPluginManager;
		internalPluginListener = new InternalSearchPluginListener(internalPluginManager, bc);
		bc.addServiceListener(internalPluginListener, InternalSearchPluginListener.FILTER);
		
		searchProviderManager = new SearchProviderManager();
		bc.registerService(ISearchProviderManager.class.getName(), searchProviderManager, null);
		searchProviderListener = new SearchProviderListener(searchProviderManager, bc);
		bc.addServiceListener(searchProviderListener, SearchProviderListener.FILTER);
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		bc.removeServiceListener(internalPluginListener);
		bc.removeServiceListener(searchProviderListener);
		searchProviderManager.shutdown();
		internalPluginListener = null;
		internalPluginManager = null;
		searchProviderListener = null;
		searchProviderManager = null;
		bc = null;
	}
}
