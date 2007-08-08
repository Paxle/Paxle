package org.paxle.se.search.impl;

import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchRequest;

public class SearchRequest implements ISearchRequest {
	
	private final int maxResults;
	private final long timeout;
	private AToken query;
	
	public SearchRequest(AToken query, int maxResults, long timeout) {
		this.maxResults = maxResults;
		this.query = query;
		this.timeout = timeout;
	}
	
	public int getMaxResultCount() {
		return this.maxResults;
	}
	
	public AToken getSearchQuery() {
		return this.query;
	}
	
	public long getTimeout() {
		return this.timeout;
	}
	
}
