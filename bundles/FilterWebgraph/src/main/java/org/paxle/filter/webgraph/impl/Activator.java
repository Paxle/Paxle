package org.paxle.filter.webgraph.impl;

import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.paxle.core.filter.IFilter;
import org.paxle.filter.webgraph.gui.GuiListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Activator implements BundleActivator {

	private Log logger = LogFactory.getLog(this.getClass());	
	
	public void start(BundleContext bc) throws Exception {
		this.logger.info("Starting bundle " + bc.getBundle().getSymbolicName());
		
		// creating filter
		GraphFilter myFilter = new GraphFilter();
		
		// specifying filter properties
		final Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {
							"org.paxle.parser.out; pos=0"
					});
		
		// registering filter
		bc.registerService(IFilter.class.getName(),myFilter, filterProps);
		GuiListener guilistener=new GuiListener(bc, myFilter);
		Bundle[] bundles=bc.getBundles();
		for(int i=0;i<bundles.length;i++){
			if(bundles[i].getHeaders().get(Constants.BUNDLE_SYMBOLICNAME).equals("org.paxle.gui"))
				guilistener.registerServlet();
		}
	}
	

	public void stop(BundleContext bc) throws Exception {
		this.logger.info("Stopping bundle " + bc.getBundle().getSymbolicName());
	}
}
