/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.se.search.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.ISearchResult;

public class SearchProviderCallable implements Callable<ISearchResult> {
	
	private final Log logger;
	private final List<IIndexerDocument> results = new ArrayList<IIndexerDocument>();
	private final ISearchProvider provider;
	private final ISearchRequest searchRequest;
	
	public SearchProviderCallable(ISearchProvider provider, ISearchRequest searchRequest) {
		if (provider == null) throw new NullPointerException("Search provider was null.");
		if (searchRequest == null) throw new NullPointerException("Search-request was null.");
		
		this.provider = provider;
		this.searchRequest = searchRequest;
		this.logger = LogFactory.getLog(getClass().getSimpleName() + "_" + provider.getClass().getSimpleName());
	}
	
	public ISearchResult call() throws Exception {
		final long start = System.currentTimeMillis();
		AToken query = null;
		try {
			query = this.searchRequest.getSearchQuery();
			this.logger.info("starting search for '" + query + "' (" + this.searchRequest.getSearchQuery().toString() + ")");			
			this.provider.search(
					query, 
					this.results, 
					this.searchRequest.getMaxResultCount(), 
					this.searchRequest.getTimeout()
			);
		} catch (InterruptedException e) { 
			/* just fall through */ 
		} catch (Exception e) {
			this.logger.error(String.format("Unexpected '%s' while performing a search for '%s' with provider '%s'.",
					e.getClass().getName(),
					query,
					this.provider.getClass().getName()
			),e);
		}
		return new SearchResult(this.results, System.currentTimeMillis() - start);
	}
}
