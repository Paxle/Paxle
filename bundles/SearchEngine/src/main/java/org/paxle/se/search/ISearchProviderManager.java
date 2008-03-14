package org.paxle.se.search;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public interface ISearchProviderManager {
	/**
	 * @return the list of installed {@link ISearchProvider search-providers}
	 */
	public Collection<ISearchProvider> getSearchProviders();
	
	/**
	 * @return a list of known but disabled search-providers
	 */
	public Set<String> disabledProviders();
	
	/**
	 * Disable a search provider
	 * @param providerName the classname of the provider
	 */
	public void enableProvider(String providerName);
	
	/**
	 * Enables a provider
	 * @param providerName the classname of the provider
	 */
	public void disableProvider(String providerName);	
	
	public List<ISearchResult> search(String paxleQuery, int maxResults, long timeout) throws
			InterruptedException, ExecutionException;
	public void search(String paxleQuery, int maxResults, long timeout, ISearchResultCollector collector) throws
			InterruptedException, ExecutionException;
}
