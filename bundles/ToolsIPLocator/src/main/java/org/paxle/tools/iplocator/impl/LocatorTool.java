/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.tools.iplocator.impl;

import java.io.InputStream;
import java.util.Locale;

import net.sf.javainetlocator.InetAddressLocator;
import net.sf.javainetlocator.InetAddressLocatorException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.paxle.tools.iplocator.ILocatorTool;

@Component(immediate=true, metatype=false)
@Service(ILocatorTool.class)
public class LocatorTool implements ILocatorTool {
	public Locale getLocale(String host) {
		try {
			return InetAddressLocator.getLocale(host);
		} catch (InetAddressLocatorException e) {
			return null;
		}
	}
	
	public InputStream getIcon(String hostNameIp) {
		// trying to determine the locale
		Locale locale = getLocale(hostNameIp);
		if (locale == null) return null;
		return this.getIcon(locale);
	}
	
	public  InputStream getIcon(Locale locale) {
		return LocatorTool.class.getResourceAsStream("/resources/flags/" + locale.getCountry().toLowerCase() + ".png");
	}
}
