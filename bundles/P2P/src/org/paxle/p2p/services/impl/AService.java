package org.paxle.p2p.services.impl;

import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.p2p.impl.P2PManager;

public abstract class AService {
	protected Log logger = LogFactory.getLog(this.getClass().getName());
	
	/**
	 * paxle p2p-manager. required for access to the application-peer-group.
	 */
	protected P2PManager p2pManager = null;

	/**
	 * The {@link #pg p2p-group} where the service should be published to
	 */
	protected PeerGroup pg = null;

	/**
	 * JXTA pipe-service. Required for creation of input- and output-pipes.
	 */
	protected PipeService pgPipeService = null;
	
	/**
	 * JXTA discovery-service. Required to publish and lookup advertisements
	 */
	protected DiscoveryService pgDiscoveryService = null;	
	
	protected AService(P2PManager p2pManager) {
		if (p2pManager == null) throw new NullPointerException("P2PManager is null");
		this.p2pManager = p2pManager;
		
		// get required services from peer group (e.g. pipe- and discovery-service)
		this.init(this.p2pManager.getPeerGroup());
	}	
	
	private void init(PeerGroup appPeerGroup) {
		if (appPeerGroup == null) throw new NullPointerException("Peer group is null");

		// the peer the service should be deployed to
		this.pg = appPeerGroup;

		// some peer group services needed later
		this.pgPipeService = this.pg.getPipeService();
		this.pgDiscoveryService = this.pg.getDiscoveryService();
	}
	
	public abstract void terminate();
}
