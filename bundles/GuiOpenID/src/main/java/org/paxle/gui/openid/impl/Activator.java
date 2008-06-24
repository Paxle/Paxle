
package org.paxle.gui.openid.impl;

import org.openid4java.consumer.ConsumerManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

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
		
		ServiceReference sr  =  bc.getServiceReference(HttpService.class.getName());
		HttpService http     = (HttpService)bc.getService(sr);
		
		// register the servlet
		ConsumerManager manager = new ConsumerManager();
		http.registerServlet("/openid/auth",new AuthServlet(manager),null,null);
		http.registerServlet("/openid/verify",new VerifyServlet(manager),null,null);
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