package org.paxle.se.provider.paxleinfrastructure.impl;

import java.io.IOException;
import java.util.List;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IndexerDocument;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.search.ISearchProvider;

public class PaxleInfrastructureSearchProvider implements ISearchProvider {

	public PaxleInfrastructureSearchProvider(){
	}
	public ITokenFactory getTokenFactory() {
		return new PaxleInfrastructureTokenFactor();
	}
	
	
	public void search(String request, List<IIndexerDocument> results, int maxCount, long timeout) throws IOException, InterruptedException {
		try {
			IIndexerDocument indexerDoc = new IndexerDocument();
			if(request.toLowerCase().equals("paxle wiki")){
				indexerDoc.set(IIndexerDocument.LOCATION, "http://wiki.paxle.net/");
				indexerDoc.set(IIndexerDocument.TITLE, "Paxle Wiki");
			}else if(request.toLowerCase().equals("paxle homepage")){
				indexerDoc.set(IIndexerDocument.LOCATION, "http://wiki.paxle.net/");
				indexerDoc.set(IIndexerDocument.TITLE, "Paxle Homepage");
			}else if(request.toLowerCase().equals("paxle forum")){
				indexerDoc.set(IIndexerDocument.LOCATION, "http://forum.paxle.info/");
				indexerDoc.set(IIndexerDocument.TITLE, "Paxle Forum");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
