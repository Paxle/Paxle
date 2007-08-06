package org.paxle.se.search.impl;

import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class SearchProviderListener implements ServiceListener {
	private SearchProviderManager searchProviderManager = null;
	
	public SearchProviderListener(SearchProviderManager searchManager) {
		this.searchProviderManager = searchManager;
	}

	public void serviceChanged(ServiceEvent event) {
		final ServiceReference ref = event.getServiceReference();
		
		switch (event.getType()) {
			case ServiceEvent.REGISTERED:
				// TODO: add provider to manager
				break;
			case ServiceEvent.UNREGISTERING:
				// remove provider from manager
				break;
			case ServiceEvent.MODIFIED:
				// TODO
				break;
		}
	}
}
