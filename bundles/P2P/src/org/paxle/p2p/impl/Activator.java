package org.paxle.p2p.impl;
/*
 * Created on Fri Jul 27 18:27:15 GMT+02:00 2007
 */

import java.io.File;
import java.net.URL;
import java.util.Properties;

import meteor.lookup.MeteorPeer;
import net.jxta.peergroup.PeerGroup;

import org.apache.velocity.app.VelocityEngine;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.paxle.p2p.FirewallCheckViewServlet;
import org.paxle.p2p.IP2PManager;
import org.paxle.p2p.services.IService;
import org.paxle.p2p.services.IServiceClient;
import org.paxle.p2p.services.IServiceServer;
import org.paxle.p2p.services.search.ISearchClient;
import org.paxle.p2p.services.search.impl.SearchClientImpl;
import org.paxle.p2p.services.search.impl.SearchServerImpl;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.search.ISearchProviderManager;

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
		URL seedURI = bc.getBundle().getResource("/resources/seeds.txt");
		p2pManager = new P2PManager(new File("p2p"), seedURI.toURI());
		
		// register the P2P-manager as a osgi service
		bc.registerService(IP2PManager.class.getName(), p2pManager, null);		
		bc.registerService(PeerGroup.class.getName(), p2pManager.getPeerGroup(), null);
		
		// init passive firewall check (hit on /paxle/firewallcheck means: not firewalled)
//		initFirewallCheckPassive();
		
		// init active firewall check
//		this.initFirewallCheckActive();
		
		/*
		 * Get the search provider.
		 * ATTENTION: don't replace the string by ISearchProviderManager.class.getName(), otherwise the
		 * 			  paxle search bundle is not optional. 
		 */
		ServiceReference searchProviderRef = bc.getServiceReference("org.paxle.se.search.ISearchProviderManager");
		SearchServerImpl searchServiceServer = null;
		if (searchProviderRef != null) {			
			searchServiceServer = new SearchServerImpl(p2pManager,(ISearchProviderManager)bc.getService(searchProviderRef));
			bc.registerService(new String[]{IService.class.getName(), IServiceServer.class.getName()}, searchServiceServer, null);
		}
		ServiceReference fieldManagerRef = bc.getServiceReference("org.paxle.se.index.IFieldManager"); 
		if (fieldManagerRef != null) {
			// start a new search client
			SearchClientImpl searchServiceClient = new SearchClientImpl(
					p2pManager,
					(IFieldManager)bc.getService(fieldManagerRef),
					searchServiceServer
			);
			
			// register remote search service
			bc.registerService(new String[]{
					IService.class.getName(), 
					IServiceClient.class.getName(),
					ISearchClient.class.getName(),
					"org.paxle.se.search.ISearchProvider"}, // ATTENTION: do not replace the string by class.getName()
					searchServiceClient, 
					null
			);
		}
		
		MeteorPeer mp = new MeteorPeer();
		mp.init(p2pManager.getPeerGroup(), null, null);
		mp.startApp(null);

		/* ==========================================================
		 * Register Services
		 * ========================================================== */
		
//		
//		// just for testing
//		new Thread() {
//			@Override
//			public void run() {
//				ServiceReference fieldManagerRef = bc.getServiceReference("org.paxle.se.index.IFieldManager"); 
//				if (fieldManagerRef != null) {
//					SearchClientImpl client = new SearchClientImpl(p2pManager,(IFieldManager)bc.getService(fieldManagerRef));				
////					try {
////					Thread.sleep(60000);
////					} catch (InterruptedException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////					}
//
//					while(true) {
//						client.remoteSearch("test",100,6000);
//					}
//				}
//			}
//		}.start();
		
		
		
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		// cleanup
		bc = null;		
		p2pManager.terminate();
		p2pManager = null;
		velocity = null;
	}
	
	/**
	 * TODO: replace this
	 * 
	 * To detect if a peer is capable to work as a relay peer can do the following
	 * 1.) contact an already known relay peer
	 * 2.) force the relay peer to open a plain tcp connection to the jxta port of our peer
	 * If the peer is capable to accept connection from outside, the connection will be accepted, e.g.
	 * <pre>
	 * theli@theli:~/workspace/JxtaTest2$ telnet 192.168.0.93 9701
	 * Trying 192.168.0.93...
	 * Connected to 192.168.0.93.
	 * Escape character is '^]'.
	 * JXTAHELLO tcp://192.168.0.96:35381 tcp://192.168.0.93:9701 urn:jxta:uuid-59616261646162614E50472050325033E0BE60AC7C38466FB8EFCB6A4ED2230203 0 1.1
	 * </pre>
	 * 
	 * @deprecated
	 */
	private void initFirewallCheckActive() {
		new PingFirewallcheckService("http://test.laxu.de/firewallcheck.php?port=8080");
	}
	
	/**
	 * @deprecated not required anymore, see {@link #initFirewallCheckActive()}
	 */
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