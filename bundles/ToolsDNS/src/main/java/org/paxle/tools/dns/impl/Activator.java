package org.paxle.tools.dns.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.filter.IFilter;
import org.paxle.tools.dns.IAddressTool;

public class Activator implements BundleActivator {
	
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext context) throws Exception {		
		context.registerService(IAddressTool.class.getName(), new AddressTool(), null);
		
		Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[]{String.format("org.paxle.crawler.in; %s=%b,org.paxle.parser.out", IFilter.PROP_FILTER_TARGET_DISABLED,Boolean.TRUE)});
		context.registerService(IFilter.class.getName(), new DNSFilter(), filterProps);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		// cleanup
	}
}