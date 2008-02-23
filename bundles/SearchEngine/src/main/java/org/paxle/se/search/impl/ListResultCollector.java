package org.paxle.se.search.impl;

import java.util.ArrayList;

import org.paxle.se.search.ISearchResultCollector;
import org.paxle.se.search.ISearchResult;

public class ListResultCollector extends ArrayList<ISearchResult> implements ISearchResultCollector {
	
	private static final long serialVersionUID = 1L;
	
	public void collect(ISearchResult result) {
		super.add(result);
	}
}
