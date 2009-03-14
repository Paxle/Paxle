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
package org.paxle.crawler.ftp.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ftp.IFtpCrawler;

public class Activator implements BundleActivator {
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext bc) throws Exception {
		/* ==========================================================
		 * Register Services
		 * ========================================================== */			
		
		// register this crawler as subcrawler
		FtpCrawler crawler = new FtpCrawler();
		Hashtable<String,Object> props = new Hashtable<String, Object>();
		props.put(Constants.SERVICE_PID, IFtpCrawler.class.getName());
		props.put(ISubCrawler.PROP_PROTOCOL, FtpCrawler.PROTOCOLS);	  
		bc.registerService(new String[]{ISubCrawler.class.getName(),IFtpCrawler.class.getName()}, crawler, props);
		
		// register URL handler service
		Hashtable<String,String[]> properties = new Hashtable<String,String[]>(1);
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[]{FtpStreamHandlerService.PROTOCOL});
        bc.registerService(URLStreamHandlerService.class.getName(), new FtpStreamHandlerService(), properties);
        
		/*
		 * Create configuration if not available
		 */
		ServiceReference cmRef = bc.getServiceReference(ConfigurationAdmin.class.getName());
		if (cmRef != null) {
			ConfigurationAdmin cm = (ConfigurationAdmin) bc.getService(cmRef);
			Configuration config = cm.getConfiguration(IFtpCrawler.class.getName());
			if (config.getProperties() == null) {
				config.update(crawler.getDefaults());
			}
		}
		
		/* 
		 * Register as managed service
		 * 
		 * ATTENTION: it's important to specify a unique PID, otherwise
		 * the CM-Admin service does not recognize us.
		 */
		Hashtable<String,Object> msProps = new Hashtable<String, Object>();
		msProps.put(Constants.SERVICE_PID, IFtpCrawler.class.getName());
		bc.registerService(ManagedService.class.getName(), crawler, msProps);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {		
		// cleanup
	}
}