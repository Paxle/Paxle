package org.paxle.p2p.services.search;

public interface SearchClient {
	public void remoteSearch(String query, int maxResults, long timeout);
}
