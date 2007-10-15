package org.paxle.p2p.services.impl;

import net.jxta.protocol.PipeAdvertisement;

import org.paxle.p2p.impl.P2PManager;


abstract class AServiceClient extends AService {

	/**
	 * @param p2pManager required to get jxta peer-group services (e.g. pipe- and discovery-service)
	 */
	protected AServiceClient(P2PManager p2pManager) {
		super(p2pManager);
	}

	/**
	 * Function to create the advertisement for the {@link #serviceInputPipe input-pipe} 
	 * @return
	 */
	protected abstract PipeAdvertisement createPipeAdvertisement();
	
}
