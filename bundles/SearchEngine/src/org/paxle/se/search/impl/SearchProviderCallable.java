package org.paxle.se.search.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.ISearchResult;

public class SearchProviderCallable implements Callable<ISearchResult> {
	
	private ISearchProvider provider = null;
	private ISearchRequest searchRequest = null;
	private final List<IIndexerDocument> results = new ArrayList<IIndexerDocument>();
	
	public SearchProviderCallable(ISearchProvider provider, ISearchRequest searchRequest) {
		this.provider = provider;
		this.searchRequest = searchRequest;
	}
	
	public ISearchResult call() throws Exception {
		this.provider.search(this.searchRequest.getSearchQuery().getString(), this.results, this.searchRequest.getMaxResultCount());
		return new SearchResult(this.results);
	}
}
