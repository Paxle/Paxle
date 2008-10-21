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

package org.paxle.crawler.ftp.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ftp.IFtpCrawler;

public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	public static BundleContext bc;		
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext context) throws Exception {
		bc = context;		
		
		/* ==========================================================
		 * Register Services
		 * ========================================================== */			
		
		// register this crawler as subcrawler
		FtpCrawler crawler = new FtpCrawler();
		Hashtable<String,Object> props = new Hashtable<String, Object>();
		props.put(ISubCrawler.PROP_PROTOCOL, crawler.getProtocols());	  
		bc.registerService(new String[]{ISubCrawler.class.getName(),IFtpCrawler.class.getName()}, crawler, props);
		
		// register URL handler service
		Hashtable<String,String[]> properties = new Hashtable<String,String[]>(1);
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[]{FtpStreamHandlerService.PROTOCOL});
        context.registerService(URLStreamHandlerService.class.getName(), new FtpStreamHandlerService(), properties);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		
		// cleanup
		bc = null;
	}
}