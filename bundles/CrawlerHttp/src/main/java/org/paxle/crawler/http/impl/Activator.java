package org.paxle.crawler.http.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.http.IHttpCrawler;

public class Activator implements BundleActivator {

	/**
	 * The HTTP-Crawler
	 */
	private HttpCrawler crawler = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception {
		/* 
		 * Register this crawler as subcrawler
		 */
		this.crawler = new HttpCrawler();
		Hashtable<String,Object> props = new Hashtable<String, Object>();
		props.put(ISubCrawler.PROP_PROTOCOL, this.crawler.getProtocols());	  
		context.registerService(new String[]{ISubCrawler.class.getName(),IHttpCrawler.class.getName()}, this.crawler, props);
		
		/*
		 * Create configuration if not available
		 */
		ServiceReference cmRef = context.getServiceReference(ConfigurationAdmin.class.getName());
		if (cmRef != null) {
			ConfigurationAdmin cm = (ConfigurationAdmin) context.getService(cmRef);
			Configuration config = cm.getConfiguration(IHttpCrawler.class.getName());
			if (config.getProperties() == null) {
				config.update(this.crawler.getDefaults());
			}
		}
		
		/* 
		 * Register as managed service
		 * 
		 * ATTENTION: it's important to specify a unique PID, otherwise
		 * the CM-Admin service does not recognize us.
		 */
		Hashtable<String,Object> msProps = new Hashtable<String, Object>();
		msProps.put(Constants.SERVICE_PID, IHttpCrawler.class.getName());
		context.registerService(ManagedService.class.getName(), this.crawler, msProps);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		if (this.crawler != null) {
			this.crawler.cleanup();
		}
	}
}