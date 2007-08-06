package org.paxle.se.search.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchProviderManager;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.ISearchResult;

public class SearchProviderManager implements ISearchProviderManager {
	private ExecutorService execService = null;
	private Collection<ISearchProvider> providers = new ArrayList<ISearchProvider>();
	
	public SearchProviderManager() {
		this.execService = Executors.newCachedThreadPool();
	}
	
	void addProvider(ISearchProvider provider) {
		this.providers.add(provider);
	}
	
	public List<ISearchResult> search(ISearchRequest searchRequest) throws InterruptedException, ExecutionException {
		List<ISearchResult> results = new ArrayList<ISearchResult>();
		long timeout = searchRequest.getTimeout();
		
		CompletionService<ISearchResult> execCompletionService = new ExecutorCompletionService<ISearchResult>(this.execService);
		for (ISearchProvider searcher : this.providers) {
			execCompletionService.submit(new SearchProviderCallable<ISearchResult>(searcher, searchRequest));
		}
		
		int n = providers.size();
		for (int i = 0; i < n; ++i) {
			long start = System.currentTimeMillis();
			ISearchResult r = execCompletionService.poll(timeout, TimeUnit.MILLISECONDS) .get();
			
			if (r != null) {
				results.add(r);
			}
			
			long diff = System.currentTimeMillis() - start;
			if ((timeout-=diff)<= 0) break;
		}
		
		return results;
	}
}
