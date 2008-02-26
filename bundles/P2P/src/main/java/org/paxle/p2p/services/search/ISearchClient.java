package org.paxle.p2p.services.search;

import java.io.IOException;
import java.util.List;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.p2p.services.IServiceClient;

public interface ISearchClient extends IServiceClient {
	public List<IIndexerDocument> remoteSearch(String query, int maxResults, long timeout) throws IOException, InterruptedException;
}
