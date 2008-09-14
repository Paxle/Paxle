
package org.paxle.desktop.impl;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	
	private static final String         PREFIX          = "OSGI-INF/l10n/";					//$NON-NLS-1$
	private static final String			BUNDLE_NAME		= "messages";						//$NON-NLS-1$
																							
	private static final ResourceBundle	RESOURCE_BUNDLE	= ResourceBundle.getBundle(PREFIX + BUNDLE_NAME);
	
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	private Messages() {}
}
