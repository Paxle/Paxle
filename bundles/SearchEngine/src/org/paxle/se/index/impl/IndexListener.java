package org.paxle.se.index.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

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
	
	private final Hashtable<Long,Class<?>> interfaceMapping = new Hashtable<Long,Class<?>>();
	
	private final SEWrapper sewrapper;
	private final BundleContext context;
	
	public IndexListener(SEWrapper sewrapper, BundleContext context) {
		this.sewrapper = sewrapper;
		this.context = context;
	}
	
	public void serviceChanged(ServiceEvent event) {
		final ServiceReference ref = event.getServiceReference();
		switch (event.getType()) {
			case ServiceEvent.REGISTERED:    register(this.context.getService(ref)); break;		// Service installed
			case ServiceEvent.UNREGISTERING: unregister(ref.getBundle().getBundleId()); break;	// Service uninstalled
			case ServiceEvent.MODIFIED:      break; 						// Service properties changed
		}
	}
	
	private void register(Object service) {
		if (service instanceof IIndexSearcher) {
			this.sewrapper.addISearcher((IIndexSearcher)service);
		}
		if (service instanceof IIndexModifier) {
			this.sewrapper.addIModifier((IIndexModifier)service);
		}
		if (service instanceof IIndexWriter) {
			this.sewrapper.addIWriter((IIndexWriter)service);
		}
		if (service instanceof ITokenFactory) {
			this.sewrapper.addTokenFactory((ITokenFactory)service);
		}
	}
	
	private void unregister(long id) {
		final Class<?> clazz = this.interfaceMapping.get(id);
		if (IIndexSearcher.class.isAssignableFrom(clazz)) {
			this.sewrapper.removeISearcher();
		}
		if (IIndexModifier.class.isAssignableFrom(clazz)) {
			this.sewrapper.removeIModifier();
		}
		if (IIndexWriter.class.isAssignableFrom(clazz)) {
			this.sewrapper.removeIWriter();
		}
		if (ITokenFactory.class.isAssignableFrom(clazz)) {
			this.sewrapper.removeTokenFactory();
		}
	}
}
