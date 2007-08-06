package org.paxle.se.search;

public interface ISearchRequest {
	public long getTimeout();
	public String getSearchQuery();
	
	/* TODO: other request metadata not directly available in the query string, e.g.
	 * - max search results
	 * - ranking parameter?
	 */
}
