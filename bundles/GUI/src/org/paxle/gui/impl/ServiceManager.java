package org.paxle.gui.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ServiceManager {
	private BundleContext context = null;
	
	public ServiceManager(BundleContext context) {
		this.context = context;
	}

	public Object getService(String serviceName) {
		ServiceReference reference = this.context.getServiceReference(serviceName);
		return (reference == null) ? null : this.context.getService(reference);
	}
}
