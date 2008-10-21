/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
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

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.paxle.se.provider.rsssearch.impl.gui.ConfigServlet;

public class GuiListener implements BundleListener {
	private ServiceRegistration serviceReg = null;
	private final BundleContext bc;
	
	public GuiListener(BundleContext bc) {
		this.bc = bc;
	}
	
	public void bundleChanged(BundleEvent event) {
		if (event.getBundle().getHeaders().get(Constants.BUNDLE_SYMBOLICNAME).equals("org.paxle.gui")) {
			if (event.getType() == BundleEvent.STARTED) {
				/*
				 * Registering the servlet
				 */
				registerServlet();
			} else if (event.getType() == BundleEvent.STOPPED && this.serviceReg != null) {
				this.serviceReg.unregister();
				this.serviceReg = null;
			}
		}
	}

	public void registerServlet() {
		ConfigServlet servlet=new ConfigServlet();
		servlet.setBundleLocation(bc.getBundle().getEntry("/").toString());
		Hashtable<String, String> props = new Hashtable<String, String>();
		props.put("path", "/rsssearchconfig");
		props.put("menu", "RSS search sources");
		this.serviceReg = bc.registerService("javax.servlet.Servlet", servlet, props);
	}
}
