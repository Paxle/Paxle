package org.paxle.p2p.services.impl;

import java.util.List;

import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.Message;
import net.jxta.id.IDFactory;
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
			byte[] msgBytes = reqMsg.getMessageElement("query").getBytes(true);
			String query = new String(msgBytes,"UTF-8");
			System.out.println(String.format("Query: %s",query));
			
			List<ISearchResult> results = this.searchProviderManager.search(query, 10, 30);
			System.out.println(String.format("%d results found for query '%s'.",results.size(),query));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
