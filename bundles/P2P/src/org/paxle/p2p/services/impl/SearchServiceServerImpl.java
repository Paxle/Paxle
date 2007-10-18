package org.paxle.p2p.services.impl;

import java.util.List;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.id.IDFactory;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.platform.ModuleClassID;
import net.jxta.protocol.ModuleClassAdvertisement;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.paxle.p2p.impl.P2PManager;
import org.paxle.se.search.ISearchProviderManager;
import org.paxle.se.search.ISearchResult;

public class SearchServiceServerImpl extends AServiceServer {
	public static final String REQ_ID = "reqID";
	public static final String REQ_PIPE_ADV = "pipeAdv";
	public static final String REQ_QUERY = "query";
	public static final String REQ_MAX_RESULTS = "maxResults";
	public static final String REQ_TIMEOUT = "timeout";
	
	public static final String RESP_SIZE = "size";
	public static final String RESP_REQ_ID = REQ_ID;
	
	/**
	 * paxle search provider. required for local search
	 */
	private ISearchProviderManager searchProviderManager = null;

	public SearchServiceServerImpl(P2PManager p2pManager, ISearchProviderManager searchProviderManager) {
		super(p2pManager);
		this.searchProviderManager = searchProviderManager;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ModuleClassAdvertisement createModClassAdv() {
		ModuleClassAdvertisement mcadv = (ModuleClassAdvertisement) AdvertisementFactory.newAdvertisement(ModuleClassAdvertisement.getAdvertisementType());
		mcadv.setName("JXTAMOD:Paxle:SearchService");
		mcadv.setDescription("Paxle Remote Search Service");
		ModuleClassID mcID = IDFactory.newModuleClassID();
		mcadv.setModuleClassID(mcID);
		return mcadv;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ModuleSpecAdvertisement createModSpecAdv(ModuleClassID mcID, PipeAdvertisement pipeAdv) {
        ModuleSpecAdvertisement mdadv = (ModuleSpecAdvertisement) AdvertisementFactory.newAdvertisement(ModuleSpecAdvertisement.getAdvertisementType());
        mdadv.setName("JXTASPEC:Paxle:SearchService");
        mdadv.setVersion("Version 1.0");
        mdadv.setCreator("www.paxle.org");
        mdadv.setModuleSpecID(IDFactory.newModuleSpecID(mcID));
        mdadv.setSpecURI("http://wiki.paxle.net/dev/p2p/");			
        mdadv.setPipeAdvertisement(pipeAdv);
        return mdadv;
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
		pipeAdv.setName("Paxle:SearchService:RequestPipe");
		pipeAdv.setDescription("Paxle Search Service - Request pipe");
		return pipeAdv;	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processRequest(Message reqMsg) {
		try {
			/* ================================================================
			 * Read the request
			 * ================================================================ */
			
			// getting the request ID
			MessageElement queryID = reqMsg.getMessageElement(REQ_ID);
			int reqMsgNr = Integer.valueOf(new String(queryID.getBytes(false),"UTF-8")).intValue();
			System.out.println(String.format("QueryID: %s",reqMsgNr));					
			
			// getting the search query string
			MessageElement query = reqMsg.getMessageElement(REQ_QUERY);
			String queryString = new String(query.getBytes(false),"UTF-8");
			System.out.println(String.format("Query: %s",queryString));		
			
			// getting the advertisement for the pipe where the response should be sent to
			MessageElement pipeAdv = reqMsg.getMessageElement(null, REQ_PIPE_ADV);
		    PipeAdvertisement adv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(
		    		(XMLDocument) StructuredDocumentFactory.newStructuredDocument(pipeAdv)
		    );			
		    
			/* ================================================================
			 * Process request
			 * ================================================================ */
			List<ISearchResult> results = this.searchProviderManager.search(queryString, 10, 30);
			System.out.println(String.format("%d results found for query '%s'.",results.size(),query));
			
			/* ================================================================
			 * Write response
			 * ================================================================ */			
			// create an output pipe to send the request
			OutputPipe outputPipe = this.pgPipeService.createOutputPipe(adv, 10000l);
            Message respMessage = new Message();
            respMessage.addMessageElement(null, new StringMessageElement(RESP_SIZE, Integer.toString(results.size()), null));
            respMessage.addMessageElement(null, new StringMessageElement(RESP_REQ_ID, Integer.toString(reqMsgNr), null));
            // TODO: add the search result to the response message
            outputPipe.send(respMessage);
            
            // close pipe
            outputPipe.close();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
