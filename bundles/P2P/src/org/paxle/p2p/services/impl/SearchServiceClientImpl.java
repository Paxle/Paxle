package org.paxle.p2p.services.impl;

import java.io.IOException;
import java.util.Enumeration;

import org.paxle.p2p.impl.P2PManager;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.id.IDFactory;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.ModuleSpecAdvertisement;
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

	/**
	 * Function to process the response that was received for the service request message
	 * that was sent by {@link #remoteSearch(String)}
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void processResponse(Message respMsg) {
		// TODO Auto-generated method stub		
	}

	/**
	 * FIXME: just a test version of the service
	 * @param query
	 */
	public void remoteSearch(String query) {
		try {
			// FIXME: move this string
			String SERVICE_NAME = "JXTASPEC:*";
			
			// discover service
			this.pgDiscoveryService.getRemoteAdvertisements(null, DiscoveryService.ADV, "Name",SERVICE_NAME ,10);

			// wait a few seconds
			Thread.sleep(10000);
			
			// loop through the found advertisements
			PipeAdvertisement otherPeerPipeAdv = null;
			Enumeration<Advertisement> advs = this.pgDiscoveryService.getLocalAdvertisements(DiscoveryService.ADV, "Name", SERVICE_NAME);
			while (advs.hasMoreElements()) {
				Advertisement adv = advs.nextElement();
				System.out.println(adv.toString());
				if (adv instanceof ModuleSpecAdvertisement) {		
					try {
						// getting the pipe adv of the other peer
						otherPeerPipeAdv = ((ModuleSpecAdvertisement)adv).getPipeAdvertisement();

						// create an output pipe to send the request
						OutputPipe outputPipe = this.pgPipeService.createOutputPipe(otherPeerPipeAdv, 10000);
						
						// build the request message
			            Message reqMsg = new Message();
			            StringMessageElement sme = new StringMessageElement("query", query, null);
			            reqMsg.addMessageElement(null, sme);

			            // send the message to the service pipe
			            outputPipe.send(reqMsg);
						
					} catch (IOException ioe) {
						// unable to connect to the remote peer
						this.pgDiscoveryService.flushAdvertisement(otherPeerPipeAdv);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
