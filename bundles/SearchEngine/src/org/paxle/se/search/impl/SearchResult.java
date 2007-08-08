package org.paxle.se.search.impl;

import java.util.Collection;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.search.ISearchResult;

public class SearchResult implements ISearchResult {
	
	private final IIndexerDocument[] result;
	
	public SearchResult(Collection<IIndexerDocument> results) {
		this.result = results.toArray(new IIndexerDocument[results.size()]);
	}
	
	public SearchResult(IIndexerDocument[] results) {
		this.result = results;
	}
	
	public IIndexerDocument[] getResult() {
		return this.result;
	}
}
