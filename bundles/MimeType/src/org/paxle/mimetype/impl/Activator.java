package org.paxle.mimetype.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.mimetype.IMimeTypeDetector;

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
		// register the SubParser-Manager as service
		bc.registerService(IMimeTypeDetector.class.getName(), new MimeTypeDetector(), null);			
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