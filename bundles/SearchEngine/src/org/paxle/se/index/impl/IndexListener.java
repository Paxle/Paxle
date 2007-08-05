package org.paxle.se.index.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import org.paxle.se.index.IIndexModifier;
import org.paxle.se.index.IIndexSearcher;
import org.paxle.se.index.IIndexWriter;
import org.paxle.se.query.ITokenFactory;

public class IndexListener implements ServiceListener {
	
	public static final String FILTER = String.format("(| (objectClass=%s) (objectClass=%s) (objectClass=%s) (objectClass=%s) )",
			IIndexSearcher.class.getName(),
			IIndexModifier.class.getName(),
			IIndexWriter.class.getName(),
			ITokenFactory.class.getName());
	
	private final SEWrapper sewrapper;
	private final BundleContext context;
	
	public IndexListener(SEWrapper sewrapper, BundleContext context) {
		this.sewrapper = sewrapper;
		this.context = context;
	}
	
	public void serviceChanged(ServiceEvent event) {
		Object service = this.context.getService(event.getServiceReference());
		
		switch (event.getType()) {
			case ServiceEvent.REGISTERED:    register(service); break;		// Service installed
			case ServiceEvent.UNREGISTERING: unregister(service); break;	// Service uninstalled
			case ServiceEvent.MODIFIED:      break; 						// Service properties changed
		}
	}
	
	private void register(Object service) {
		if (service instanceof IIndexSearcher) {
			this.sewrapper.addISearcher((IIndexSearcher)service);
		} else if (service instanceof IIndexModifier) {
			this.sewrapper.addIModifier((IIndexModifier)service);
		} else if (service instanceof IIndexWriter) {
			this.sewrapper.addIWriter((IIndexWriter)service);
		} else if (service instanceof ITokenFactory) {
			this.sewrapper.addTokenFactory((ITokenFactory)service);
		}
	}
	
	private void unregister(Object service) {
		if (service instanceof IIndexSearcher) {
			this.sewrapper.removeISearcher((IIndexSearcher)service);
		} else if (service instanceof IIndexModifier) {
			this.sewrapper.removeIModifier((IIndexModifier)service);
		} else if (service instanceof IIndexWriter) {
			this.sewrapper.removeIWriter((IIndexWriter)service);
		} else if (service instanceof ITokenFactory) {
			this.sewrapper.removeTokenFactory((ITokenFactory)service);
		}
	}
}
