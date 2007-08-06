package org.paxle.se.search;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ISearchProviderManager {
	public List<ISearchResult> search(ISearchRequest searchRequest) throws InterruptedException, ExecutionException;
}
