package org.paxle.se.search.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.ISearchResult;

public class SearchProviderCallable implements Callable<ISearchResult> {
	
	private final List<IIndexerDocument> results = new ArrayList<IIndexerDocument>();
	private final ISearchProvider provider;
	private final ISearchRequest searchRequest;
	
	public SearchProviderCallable(ISearchProvider provider, ISearchRequest searchRequest) {
		this.provider = provider;
		this.searchRequest = searchRequest;
	}
	
	public ISearchResult call() throws Exception {
		final long start = System.currentTimeMillis();
		try {
			this.provider.search(this.searchRequest.getSearchQuery().getString(), this.results, this.searchRequest.getMaxResultCount());
		} catch (InterruptedException e) { /* just fall through */ }
		return new SearchResult(this.results, System.currentTimeMillis() - start);
	}
}
