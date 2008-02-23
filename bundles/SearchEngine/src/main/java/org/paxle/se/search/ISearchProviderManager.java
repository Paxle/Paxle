package org.paxle.se.search;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ISearchProviderManager {
	/**
	 * @return the list of installed {@link ISearchProvider search-providers}
	 */
	public Collection<ISearchProvider> getSearchProviders();
	
	public List<ISearchResult> search(String paxleQuery, int maxResults, long timeout) throws
			InterruptedException, ExecutionException;
	public void search(String paxleQuery, int maxResults, long timeout, ISearchResultCollector collector) throws
			InterruptedException, ExecutionException;
}
