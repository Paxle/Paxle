package org.paxle.se.search;

import java.io.IOException;
import java.util.List;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.query.ITokenFactory;

public interface ISearchProvider {
	
	public void search(String request, List<IIndexerDocument> results, int maxCount) throws IOException;
	public ITokenFactory getTokenFactory();
}
