/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

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
