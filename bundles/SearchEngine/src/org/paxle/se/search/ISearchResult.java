package org.paxle.se.search;

import org.paxle.core.doc.IIndexerDocument;

public interface ISearchResult {	
	public IIndexerDocument[] getResult();
	public long getSearchTime();
	
	/* TODO: get search result metadata such as 
	 * - the name of the provider
	 * - total number of results
	 * - ranking?
	 */
}
