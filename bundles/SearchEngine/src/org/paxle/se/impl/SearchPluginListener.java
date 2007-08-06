package org.paxle.se.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.se.ISearchPlugin;

public class SearchPluginListener implements ServiceListener {
	
	public static final String FILTER = String.format("(& (objectClass=%s) (%s=*))", ISearchPlugin.class.getName(), ISearchPlugin.PROP_MOD);
	
	private final SearchPluginManager manager;
	private final BundleContext context;
	
	public SearchPluginListener(SearchPluginManager manager, BundleContext context) {
		this.manager = manager;
		this.context = context;
	}
	
	public void serviceChanged(ServiceEvent event) {
		final ServiceReference ref = event.getServiceReference();
		final String mods = (String)ref.getProperty(ISearchPlugin.PROP_MOD);
		
		switch (event.getType()) {
			case ServiceEvent.REGISTERED:
				final ISearchPlugin splugin = (ISearchPlugin)this.context.getService(ref);
				this.manager.addSearchPlugin(splugin, mods);
				break;
			case ServiceEvent.UNREGISTERING:
				this.manager.removeSearchPlugin(mods);
				break;
			case ServiceEvent.MODIFIED:
				// TODO
				break;
		}
	}
}
