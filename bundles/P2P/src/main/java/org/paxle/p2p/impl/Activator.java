package org.paxle.p2p.impl;

import java.io.File;
import java.net.URL;

import net.jxta.peergroup.PeerGroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.paxle.p2p.IP2PManager;
import org.paxle.p2p.services.search.impl.SearchServerImpl;

/**
 * ATTENTION: Set the property value of
 * <code>org.osgi.framework.system.packages</code>
 * to <code>javax.security.cert,sun.reflect</code>
 * to run the following code:	
 */
public class Activator implements BundleActivator {
	private P2PManager p2pManager = null;
	
	private P2PServiceManager serviceManager = null;
	
	/**
	 * For logging
	 */
	private Log logger = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */			
	public void start(BundleContext bc) throws Exception {
		// init logger
		this.logger = LogFactory.getLog(this.getClass());
		
		// init P2P manager
		URL seedURI = bc.getBundle().getResource("/resources/seeds.txt");
		this.logger.info(String.format("Loading seed-file from '%s'.", seedURI==null?"null":seedURI.toString()));
		
		// init P2P manager
		p2pManager = new P2PManager(new File("p2p"), seedURI.toURI());
		
		// register the P2P-manager as a osgi service
		bc.registerService(IP2PManager.class.getName(), p2pManager, null);		
		bc.registerService(PeerGroup.class.getName(), p2pManager.getPeerGroup(), null);
		
		// init passive firewall check (hit on /paxle/firewallcheck means: not firewalled)
//		initFirewallCheckPassive();
		
		// init active firewall check
//		this.initFirewallCheckActive();
		
		// create a P2P-service-manager and register it as OSGI service listener
		P2PServiceManager serviceManager = new P2PServiceManager(p2pManager, bc);
		bc.addServiceListener(serviceManager);
		
		Filter filter = bc.createFilter("(objectClass=org.paxle.se.search.ISearchProviderManager)");
		serviceManager.addService(new Filter[]{filter}, SearchServerImpl.class);
		
		/*
		 * Get the search provider.
		 * ATTENTION: don't replace the string by ISearchProviderManager.class.getName(), otherwise the
		 * 			  paxle search bundle is not optional. 
		 */
//		ServiceReference searchProviderRef = bc.getServiceReference("org.paxle.se.search.ISearchProviderManager");
//		SearchServerImpl searchServiceServer = null;
//		if (searchProviderRef != null) {			
//			searchServiceServer = new SearchServerImpl(p2pManager,(ISearchProviderManager)bc.getService(searchProviderRef));
//			bc.registerService(new String[]{IService.class.getName(), IServiceServer.class.getName()}, searchServiceServer, null);
//		}
//		ServiceReference fieldManagerRef = bc.getServiceReference("org.paxle.se.index.IFieldManager"); 
//		if (fieldManagerRef != null) {
//			// start a new search client
//			SearchClientImpl searchServiceClient = new SearchClientImpl(
//					p2pManager,
//					(IFieldManager)bc.getService(fieldManagerRef),
//					searchServiceServer
//			);
//			
//			// register remote search service
//			bc.registerService(new String[]{
//					IService.class.getName(), 
//					IServiceClient.class.getName(),
//					ISearchClient.class.getName(),
//					"org.paxle.se.search.ISearchProvider"}, // ATTENTION: do not replace the string by class.getName()
//					searchServiceClient, 
//					null
//			);
//		}
		
//		MeteorPeer mp = new MeteorPeer();
//		mp.init(p2pManager.getPeerGroup(), null, null);
//		mp.startApp(null);

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
		p2pManager.terminate();
		p2pManager = null;
		
		this.serviceManager.stopAllServices();
	}	
}