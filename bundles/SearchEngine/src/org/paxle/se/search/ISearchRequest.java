package org.paxle.se.search;

import org.paxle.se.query.tokens.AToken;

public interface ISearchRequest {
	
	public long getTimeout();
	public AToken getSearchQuery();
	public int getMaxResultCount();
	
	/* TODO: other request metadata not directly available in the query string, e.g.
	 * - max search results
	 * - ranking parameter?
	 */
}
