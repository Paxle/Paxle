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