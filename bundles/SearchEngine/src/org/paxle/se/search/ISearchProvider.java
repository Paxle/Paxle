package org.paxle.se.search;

import java.util.List;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.query.ITokenFactory;

public interface ISearchProvider {
	
	public void search(String request, List<IIndexerDocument> results, int maxCount);
	public ITokenFactory getTokenFactory();
}
