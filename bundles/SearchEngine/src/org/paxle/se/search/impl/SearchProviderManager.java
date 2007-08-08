package org.paxle.se.search.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.paxle.se.query.PaxleQueryParser;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchProviderManager;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.ISearchResult;

public class SearchProviderManager implements ISearchProviderManager {
	
	private final ExecutorService execService;
	private final List<ISearchProvider> providers = new ArrayList<ISearchProvider>();
	private final PaxleQueryParser pqp = new PaxleQueryParser();
	
	public SearchProviderManager() {
		this.execService = Executors.newCachedThreadPool();
	}
	
	void addProvider(ISearchProvider provider) {
		this.providers.add(provider);
		this.pqp.addTokenFactory(provider.getTokenFactory());
	}
	
	public List<ISearchResult> search(String paxleQuery, int maxResults, long timeout) throws InterruptedException, ExecutionException {
		List<ISearchResult> results = new ArrayList<ISearchResult>();
		
		CompletionService<ISearchResult> execCompletionService = new ExecutorCompletionService<ISearchResult>(this.execService);
		
		final List<AToken> queries = this.pqp.parse(paxleQuery);
		
		// XXX: the "internal" search plugins should be applied here to the queries-list

		int n = providers.size();
		for (int i=0; i<n; i++) {
			final ISearchRequest searchRequest = new SearchRequest(queries.get(i), maxResults, timeout);
			execCompletionService.submit(new SearchProviderCallable(this.providers.get(i), searchRequest));
		}
		
		for (int i = 0; i < n; ++i) {
			long start = System.currentTimeMillis();
			
			final Future<ISearchResult> future = execCompletionService.poll(timeout, TimeUnit.MILLISECONDS);
			if (future != null) {
				ISearchResult r = future.get();
				
				if (r != null) {
					results.add(r);
				}
			}
			
			long diff = System.currentTimeMillis() - start;
			if ((timeout-=diff)<= 0) break;
		}
		
		return results;
	}
}
