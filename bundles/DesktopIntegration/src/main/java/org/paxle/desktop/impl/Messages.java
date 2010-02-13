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
