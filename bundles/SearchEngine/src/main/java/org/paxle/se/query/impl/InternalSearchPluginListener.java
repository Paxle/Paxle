package org.paxle.se.query.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.se.query.IModTokenFactory;

public class InternalSearchPluginListener implements ServiceListener {
	
	public static final String FILTER = String.format("(& (%s=%s) (%s=*))",
			Constants.OBJECTCLASS, IModTokenFactory.class.getName(),
			IModTokenFactory.PROP_SUPPORTED_TOKENS);
	
	private final BundleContext context;
	private final InternalSearchPluginManager manager;
	
	public InternalSearchPluginListener(InternalSearchPluginManager manager, BundleContext context) {
		this.manager = manager;
		this.context = context;
		
		try {
			final ServiceReference[] refs = context.getServiceReferences(IModTokenFactory.class.getName(), "(" + IModTokenFactory.PROP_SUPPORTED_TOKENS + "=*)");
			if (refs != null)
				for (final ServiceReference ref : refs)
					register(ref);
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private void register(ServiceReference ref) {
		final String tokens = (String)ref.getProperty(IModTokenFactory.PROP_SUPPORTED_TOKENS);
		final IModTokenFactory plugin = (IModTokenFactory)this.context.getService(ref);
		this.manager.addPlugin(tokens, plugin);
	}
	
	private void unregister(ServiceReference ref) {
		final String tokens = (String)ref.getProperty(IModTokenFactory.PROP_SUPPORTED_TOKENS);
		this.manager.removePlugin(tokens);
	}
	
	public void serviceChanged(ServiceEvent event) {
		final ServiceReference ref = event.getServiceReference();
		
		switch (event.getType()) {
			case ServiceEvent.REGISTERED: register(ref); break;
			case ServiceEvent.UNREGISTERING: unregister(ref); break;
			case ServiceEvent.MODIFIED: break; // TODO
		}
	}
}
