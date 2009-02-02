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
package org.paxle.crawler.proxy.impl;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.prefs.IPropertiesStore;
import org.paxle.core.prefs.Properties;
import org.paxle.core.queue.ICommandTracker;
import org.paxle.crawler.proxy.IHttpProxy;

public class Activator implements BundleActivator {
	
	/**
	 * A wrapper around the {@link Proxy} component
	 */
	private Proxy proxy = null;
	
	/**
	 * Logger
	 */
	private Log logger = null;	
	
	/**
	 * A special {@link IDataProvider} which pipes the intercepted {@link org.xsocket.connection.http.HttpResponse HTTP-response-data}
	 * into the {@link org.paxle.parser.ISubParser parser}-input-queue if the 
	 * {@link org.xsocket.connection.http.HttpRequest HTTP-request-} and {@link org.xsocket.connection.http.HttpResponse -response-data}
	 * not contain personal user-data
	 */
	private ProxyDataProvider dataProvider = null;
	
	/**
	 * OSGi Service to tracke the {@link UserAdmin} service.
	 */
	private ServiceTracker userAgentTracker = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception {
		// init logger
		this.logger = LogFactory.getLog(this.getClass());		
		
		// init useradmin tracker
		this.userAgentTracker = new ServiceTracker(context, UserAdmin.class.getName(), null);
		this.userAgentTracker.open();
		
		// Load the preferences of this bundle
		Properties providerPrefs = null;
		ServiceReference ref = context.getServiceReference(IPropertiesStore.class.getName());
		if (ref != null) providerPrefs = ((IPropertiesStore) context.getService(ref)).getProperties(context);
		
        // getting the command-tracker
        ServiceReference commandTrackerRef = context.getServiceReference(ICommandTracker.class.getName());
        ICommandTracker commandTracker = (commandTrackerRef == null) ? null :  (ICommandTracker) context.getService(commandTrackerRef);
        if (commandTracker == null) {
        	this.logger.warn("No CommandTracker-service found. Command-tracking will not work.");
        }
		
		// init data provider
		this.dataProvider = new ProxyDataProvider(providerPrefs, commandTracker);
		final Hashtable<String,String> providerProps = new Hashtable<String,String>();
		providerProps.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.parser.sink");		
		context.registerService(new String[]{IDataProvider.class.getName()}, this.dataProvider, providerProps);
		
		// init proxy
		this.proxy = new Proxy(this.userAgentTracker);
		
		/*
		 * Create configuration if not available
		 */
		ServiceReference cmRef = context.getServiceReference(ConfigurationAdmin.class.getName());
		if (cmRef != null) {
			ConfigurationAdmin cm = (ConfigurationAdmin) context.getService(cmRef);
			Configuration config = cm.getConfiguration(IHttpProxy.class.getName());
			if (config.getProperties() == null) {
				config.update(this.proxy.getDefaults());
			}
		}
		
		/* 
		 * Register as managed service
		 * 
		 * ATTENTION: it's important to specify a unique PID, otherwise
		 * the CM-Admin service does not recognize us.
		 */
		Hashtable<String,Object> msProps = new Hashtable<String, Object>();
		msProps.put(Constants.SERVICE_PID, IHttpProxy.class.getName());
		context.registerService(ManagedService.class.getName(), this.proxy, msProps);	
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		this.proxy.terminate();
		this.proxy = null;
		
		this.dataProvider.terminate();
		this.dataProvider = null;
		
		this.userAgentTracker.close();
		this.userAgentTracker = null;
	}
}