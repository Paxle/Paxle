
package org.paxle.se.search;

import java.io.IOException;
import java.util.List;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.query.tokens.AToken;

public interface ISearchProvider {
	
	/**
	 * 
	 * @param request the query string
	 * @param results a list where the search result items should be appended to
	 * @param maxCount the maximum amount of result items that should be returned back
	 * @param timeout the timeout to use
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void search(AToken request, List<IIndexerDocument> results, int maxCount, long timeout) throws IOException, InterruptedException;
}
