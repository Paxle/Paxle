package org.paxle.se;

import java.util.List;

import org.paxle.se.search.ISearchResult;

public interface ISearchEngine {
	
	public abstract List<ISearchResult> doSearch(String paxleQuery, int count);
}
