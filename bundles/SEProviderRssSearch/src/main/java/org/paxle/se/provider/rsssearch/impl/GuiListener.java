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
package org.paxle.se.provider.rsssearch.impl;

import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.paxle.se.provider.rsssearch.impl.gui.ConfigServlet;

public class GuiListener implements BundleListener {
	/**
	 * The symbolic-name of the paxle-gui bundle
	 * @see Constants#BUNDLE_SYMBOLICNAME
	 */
	private static final String GUI_SYMNAME = "org.paxle.gui";
	
	/**
	 * The bundle-context used for servlet-(un)-registration
	 * @see #registerServlet()
	 */
	private final BundleContext bc;
	
	/**
	 * A component to manager rss-search-provider
	 */
	private final RssSearchProviderManager pManager;
	
	/**
	 * {@link ServiceRegistration} object of our {@link ConfigServlet}
	 */
	private ServiceRegistration serviceReg = null;
	
	public GuiListener(BundleContext bc, RssSearchProviderManager pManager) {
		this.bc = bc;
		this.pManager = pManager;
		
		for (Bundle bundle : bc.getBundles()) {
			final String bundleSymolicName = (String) bundle.getHeaders().get(Constants.BUNDLE_SYMBOLICNAME);
			if(bundleSymolicName.equals(GUI_SYMNAME) && bundle.getState() == Bundle.ACTIVE) {
				this.registerServlet();
			}
		}
	}
	
	public void bundleChanged(BundleEvent event) {
		final Bundle bundle = event.getBundle();
		final String bundleSymolicName = (String) bundle.getHeaders().get(Constants.BUNDLE_SYMBOLICNAME);
		
		if (bundleSymolicName.equals(GUI_SYMNAME)) {
			if (event.getType() == BundleEvent.STARTED) {
				/*
				 * Registering the servlet
				 */
				this.registerServlet();
			} else if (event.getType() == BundleEvent.STOPPED && this.serviceReg != null) {
				/*
				 * Unregistering the servlet
				 */
				this.unregisterServlet();
			}
		}
	}

	public void registerServlet() {
		ConfigServlet servlet=new ConfigServlet(this.pManager);
		servlet.setBundleLocation(bc.getBundle().getEntry("/").toString());
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("path", "/rsssearchconfig");
		props.put("menu", "RSS search sources");
		props.put("doUserAuth", Boolean.TRUE);
		this.serviceReg = bc.registerService("javax.servlet.Servlet", servlet, props);
	}
	
	public void unregisterServlet() {
		this.serviceReg.unregister();
		this.serviceReg = null;
	}
}
