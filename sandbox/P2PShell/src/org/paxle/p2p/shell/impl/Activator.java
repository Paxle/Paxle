package org.paxle.p2p.shell.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceListener;

public class Activator implements BundleActivator {
	private static ServiceListener listener = null;

	public void start(BundleContext context) throws Exception {			
		context.addServiceListener(listener = new GroupListener(context),GroupListener.FILTER);	
	}

	public void stop(BundleContext context) throws Exception {
		context.removeServiceListener(listener);
	}
}