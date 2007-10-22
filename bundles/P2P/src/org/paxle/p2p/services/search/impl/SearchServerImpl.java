package org.paxle.p2p.services.search.impl;

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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.p2p.impl.P2PManager;
import org.paxle.p2p.services.impl.AServiceServer;
import org.paxle.se.search.ISearchProviderManager;
import org.paxle.se.search.ISearchResult;

public class SearchServerImpl extends AServiceServer {
	/**
	 * paxle search provider. required for local search
	 */
	private ISearchProviderManager searchProviderManager = null;

	public SearchServerImpl(P2PManager p2pManager, ISearchProviderManager searchProviderManager) {
		super(p2pManager);
		this.searchProviderManager = searchProviderManager;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ModuleClassAdvertisement createModClassAdv() {
		ModuleClassAdvertisement mcadv = (ModuleClassAdvertisement) AdvertisementFactory.newAdvertisement(ModuleClassAdvertisement.getAdvertisementType());
		mcadv.setName(SearchServiceConstants.SERVICE_MOD_CLASS_NAME);
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
        mdadv.setName(SearchServiceConstants.SERVICE_MOD_SPEC_NAME);
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
			// getting the search query string
			String queryString = reqMsg.getMessageElement(SearchServiceConstants.REQ_QUERY).toString();
			Integer queryMaxResults = Integer.valueOf(reqMsg.getMessageElement(SearchServiceConstants.REQ_MAX_RESULTS).toString());
			Long queryTimeout = Long.valueOf(reqMsg.getMessageElement(SearchServiceConstants.REQ_TIMEOUT).toString());
			
			// getting the advertisement for the pipe where the response should be sent to
			MessageElement pipeAdv = reqMsg.getMessageElement(null, SearchServiceConstants.REQ_PIPE_ADV);
		    PipeAdvertisement adv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(
		    		(XMLDocument) StructuredDocumentFactory.newStructuredDocument(pipeAdv)
		    );			
		    
			/* ================================================================
			 * Process request
			 * ================================================================ */
		    // do a local search
			List<ISearchResult> searchResults = this.searchProviderManager.search(queryString, queryMaxResults, queryTimeout);
			
			// convert the result to XML
			Document xmlDoc = DocumentHelper.createDocument();
			Element xmlRoot = xmlDoc.addElement(SearchServiceConstants.RESULT_ROOT);
			
			if (searchResults != null) {
				for (ISearchResult result : searchResults) {
					Element resultElement = xmlRoot.addElement(SearchServiceConstants.RESULT_ENTRY);
					
					IIndexerDocument[] resultItems = result.getResult();
					if (resultItems != null) {						
						for (IIndexerDocument resultItem : resultItems) {
							Element resultItemElement = resultElement.addElement(SearchServiceConstants.RESULT_ENTRY_ITEM);
							
							// TODO: what fields to transfer?
							resultItemElement.addElement(IIndexerDocument.TITLE.getName())
											 .addText(resultItem.get(IIndexerDocument.TITLE));
							resultItemElement.addElement(IIndexerDocument.LOCATION.getName())
											 .addText(resultItem.get(IIndexerDocument.LOCATION));
						}
					}
				}
			}
			
			// build the response message
            Message respMessage = this.createResponseMessage(reqMsg);			
			respMessage.addMessageElement(null, new StringMessageElement(SearchServiceConstants.RESP_RESULT, xmlDoc.asXML(),null));
			
			/* ================================================================
			 * Write response
			 * ================================================================ */			
			// create an output pipe to send the request
			OutputPipe outputPipe = this.pgPipeService.createOutputPipe(adv, 10000l);
			
			// send the response back to the client
            outputPipe.send(respMessage);
            
            // close pipe
            outputPipe.close();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
