package org.paxle.se.index;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchProvider;

public interface IIndexSearcher extends ISearchProvider, Closeable {
	
	/**
	 * @see ISearchProvider#search(String, List, int, long)
	 */
	public void search(AToken request, List<IIndexerDocument> results, int maxCount, long timeout) throws IOException, InterruptedException;
	
	public int getDocCount() throws IOException;
	
	/**
	 * @see Closeable#close()
	 */
	public void close() throws IOException;
}
