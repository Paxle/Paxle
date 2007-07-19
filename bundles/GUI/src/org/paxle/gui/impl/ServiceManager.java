package org.paxle.gui.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
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
	
	public Object[] getServices(String serviceName, String query) throws InvalidSyntaxException {
		ServiceReference[] references = this.context.getServiceReferences(serviceName,query);
		if (references == null) return null;
		
		Object[] services = new Object[references.length];
		for (int i=0; i < references.length; i++) {			
			services[i] = this.context.getService(references[i]);
		}
		
		return services;
	}
}
