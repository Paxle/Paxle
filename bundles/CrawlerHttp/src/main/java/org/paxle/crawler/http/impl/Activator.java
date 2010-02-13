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

package org.paxle.crawler.http.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.paxle.core.prefs.IPropertiesStore;
import org.paxle.core.prefs.Properties;
import org.paxle.crawler.ICrawlerContextAware;
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
		 * Load the properties of this bundle
		 */
		Properties properties = null;
		final ServiceReference ref = context.getServiceReference(IPropertiesStore.class.getName());
		if (ref != null)
			properties = ((IPropertiesStore)context.getService(ref)).getProperties(context);
		
		/* 
		 * Register this crawler as subcrawler
		 */
		this.crawler = new HttpCrawler(properties);
		Hashtable<String,Object> props = new Hashtable<String, Object>();
		props.put(Constants.SERVICE_PID, IHttpCrawler.class.getName());
		props.put(ISubCrawler.PROP_PROTOCOL, HttpCrawler.PROTOCOLS);	  
		context.registerService(new String[]{
				ISubCrawler.class.getName(),
				IHttpCrawler.class.getName(),
				ICrawlerContextAware.class.getName()}, this.crawler, props);
		
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
			this.crawler.saveProperties();
		}
	}
}