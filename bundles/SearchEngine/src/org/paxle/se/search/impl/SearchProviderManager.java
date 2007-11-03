package org.paxle.se.search.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.query.PaxleQueryParser;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchProviderManager;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.ISearchResult;
import org.paxle.se.search.ISearchResultCollector;

public class SearchProviderManager implements ISearchProviderManager {
	
	private final ExecutorService execService;
	private final List<ISearchProvider> providers = new ArrayList<ISearchProvider>();
	private final PaxleQueryParser pqp = new PaxleQueryParser();
	private final Log logger = LogFactory.getLog(SearchProviderManager.class);
	
	public SearchProviderManager() {
		this.execService = Executors.newCachedThreadPool();
	}
	
	Integer addProvider(ISearchProvider provider) {
		final int ret = this.providers.size();
		
		ITokenFactory tokenFactory = provider.getTokenFactory();
		if (tokenFactory == null) {
			this.logger.error(String.format("Search-provider '%s' does not provide a TokenFactory.",provider.getClass().getName()));
			return null;
		} else {
			this.logger.info("added search provider: " + provider.getClass().getName());
			this.providers.add(provider);
			this.pqp.addTokenFactory(tokenFactory);
			
			// return provider number
			return ret;
		}
	}
	
	void removeProvider(int number) {
		final ISearchProvider provider = this.providers.remove(number);
		this.logger.info("removed search provider: " + provider.getClass().getName());
		this.pqp.removeTokenFactory(number);
	}
		
	/**
	 * @see ISearchProviderManager#getSearchProviders()
	 */
	public Collection<ISearchProvider> getSearchProviders() {
		return new ArrayList<ISearchProvider>(this.providers);
	}
	
	public void shutdown() throws IOException {
		this.logger.info("search provider manager is shutting down...");
		this.logger.debug("waiting for searches to finish");
		this.execService.shutdown();
		this.logger.debug("searches finished, cleaning up...");
		this.providers.clear();
		this.pqp.clearTokenFactories();
		this.logger.info("shutdown complete");
	}
	
	public List<ISearchResult> search(String paxleQuery, int maxResults, long timeout) throws InterruptedException, ExecutionException {
		final ListResultCollector collector = new ListResultCollector();
		search(paxleQuery, maxResults, timeout, collector);
		return collector;
	}
	
	public void search(String paxleQuery, int maxResults, long timeout, ISearchResultCollector results) throws InterruptedException, ExecutionException {
		final CompletionService<ISearchResult> execCompletionService = new ExecutorCompletionService<ISearchResult>(this.execService);
		
		final List<AToken> queries = this.pqp.parse(paxleQuery);
		
		int n = providers.size();
		for (int i=0; i<n; i++) {
			final ISearchRequest searchRequest = new SearchRequest(queries.get(i), maxResults, timeout);
			execCompletionService.submit(new SearchProviderCallable(this.providers.get(i), searchRequest));
		}
		
		for (int i = 0; i < n; ++i) {
			final long start = System.currentTimeMillis();
			
			final Future<ISearchResult> future = execCompletionService.poll(timeout, TimeUnit.MILLISECONDS);
			if (future != null) {
				final ISearchResult r = future.get();
				
				if (r != null) {
					results.collect(r);
				}
			}
			
			final long diff = System.currentTimeMillis() - start;
			if ((timeout-=diff)<= 0) break;
		}
	}
}
