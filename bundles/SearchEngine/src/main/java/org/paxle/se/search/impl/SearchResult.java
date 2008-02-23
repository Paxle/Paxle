package org.paxle.se.search.impl;

import java.util.Collection;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.search.ISearchResult;

public class SearchResult implements ISearchResult {
	
	private final IIndexerDocument[] result;
	private final long searchTime;
	
	public SearchResult(Collection<IIndexerDocument> results, long searchTime) {
		this(results.toArray(new IIndexerDocument[results.size()]), searchTime);
	}
	
	public SearchResult(IIndexerDocument[] results, long searchTime) {
		this.result = results;
		this.searchTime = searchTime;
	}
	
	public IIndexerDocument[] getResult() {
		return this.result;
	}
	
	public long getSearchTime() {
		return this.searchTime;
	}
}
