package org.paxle.se.search;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ISearchProviderManager {
	public List<ISearchResult> search(String paxleQuery, int maxResults, long timeout) throws
			InterruptedException, ExecutionException;
	public void search(String paxleQuery, int maxResults, long timeout, ISearchResultCollector collector) throws
			InterruptedException, ExecutionException;
}
