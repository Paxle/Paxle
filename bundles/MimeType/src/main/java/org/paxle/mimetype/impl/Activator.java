package org.paxle.mimetype.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.mimetype.IMimeTypeDetector;

public class Activator implements BundleActivator {
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext bc) throws Exception {
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */		
		// register the SubParser-Manager as service
		MimeTypeDetector detector = new MimeTypeDetector(null);
		bc.registerService(IMimeTypeDetector.class.getName(), detector , null);
		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */		
		bc.addServiceListener(new DetectionHelperListener(detector,bc),DetectionHelperListener.FILTER);	
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		// nothing todo here
	}
}