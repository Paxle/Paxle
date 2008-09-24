package org.paxle.tools.dns.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.tools.dns.IAddressTool;

public class Activator implements BundleActivator {
	
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext context) throws Exception {		
		context.registerService(IAddressTool.class.getName(), new AddressTool(), null);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		// cleanup
	}
}