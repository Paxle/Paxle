package org.paxle.se.search;

import org.paxle.core.doc.IIndexerDocument;

public interface ISearchResult {	
	public IIndexerDocument[] getResult();
	
	/* TODO: get search result metadata such as 
	 * - the name of the provider
	 * - the time used for query
	 * - total number of results
	 * - ranking?
	 */
}
