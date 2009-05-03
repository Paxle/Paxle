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
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.ISearchResult;
import org.paxle.se.search.SearchProviderContext;

public class SearchProviderCallable implements Callable<ISearchResult> {
	
	/**
	 * For logging
	 */
	private final Log logger;
	
	/**
	 * A list of search-results returned by the {@link #provider}
	 */
	private final List<IIndexerDocument> results = new ArrayList<IIndexerDocument>();
	
	/**
	 * The context of this component
	 */
	protected ComponentContext ctx;		
	
	/**
	 * A reference to the {@link ISearchProvider} that should be used by this {@link Callable}
	 */
	private final ServiceReference providerRef;
	
	/**
	 * The search-request
	 */
	private final ISearchRequest searchRequest;
	
	public SearchProviderCallable(ComponentContext ctx, ServiceReference providerRef, ISearchRequest searchRequest) {
		if (providerRef == null) throw new NullPointerException("Search provider-reference was null.");
		if (searchRequest == null) throw new NullPointerException("Search-request was null.");
		
		this.ctx = ctx;
		this.providerRef = providerRef;
		this.searchRequest = searchRequest;
		this.logger = LogFactory.getLog(getClass().getSimpleName());
	}
	
	public ISearchResult call() throws Exception {
		final long start = System.currentTimeMillis();
		
		String providerID = null;
		AToken query = null;
		try {
			// the search query to use
			query = this.searchRequest.getSearchQuery();
			
			// the provider to use
			final ISearchProvider provider = (ISearchProvider) this.ctx.getBundleContext().getService(this.providerRef);
			
			// the provider-ID (may be used to fetch additional metadata)
			providerID = (String) this.providerRef.getProperty(Constants.SERVICE_PID);
			
			this.logger.info("Starting search for '" + query + "' (" + this.searchRequest.getSearchQuery().toString() + ")");
			provider.search(
					this.searchRequest, 
					this.results
			);
		} catch (InterruptedException e) { 
			/* just fall through */ 
		} catch (Exception e) {
			this.logger.error(String.format(
					"Unexpected '%s' while performing a search for '%s' using provider '%d'.",
					e.getClass().getName(),
					query,
					providerID
			),e);
		} finally {
			// context cleanup
			SearchProviderContext.removeCurrentContext();
		}
		return new SearchResult(providerID, this.results, System.currentTimeMillis() - start);
	}
}
