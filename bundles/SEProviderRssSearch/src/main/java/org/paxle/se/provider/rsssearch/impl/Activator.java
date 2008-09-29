
package org.paxle.se.provider.rsssearch.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.paxle.se.search.ISearchProvider;

public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */	
	public static BundleContext bc = null;
	public static List<ServiceRegistration> providers;

	public void registerSearchers(ArrayList<String> urls){
		Iterator<ServiceRegistration> prov_it=Activator.providers.iterator();
		while(prov_it.hasNext()){
			prov_it.next().unregister();
		}
		Iterator<String> it=urls.iterator();
		while(it.hasNext()){
			providers.add(bc.registerService(ISearchProvider.class.getName(),
				new RssSearchProvider(it.next()), new Hashtable<String,String>())
			);
		}
	}
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext context) throws Exception {
		bc = context;
		providers=new ArrayList<ServiceRegistration>();
		ArrayList<String> urls=RssSearchProvider.getUrls();
		registerSearchers(urls);
		bc.addBundleListener(new GuiListener(bc));
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}
