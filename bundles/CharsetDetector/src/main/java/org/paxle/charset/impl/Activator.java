package org.paxle.charset.impl;

import java.net.URL;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.charset.ICharsetDetector;

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
		 * Register Services provided by this bundle
		 * ========================================================== */
		// register the Charset-Detector as service
		URL mimeTypes = bc.getBundle().getEntry("/mimeTypes");
		bc.registerService(ICharsetDetector.class.getName(), new CharsetDetector(mimeTypes), null);			
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