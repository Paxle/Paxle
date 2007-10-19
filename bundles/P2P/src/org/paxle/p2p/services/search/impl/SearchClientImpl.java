package org.paxle.p2p.services.search.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.paxle.p2p.impl.P2PManager;
import org.paxle.p2p.services.impl.AServiceClient;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.InputStreamMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.id.IDFactory;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

public class SearchClientImpl extends AServiceClient {

	/**
	 * A map to hold the received results
	 */
	private final HashMap<Integer, List<Message>> resultMap = new HashMap<Integer, List<Message>>();
	
	/**
	 * {@inheritDoc}
	 */
	public SearchClientImpl(P2PManager p2pManager) {
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
		try {
			// TODO Auto-generated method stub		

			// TODO: extract the result
			// getting the search query string
			MessageElement query = respMsg.getMessageElement(SearchServerImpl.REQ_ID);
			Integer reqNr = Integer.valueOf(new String(query.getBytes(false),"UTF-8"));
			System.out.println(String.format("ReqNr: %d",reqNr));	
			
			List<Message> resultList = null;
			synchronized (this.resultMap) {				
				if (!this.resultMap.containsKey(reqNr)) {
					this.logger.warn("Unknown requestID: " + reqNr);
					return;
				} else {
					resultList = this.resultMap.get(reqNr);
				}
			}
			
			synchronized (resultList) {
				resultList.add(respMsg);
			}

			// insert it into the result queue
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected Message createRequestMessage(String query, int maxResults, long timeout) {
		Message reqMessage = super.createRequestMessage();
		
		// append the search parameters
		reqMessage.addMessageElement(null, new StringMessageElement(SearchServerImpl.REQ_QUERY, query, null));
		reqMessage.addMessageElement(null, new StringMessageElement(SearchServerImpl.REQ_MAX_RESULTS, Integer.toString(maxResults), null));
		reqMessage.addMessageElement(null, new StringMessageElement(SearchServerImpl.REQ_TIMEOUT, Long.toString(timeout), null));
		
		return reqMessage;
	}

	/**
	 * FIXME: just a test version of the service
	 * @param query
	 */
	public void remoteSearch(String query, int maxResults, long timeout) {
		try {
			/* ================================================================
			 * SEND REQUEST
			 * ================================================================ */
			// FIXME: move this string
			String SERVICE_NAME = "JXTASPEC:*";
			
			/* ----------------------------------------------------------------
			 * Discover Service
			 * ---------------------------------------------------------------- */
			this.pgDiscoveryService.getRemoteAdvertisements(null, DiscoveryService.ADV, "Name", SERVICE_NAME ,10);

			/* ----------------------------------------------------------------
			 * build the request message
			 * ---------------------------------------------------------------- */
            Message reqMsg = this.createRequestMessage(query, maxResults, timeout);
            int reqNr = reqMsg.getMessageNumber();
            
			/* ----------------------------------------------------------------
			 * add entry into the resultMap
			 * ---------------------------------------------------------------- */            
            synchronized (this.resultMap) {
				this.resultMap.put(Integer.valueOf(reqNr), new ArrayList<Message>());
			}
            
			// wait a few seconds
            // TODO: change this
			Thread.sleep(3000);
			
			/* ----------------------------------------------------------------
			 * Connect to other peers
			 * ---------------------------------------------------------------- */			
			long reqStart = System.currentTimeMillis();
			PipeAdvertisement otherPeerPipeAdv = null;
			Enumeration<Advertisement> advs = this.pgDiscoveryService.getLocalAdvertisements(DiscoveryService.ADV, "Name", SERVICE_NAME);
			while (advs.hasMoreElements()) {
				Advertisement adv = advs.nextElement();
				System.out.println(adv.toString());
				if (adv instanceof ModuleSpecAdvertisement) {		
					try {
						// FIXME: does not work! getting the pipe adv of the other peer
						otherPeerPipeAdv = ((ModuleSpecAdvertisement)adv).getPipeAdvertisement();
						if (otherPeerPipeAdv.getID().toString().equals(this.serviceInputPipe.getPipeID().toString())) {
							this.logger.debug("Found our own pipe. Continue ...");
//							break;
						}

						// create an output pipe to send the request
						OutputPipe outputPipe = this.pgPipeService.createOutputPipe(otherPeerPipeAdv, 10000);
						
			            // send the message to the service pipe
			            outputPipe.send(reqMsg);
			            outputPipe.close();
						
					} catch (IOException ioe) {
						// unable to connect to the remote peer
						this.pgDiscoveryService.flushAdvertisement(otherPeerPipeAdv);
					}
				}
			}
			
			
			/* ================================================================
			 * WAIT FOR RESPONSE
			 * ================================================================ */
			long timeToWait = Math.max(reqStart - System.currentTimeMillis(),1);
			System.out.println("Waiting " + timeToWait + " ms for the result.");			
			Thread.sleep(timeToWait);
			
			// Access result
			List<Message> resultList = null;
			synchronized (this.resultMap) {				
				if (!this.resultMap.containsKey(reqNr)) {
					this.logger.warn("Unknown requestID: " + reqNr);
					return;
				} else {
					// remove the request from the result-list
					resultList = this.resultMap.remove(reqNr);
				}
			}			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
