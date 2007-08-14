package org.paxle.se.search.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.ISearchResult;

public class SearchProviderCallable implements Callable<ISearchResult> {
	
	private final Log logger;
	private final List<IIndexerDocument> results = new ArrayList<IIndexerDocument>();
	private final ISearchProvider provider;
	private final ISearchRequest searchRequest;
	
	public SearchProviderCallable(ISearchProvider provider, ISearchRequest searchRequest) {
		this.provider = provider;
		this.searchRequest = searchRequest;
		this.logger = LogFactory.getLog(getClass().getSimpleName() + "_" + provider.getClass().getSimpleName());
	}
	
	public ISearchResult call() throws Exception {
		final long start = System.currentTimeMillis();
		try {
			final String query = this.searchRequest.getSearchQuery().getString();
			this.logger.info("starting search for '" + query + "' (" + this.searchRequest.getSearchQuery().toString() + ")");
			this.provider.search(query, this.results, this.searchRequest.getMaxResultCount());
		} catch (InterruptedException e) { /* just fall through */ }
		return new SearchResult(this.results, System.currentTimeMillis() - start);
	}
}
