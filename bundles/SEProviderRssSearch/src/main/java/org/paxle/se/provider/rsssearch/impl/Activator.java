
package org.paxle.se.provider.rsssearch.impl;

import java.util.ArrayList;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

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
		Bundle[] bundles=bc.getBundles();
		GuiListener guilistener=new GuiListener(bc);
		for(int i=0;i<bundles.length;i++){
			if(bundles[i].getHeaders().get(Constants.BUNDLE_SYMBOLICNAME).equals("org.paxle.gui"))
				guilistener.registerServlet();
		}
		RssSearchProvider.providers=new ArrayList<ServiceRegistration>();
		ArrayList<String> urls=RssSearchProvider.getUrls();
		RssSearchProvider.registerSearchers(urls);
		bc.addBundleListener(guilistener);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}
