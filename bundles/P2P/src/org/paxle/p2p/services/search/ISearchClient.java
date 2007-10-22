package org.paxle.p2p.services.search;

import java.util.List;

import org.paxle.p2p.services.IServiceClient;
import org.paxle.se.search.ISearchResult;

public interface ISearchClient extends IServiceClient {
	public List<ISearchResult> remoteSearch(String query, int maxResults, long timeout);
}
