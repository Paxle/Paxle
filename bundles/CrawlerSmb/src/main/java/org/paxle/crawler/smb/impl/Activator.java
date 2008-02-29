package org.paxle.crawler.smb.impl;

import java.util.Hashtable;

import jcifs.http.NetworkExplorer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
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
		propsCrawler.put(ISubCrawler.PROP_PROTOCOL, crawler.getProtocols());	  
		bc.registerService(new String[]{ISubCrawler.class.getName(),ISmbCrawler.class.getName()}, crawler, propsCrawler);
		
		// register URL handler service
		Hashtable<String,String[]> propsUrlHandler = new Hashtable<String,String[]>(1);
        propsUrlHandler.put(URLConstants.URL_HANDLER_PROTOCOL, new String[]{SmbStreamHandlerService.PROTOCOL});
        context.registerService(URLStreamHandlerService.class.getName(), new SmbStreamHandlerService(), propsUrlHandler);		

        // register browsing servlet
        Hashtable<String, String> propsServlet = new Hashtable<String, String>();
        propsServlet.put("path", "/smb/NetworkExplorer");
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