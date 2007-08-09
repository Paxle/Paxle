package org.paxle.se.index;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.search.ISearchProvider;

public interface IIndexSearcher extends ISearchProvider, Closeable {
	
	public void search(String request, List<IIndexerDocument> results, int maxCount) throws IOException, InterruptedException;
	public ITokenFactory getTokenFactory();
	
	public int getDocCount();
	
	public void close() throws IOException;
}
