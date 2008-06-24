
package org.paxle.gui.openid.impl;

import org.openid4java.consumer.ConsumerManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {
	private ServiceTracker userAdminTracker = null;	
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext context) throws Exception {
		
		ServiceReference sr  =  context.getServiceReference(HttpService.class.getName());
		HttpService http  = (HttpService)context.getService(sr);
		
		this.userAdminTracker = new ServiceTracker(context, UserAdmin.class.getName(),null);
		this.userAdminTracker.open();		
		
		// register the servlet
		ConsumerManager manager = new ConsumerManager();
		http.registerServlet("/openid/auth",new AuthServlet(manager),null,null);
		http.registerServlet("/openid/verify",new VerifyServlet(this.userAdminTracker, manager),null,null);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
	
	}
}