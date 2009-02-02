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
package org.paxle.core.norm.impl;

import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

public class URLStreamHandlerListener implements ServiceListener {
	
	public static final String FILTER = String.format("(%s=%s)",
			Constants.OBJECTCLASS,
			URLStreamHandlerService.class.getName());
	
	private final BundleContext bc;
	private final Map<String,Integer> protocolMap;
	
	public URLStreamHandlerListener(final BundleContext bc, final Map<String,Integer> protocolMap) {
		this.bc = bc;
		this.protocolMap = protocolMap;
		
		final ServiceReference[] refs;
		try {
			refs = bc.getAllServiceReferences(URLStreamHandlerService.class.getName(), null);
			if (refs == null)
				return;
		} catch (InvalidSyntaxException e) { e.printStackTrace(); return; }
		
		for (final ServiceReference ref : refs)
			addProtocols(ref);
	}
	
	private void addProtocols(final ServiceReference ref) {
		final URLStreamHandlerService streamHandler = (URLStreamHandlerService)bc.getService(ref);
		if (streamHandler == null)
			return;
		
		final int defaultPortInt = streamHandler.getDefaultPort();
		if (defaultPortInt < 0 || defaultPortInt > 65535)
			return;
		final Integer defaultPort = Integer.valueOf(defaultPortInt);
		
		final Object protVal = ref.getProperty(URLConstants.URL_HANDLER_PROTOCOL);
		if (protVal instanceof String) {
			final String protocol = (String)protVal;
			protocolMap.put(protocol, defaultPort);
			
		} else if (protVal instanceof String[]) {
			final String[] protocols = (String[])protVal;
			for (final String protocol : protocols)
				protocolMap.put(protocol, defaultPort);
		}
	}
	
	private void removeProtocols(final ServiceReference ref) {
		final Object protVal = ref.getProperty(URLConstants.URL_HANDLER_PROTOCOL);
		if (protVal instanceof String) {
			protocolMap.remove(protVal);
		} else if (protVal instanceof String[]) {
			final String[] protocols = (String[])protVal;
			for (final String protocol : protocols)
				protocolMap.remove(protocol);
		}
	}
	
	public void serviceChanged(ServiceEvent event) {
		final ServiceReference ref = event.getServiceReference();
		switch (event.getType()) {
			case ServiceEvent.REGISTERED:
				addProtocols(ref);
				break;
			case ServiceEvent.UNREGISTERING:
				removeProtocols(ref);
				break;
			case ServiceEvent.MODIFIED:
				removeProtocols(ref);
				addProtocols(ref);
				break;
		}
	}
}
