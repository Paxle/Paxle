package org.paxle.se.search.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.se.index.IIndexSearcher;
import org.paxle.se.search.ISearchProvider;

public class SearchProviderListener implements ServiceListener {
	
	public static final String FILTER = String.format("(| (%s=%s) (%s=%s))",
			Constants.OBJECTCLASS, ISearchProvider.class.getName(),
			Constants.OBJECTCLASS, IIndexSearcher.class.getName());
	
	private final SearchProviderManager searchProviderManager;
	private final BundleContext context;
	
	public SearchProviderListener(SearchProviderManager searchManager, BundleContext context) {
		this.searchProviderManager = searchManager;
		this.context = context;
		
		try {
			addRegisteredServices(ISearchProvider.class.getName());
			addRegisteredServices(IIndexSearcher.class.getName());
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private final void addRegisteredServices(String className) throws InvalidSyntaxException {
		final ServiceReference[] refs = context.getServiceReferences(className, null);
		if (refs != null)
			for (final ServiceReference ref : refs) {
				register(ref);
			}
	}
	
	private void register(ServiceReference ref) {
		// the (unique) service ID of the registered filter
		Long serviceID = (Long) ref.getProperty(Constants.SERVICE_ID);
		
		// register provider
		final ISearchProvider provider = (ISearchProvider)this.context.getService(ref);
		this.searchProviderManager.addProvider(serviceID, provider);
	}
	
	private void unregister(ServiceReference ref) {
		// the service ID of the registered filter
		Long serviceID = (Long) ref.getProperty(Constants.SERVICE_ID);
		
		// unregister provider
		this.searchProviderManager.removeProvider(serviceID);
	}

	public void serviceChanged(ServiceEvent event) {
		final ServiceReference ref = event.getServiceReference();
		
		switch (event.getType()) {
			case ServiceEvent.REGISTERED:
				register(ref);
				break;
			case ServiceEvent.UNREGISTERING:
				unregister(ref);
				break;
			case ServiceEvent.MODIFIED:
				// TODO
				break;
		}
	}
}
