package org.paxle.p2p.services.search.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.id.IDFactory;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IndexerDocument;
import org.paxle.p2p.impl.P2PManager;
import org.paxle.p2p.services.IService;
import org.paxle.p2p.services.impl.AServiceClient;
import org.paxle.p2p.services.search.ISearchClient;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchResult;

public class SearchClientImpl extends AServiceClient implements ISearchClient, ISearchProvider {

	/**
	 * A map to hold the received results
	 */
	private final HashMap<Integer, List<Message>> resultMap = new HashMap<Integer, List<Message>>();
	
	private IFieldManager fieldManager = null;
	
	/**
	 * {@inheritDoc}
	 */
	public SearchClientImpl(P2PManager p2pManager, IFieldManager fieldManager) {
		super(p2pManager);
		this.fieldManager = fieldManager;
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
			MessageElement query = respMsg.getMessageElement(SearchServiceConstants.REQ_ID);
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
		reqMessage.addMessageElement(null, new StringMessageElement(SearchServiceConstants.REQ_QUERY, query, null));
		reqMessage.addMessageElement(null, new StringMessageElement(SearchServiceConstants.REQ_MAX_RESULTS, Integer.toString(maxResults), null));
		reqMessage.addMessageElement(null, new StringMessageElement(SearchServiceConstants.REQ_TIMEOUT, Long.toString(timeout), null));
		
		// add entry into the resultMap
        synchronized (this.resultMap) {
			this.resultMap.put(reqMessage.getMessageNumber(), new ArrayList<Message>());
		}
		
		return reqMessage;
	}
	
	protected void sendRequestMessage(Message reqMessage) throws IOException {
		PipeAdvertisement otherPeerPipeAdv = null;
		Enumeration<Advertisement> advs = this.pgDiscoveryService.getLocalAdvertisements(DiscoveryService.ADV, "Name", SearchServiceConstants.SERVICE_MOD_SPEC_NAME);
		if (advs != null) {
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
						boolean success = outputPipe.send(reqMessage);
						if (!success) {
							// TODO: what to do in this case. Just retry?
						}			    
						this.sentMsgCount++;
						this.sentBytes += reqMessage.getByteLength();
						outputPipe.close();

					} catch (IOException ioe) {
						// unable to connect to the remote peer
						this.pgDiscoveryService.flushAdvertisement(otherPeerPipeAdv);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected List<ISearchResult> extractResult(Message reqMessage) {
		List<Message> messageList = null;
		synchronized (this.resultMap) {				
			if (!this.resultMap.containsKey(reqMessage.getMessageNumber())) {
				this.logger.warn("Unknown requestID: " + reqMessage.getMessageNumber());
				messageList = Collections.EMPTY_LIST;
			} else {
				// remove the request from the result-list
				messageList = this.resultMap.remove(reqMessage.getMessageNumber());
			}
		}	
		
		List<ISearchResult> resultList = new ArrayList<ISearchResult>();
		for (Message msg : messageList) {
			try {
				MessageElement resultListElement = msg.getMessageElement(SearchServiceConstants.RESP_RESULT);			

				SAXReader reader = new SAXReader();
				Document xmlDoc = reader.read(resultListElement.getStream());
				Element xmlRoot = xmlDoc.getRootElement();
				
		        // iterate through child elements of root
		        for (Iterator<Element> i = xmlRoot.elementIterator(SearchServiceConstants.RESULT_ENTRY); i.hasNext(); ) {
		            Element resultElement = (Element) i.next();
		            List<IIndexerDocument> indexerDocs = new ArrayList<IIndexerDocument>();
		            
			        for (Iterator<Element> j = resultElement.elementIterator(SearchServiceConstants.RESULT_ENTRY_ITEM); j.hasNext(); ) {
			        	Element resultItemElement = (Element) j.next();			        	
			        	IndexerDocument indexerDoc = new IndexerDocument();
			        	
			        	for (Iterator<Element> k = resultItemElement.elementIterator(); k.hasNext(); ) {
			        		Element resultItemFieldElement = (Element) k.next();
			        		
			        		String fieldName = resultItemFieldElement.getName();
			        		String fieldValue = resultItemFieldElement.getText();
			        		
			        		final org.paxle.core.doc.Field<?> pfield = this.fieldManager.get(fieldName);
			    			if (pfield != null) {			    				
			    				indexerDoc.put(pfield, fieldValue);
			    			}
			        	}
			        }
			        
		            RemoteSearchResult searchResult = new RemoteSearchResult(indexerDocs,-1); 
		            resultList.add(searchResult);
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return resultList;
	}

	/**
	 * FIXME: just a test version of the service
	 * @param query
	 * @return 
	 */
	public List<ISearchResult> remoteSearch(String query, int maxResults, long timeout) {
		try {
			/* ================================================================
			 * SEND REQUEST
			 * ================================================================ */
			
			/* ----------------------------------------------------------------
			 * Discover Service
			 * ---------------------------------------------------------------- */
			this.pgDiscoveryService.getRemoteAdvertisements(null, DiscoveryService.ADV, "Name", SearchServiceConstants.SERVICE_MOD_SPEC_NAME ,10);

			/* ----------------------------------------------------------------
			 * build the request message
			 * ---------------------------------------------------------------- */
            Message reqMessage = this.createRequestMessage(query, maxResults, timeout);
                        
			// wait a few seconds
            // TODO: change this
			Thread.sleep(3000);
			
			/* ----------------------------------------------------------------
			 * Connect to other peers
			 * ---------------------------------------------------------------- */			
			long reqStart = System.currentTimeMillis();
			this.sendRequestMessage(reqMessage);
			
			
			/* ================================================================
			 * WAIT FOR RESPONSE
			 * ================================================================ */
			long timeToWait = Math.max(timeout - (System.currentTimeMillis() - reqStart),1);
			System.out.println("Waiting " + timeToWait + " ms for the result.");			
			Thread.sleep(timeToWait);
			
			// Access result
			return this.extractResult(reqMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @see IService#getServiceIdentifier()
	 */
	public String getServiceIdentifier() {
		return SearchServiceConstants.SERVICE_MOD_SPEC_NAME;
	}

	/**
	 * @see ISearchProvider#getTokenFactory()
	 */
	public ITokenFactory getTokenFactory() {
		return new SearchServiceTokenFactor();
	}

	/**
	 * @see ISearchProvider#search(String, List, int)
	 */
	public void search(String request, List<IIndexerDocument> results, int maxCount) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		System.err.println("Needs to be implemented!");
	}
}
