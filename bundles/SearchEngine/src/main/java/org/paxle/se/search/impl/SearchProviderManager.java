
package org.paxle.se.search.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.prefs.Properties;
import org.paxle.se.query.PaxleQueryParser;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchProviderManager;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.ISearchResult;
import org.paxle.se.search.ISearchResultCollector;
import org.paxle.se.search.SearchException;

public class SearchProviderManager implements ISearchProviderManager {
	private static final String DISABLED_PROVIDERS = ISearchProviderManager.class.getName() + "." + "disabledProviders";	
	
	private final ExecutorService execService;
	private final LinkedHashMap<Long,ISearchProvider> providers = new LinkedHashMap<Long, ISearchProvider>();
	
	/**
	 * for logging
	 */
	private final Log logger = LogFactory.getLog(SearchProviderManager.class);
	
	/**
	 * The names of all currently disabled providers
	 */
	private Set<String> disabledProviders = new HashSet<String>();
	
	/**
	 * The properties of this component
	 */
	private Properties props = null;
	
	public SearchProviderManager(Properties props) {
		this.execService = Executors.newCachedThreadPool();
		
		this.props = props;
		if (this.props != null && this.props.containsKey(DISABLED_PROVIDERS)) {
			this.disabledProviders = this.props.getSet(DISABLED_PROVIDERS);
		}		
	}
	
	synchronized void addProvider(Long providerID, ISearchProvider provider) {
		if (providerID == null) throw new NullPointerException("The provider-ID is null");
		if (provider == null) throw new NullPointerException("The provider is null");
		if (this.providers.containsKey(providerID)) {
			String format = String.format("Unable to register se-provider with ID '%s'. Provider already registered.", providerID.toString());
			this.logger.error(format);
		}
		
		this.logger.info("added search provider: " + provider.getClass().getName());
		this.providers.put(providerID, provider);
	}
	
	synchronized void removeProvider(Long providerID) {
		if (providerID == null) throw new NullPointerException("The provider-ID is null");
		if (!this.providers.containsKey(providerID)) {
			String format = String.format("Unable to unregister se-provider with ID '%s'. Provider unknown.", providerID.toString());
			this.logger.error(format);
			return;
		}
		
		final ISearchProvider provider = this.providers.remove(providerID);
		this.logger.info("removed search provider: " + provider.getClass().getName());
		// this.pqp.removeTokenFactory(number);
	}
	
	/**
	 * @see ISearchProviderManager#getSearchProviders()
	 */
	public Collection<ISearchProvider> getSearchProviders() {
		return Collections.unmodifiableCollection(this.providers.values());
	}
	
	public void shutdown() throws IOException {
		this.logger.info("search provider manager is shutting down...");
		this.logger.debug("waiting for searches to finish");
		this.execService.shutdown();
		this.logger.debug("searches finished, cleaning up...");
		this.providers.clear();
		// this.pqp.clearTokenFactories();
		this.logger.info("shutdown complete");
	}
	
	public List<ISearchResult> search(String paxleQuery,
			int maxResults,
			long timeout) throws InterruptedException, ExecutionException, SearchException {
		final ListResultCollector collector = new ListResultCollector();
		search(paxleQuery, maxResults, timeout, collector);
		return collector;
	}
	
	public void search(String paxleQuery,
			int maxResults,
			long timeout,
			ISearchResultCollector results) throws InterruptedException, ExecutionException, SearchException {
		final CompletionService<ISearchResult> execCompletionService = new ExecutorCompletionService<ISearchResult>(this.execService);
		
		// final List<AToken> queries = this.pqp.parse(paxleQuery);
		final AToken query = PaxleQueryParser.parse(paxleQuery);
		if (query == null)
			throw new SearchException(paxleQuery, "Illegal query (maybe too short?)");
		
		int n = providers.size();
		
		int usedProviders = 0;
		for (int i=0; i<n; i++) {
			ISearchProvider provider = this.providers.get(i);
			String providerClassName = provider.getClass().getName();
			if (disabledProviders.contains(providerClassName)) {
				this.logger.debug(String.format("Skipping disabled provider %s ...", providerClassName));
				continue;
			}
			
			// final ISearchRequest searchRequest = new SearchRequest(queries.get(i), maxResults, timeout);
			final ISearchRequest searchRequest = new SearchRequest(query, maxResults, timeout);
			execCompletionService.submit(new SearchProviderCallable(provider, searchRequest));
			
			usedProviders++;
		}
		
		for (int i = 0; i < usedProviders; ++i) {
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

	/**
	 * @see ISearchProviderManager#disableProvider(String)
	 */
	public void disableProvider(String providerName) {
		this.disabledProviders.add(providerName);
		if (this.props != null) this.props.setSet(DISABLED_PROVIDERS, this.disabledProviders);
	}

	/**
	 * @see ISearchProviderManager#enableProvider(String)
	 */
	public void enableProvider(String providerName) {
		this.disabledProviders.remove(providerName);		
		if (this.props != null) this.props.setSet(DISABLED_PROVIDERS, this.disabledProviders);
	}

	/**
	 * @see ISearchProviderManager#disabledProviders()
	 */
	public Set<String> disabledProviders() {
		return Collections.unmodifiableSet(this.disabledProviders);
	}
}
