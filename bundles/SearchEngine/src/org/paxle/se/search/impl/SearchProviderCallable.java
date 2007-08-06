package org.paxle.se.search.impl;

import java.util.concurrent.Callable;

import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.ISearchResult;

public class SearchProviderCallable<V extends ISearchResult> implements Callable<V> {
	
	private ISearchProvider provider = null;
	private ISearchRequest searchRequest = null;
	
	public SearchProviderCallable(ISearchProvider provider, ISearchRequest searchRequest) {
		this.provider = provider;
		this.searchRequest = searchRequest;
	}
	
	public V call() throws Exception {
		return (V) this.provider.search(this.searchRequest);
	}

}
