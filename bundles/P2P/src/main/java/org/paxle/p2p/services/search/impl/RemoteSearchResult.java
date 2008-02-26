package org.paxle.p2p.services.search.impl;

import java.util.Collection;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.search.ISearchResult;

public class RemoteSearchResult implements ISearchResult {
	
	private final IIndexerDocument[] result;
	private final long searchTime;
	
	public RemoteSearchResult(Collection<IIndexerDocument> results, long searchTime) {
		this(results.toArray(new IIndexerDocument[results.size()]), searchTime);
	}
	
	public RemoteSearchResult(IIndexerDocument[] results, long searchTime) {
		this.result = results;
		this.searchTime = searchTime;
	}
	
	public IIndexerDocument[] getResult() {
		return this.result;
	}
	
	public long getSearchTime() {
		return this.searchTime;
	}
}