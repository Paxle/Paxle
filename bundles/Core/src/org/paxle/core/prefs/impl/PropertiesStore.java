package org.paxle.core.prefs.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.paxle.core.prefs.IPropertiesStore;
import org.paxle.core.prefs.Properties;

public class PropertiesStore implements IPropertiesStore {

	public Properties getProperties(BundleContext bundleContext) {
		ServiceReference ref = bundleContext.getServiceReference(PreferencesService.class.getName());
		if (ref != null) {
			// get OSGi preferences service
			PreferencesService prefsService = (PreferencesService) bundleContext.getService(ref);
			
			// get system preferences
			// XXX should we return the user-prefs here?
			Preferences prefs = prefsService.getSystemPreferences();
			
			// wrap preferences into properties class
			return new Properties(prefs);
		}
		
		// TODO: return a default properties class instead
		return null;
	}
}
