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
package org.paxle.crawler.smb.impl;

import java.util.Hashtable;

import jcifs.http.NetworkExplorer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.smb.ISmbCrawler;

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
		SmbCrawler crawler = new SmbCrawler();
		Hashtable<String,Object> propsCrawler = new Hashtable<String, Object>();
		propsCrawler.put(Constants.SERVICE_PID, ISmbCrawler.class.getName());
		propsCrawler.put(ISubCrawler.PROP_PROTOCOL, crawler.getProtocols());	  
		bc.registerService(new String[]{ISubCrawler.class.getName(),ISmbCrawler.class.getName()}, crawler, propsCrawler);
		
		// register URL handler service
		Hashtable<String,String[]> propsUrlHandler = new Hashtable<String,String[]>(1);
        propsUrlHandler.put(URLConstants.URL_HANDLER_PROTOCOL, new String[]{SmbStreamHandlerService.PROTOCOL});
        context.registerService(URLStreamHandlerService.class.getName(), new SmbStreamHandlerService(), propsUrlHandler);		

        // register browsing servlet
        Hashtable<String, Object> propsServlet = new Hashtable<String, Object>();
        propsServlet.put("path", "/smb/NetworkExplorer");
        propsServlet.put("doUserAuth", Boolean.TRUE);
        bc.registerService("javax.servlet.Servlet", new NetworkExplorer(), propsServlet);
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