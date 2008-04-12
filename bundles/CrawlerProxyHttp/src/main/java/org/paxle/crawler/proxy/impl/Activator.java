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
	
	private ProxyDataProvider dataProvider = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception {
		// init logger
		this.logger = LogFactory.getLog(this.getClass());		
		
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
		this.proxy = new Proxy();
		
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
	}
}