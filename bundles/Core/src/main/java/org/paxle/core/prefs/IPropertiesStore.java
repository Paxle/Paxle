package org.paxle.core.prefs;

import org.osgi.framework.BundleContext;

public interface IPropertiesStore {
	public Properties getProperties(BundleContext bundleContext);
}
