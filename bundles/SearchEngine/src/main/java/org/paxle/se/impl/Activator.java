
package org.paxle.se.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.paxle.core.prefs.IPropertiesStore;
import org.paxle.core.prefs.Properties;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.index.impl.FieldListener;
import org.paxle.se.index.impl.FieldManager;
import org.paxle.se.search.ISearchProviderManager;
import org.paxle.se.search.impl.SearchProviderListener;
import org.paxle.se.search.impl.SearchProviderManager;

public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	
	public static FieldListener fieldListener = null;
	public static FieldManager fieldManager = null;
	
	public static SearchProviderManager searchProviderManager = null;
	public static SearchProviderListener searchProviderListener = null;
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		/*
		 * Load the properties of this bundle
		 */
		Properties props = null;
		ServiceReference ref = bc.getServiceReference(IPropertiesStore.class.getName());
		if (ref != null) props = ((IPropertiesStore) bc.getService(ref)).getProperties(bc);				
		
		fieldManager = new FieldManager();
		bc.registerService(IFieldManager.class.getName(), fieldManager, null);
		fieldListener = new FieldListener(bc, fieldManager);
		bc.addServiceListener(fieldListener, FieldListener.FILTER);
		
		searchProviderManager = new SearchProviderManager(props);
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
