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
package org.paxle.dbus.impl;


import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.dbus.IDbusService;
import org.paxle.dbus.impl.networkmonitor.NetworkManagerMonitor;
import org.paxle.dbus.impl.search.tracker.TrackerSearchProvider;
import org.paxle.se.search.ISearchProvider;

/**
 * required dynamic imports
 * <ul>
 * 	<li><tt>com.sun.security.auth.module</tt></li>
 * </ul>
 */
public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	public static BundleContext bc;	
		
	/**
	 * A list of services connected to dbus
	 */
	public ArrayList<IDbusService> services = new ArrayList<IDbusService>();
	
	/**
	 * For logging
	 */
	private Log logger = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception { 
		bc = context;
		this.logger = LogFactory.getLog(this.getClass());
		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */		
		try {
			NetworkManagerMonitor nmm = new NetworkManagerMonitor(context);
			this.services.add(nmm);
		} catch (Exception e) {
			this.logger.warn(String.format("Unable to start NetworkManagerMonitor: %s", e.getMessage()));
		}
		
		/* ==========================================================
		 * Register Services
		 * ========================================================== */
		try {
			TrackerSearchProvider tsp = new TrackerSearchProvider();
			services.add(tsp);
			bc.registerService(ISearchProvider.class.getName(), tsp, new Hashtable<String,String>());
		} catch (Exception e) {
			this.logger.warn(String.format("Unable to start TrackerSearchProvider: %s", e.getMessage()));
		}
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext arg0) throws Exception {
		// cleanup
		bc = null;
		
		// loop through the registered services and disconnect
		// them from DBus
		for (IDbusService service : this.services) {
			service.terminate();
		}
	}
}