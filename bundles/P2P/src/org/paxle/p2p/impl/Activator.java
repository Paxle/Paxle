package org.paxle.p2p.impl;
/*
 * Created on Fri Jul 27 18:27:15 GMT+02:00 2007
 */

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.paxle.p2p.FirewallCheckViewServlet;

/**
 * ATTENTION: Set the property value of
 * <code>org.osgi.framework.system.packages</code>
 * to <code>javax.security.cert,sun.reflect</code>
 * to run the following code:	
 */
public class Activator implements BundleActivator {
	
	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	public static BundleContext bc;				
	
	private static P2PManager p2pManager = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */			
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		// init P2P manager
		p2pManager = new P2PManager();
		p2pManager.init();
		
		// init active firewall check
		this.initFirewallCheckActive();
		
		// init passive firewall check
		initFirewallCheckPassive();
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		// cleanup
		bc = null;		
		p2pManager.stop();
		p2pManager = null;
	}
	
	private void initFirewallCheckActive() {
		new FirewallCheck(p2pManager);
	}
	
	private void initFirewallCheckPassive() {
		//getting a reference to the osgi http service
		ServiceReference sr = bc.getServiceReference(HttpService.class.getName());
		if(sr != null) {
			// getting the http service
			HttpService http = (HttpService) bc.getService(sr);
			if(http != null) {				
				// registering the servlet which will be accessible using 
				try {
					http.registerServlet("/paxle/firewallcheck", new FirewallCheckViewServlet(p2pManager), null, null);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}		
	}
}