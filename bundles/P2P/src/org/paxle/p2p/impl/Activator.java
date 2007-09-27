package org.paxle.p2p.impl;
/*
 * Created on Fri Jul 27 18:27:15 GMT+02:00 2007
 */

import java.io.File;
import java.util.Properties;

import net.jxta.peergroup.PeerGroup;

import org.apache.velocity.app.VelocityEngine;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.paxle.p2p.FirewallCheckViewServlet;
import org.paxle.p2p.IP2PManager;

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
	
	private static VelocityEngine velocity = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */			
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		// init P2P manager
		p2pManager = new P2PManager();
		p2pManager.init(new File("p2p"));
		
		// register the P2P-manager as a osgi service
		bc.registerService(IP2PManager.class.getName(), p2pManager, null);		
		bc.registerService(PeerGroup.class.getName(), p2pManager.getPeerGroup(), null);
		
		// init passive firewall check (hit on /paxle/firewallcheck means: not firewalled)
		initFirewallCheckPassive();
		
		// init active firewall check
		this.initFirewallCheckActive();
		
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
		velocity = null;
	}
	
	private void initFirewallCheckActive() {
		new PingFirewallcheckService("http://test.laxu.de/firewallcheck.php?port=8080");
	}
	
	private void initFirewallCheckPassive() {
		//getting a reference to the osgi http service
		ServiceReference sr = bc.getServiceReference(HttpService.class.getName());
		if(sr != null) {
			// getting the http service
			HttpService http = (HttpService) bc.getService(sr);
			if(http != null) {			
				// init velocity
				try {
					Properties velocityConfig = new Properties();
					velocityConfig.load(Activator.class.getResourceAsStream("/resources/velocity.properties"));
					velocityConfig.setProperty("jar.resource.loader.path", "jar:" + bc.getBundle().getLocation());		
					velocity = new VelocityEngine();
					velocity.init(velocityConfig);		
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// registering the servlet which will be accessible using 
				try {
					http.registerServlet("/paxle/firewallcheck", new FirewallCheckViewServlet(p2pManager, velocity), null, null);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}		
	}
}