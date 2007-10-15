package org.paxle.p2p.services.impl;

import org.paxle.p2p.impl.P2PManager;

import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

public class SearchServiceClientImpl extends AServiceClient {

	/**
	 * {@inheritDoc}
	 */
	protected SearchServiceClientImpl(P2PManager p2pManager) {
		super(p2pManager);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PipeAdvertisement createPipeAdvertisement() {
		// create ID for the pipe 
		// TODO: do we need a new ID each time?
		PipeID pipeID = IDFactory.newPipeID(this.pg.getPeerGroupID());
		
		// create a new pipe advertisement and initialize it
		PipeAdvertisement pipeAdv = (PipeAdvertisement)AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
		pipeAdv.setPipeID(pipeID);
		pipeAdv.setType(PipeService.UnicastType);
		pipeAdv.setName("Paxle:SearchService:ResponsePipe");
		pipeAdv.setDescription("Paxle Search Service - Response queue");
		return pipeAdv;	
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
		
	}


}
