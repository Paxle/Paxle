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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.paxle.se.query.PaxleQueryParser;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchProviderManager;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.ISearchResult;
import org.paxle.se.search.ISearchResultCollector;
import org.paxle.se.search.SearchException;

/**
 * @scr.component
 * @scr.service interface="org.paxle.se.search.ISearchProviderManager"
 * @scr.reference name="searchProviders" 
 * 				  interface="org.paxle.se.search.ISearchProvider" 
 * 				  cardinality="0..n" 
 * 				  policy="dynamic" 
 * 				  bind="addProvider" 
 * 				  unbind="removeProvider"
 * 				  target="(service.pid=*)
 */
public class SearchProviderManager implements ISearchProviderManager {
	/**
	 * Thread pool service
	 */
	private ExecutorService execService;

	/**
	 * The context of this component
	 */
	protected ComponentContext ctx;	
	
	/**
	 * A list of all currently registered {@link ISearchProvider search-providers}
	 */
	private final LinkedHashMap<Long,ServiceReference> providersRefs = new LinkedHashMap<Long, ServiceReference>();

	/**
	 * for logging
	 */
	private final Log logger = LogFactory.getLog(SearchProviderManager.class);

	protected void activate(ComponentContext context) {
		this.ctx = context;
		this.execService = Executors.newCachedThreadPool();
	}
	
	protected void deactivate(ComponentContext context ){
		this.logger.info("Search provider manager going to be deactivated now ...");
		this.logger.debug("Waiting for searches to finish");
		this.execService.shutdown();
		this.logger.debug("Searches finished, cleaning up...");
		this.logger.info("shutdown complete");
	}
	
	
	protected void addProvider(ServiceReference providerRef) {
		Long providerID = (Long) providerRef.getProperty(Constants.SERVICE_ID);
		
		this.providersRefs.put(providerID, providerRef);
		this.logger.info(String.format(
				"Search provider with ID '%d' from bundle '%s' registered.",
				providerID,
				providerRef.getBundle().getSymbolicName()
		));
	}

	protected void removeProvider(ServiceReference providerRef) {
		Long providerID = (Long) providerRef.getProperty(Constants.SERVICE_ID);
		this.providersRefs.remove(providerID);
		
		this.logger.info(String.format(
				"Search provider with ID %d from bundle %s unregistered.",
				providerID,
				providerRef.getBundle().getSymbolicName()
		));
	}

	/**
	 * @see ISearchProviderManager#getSearchProviders()
	 */
	public Map<String,ISearchProvider> getSearchProviders() {
		final HashMap<String,ISearchProvider> providers = new HashMap<String, ISearchProvider>();
		for (ServiceReference proproviderRef : this.providersRefs.values()) {
			final ISearchProvider provider = (ISearchProvider) this.ctx.locateService("searchProviders", proproviderRef);
			final String providerPID = (String) proproviderRef.getProperty(Constants.SERVICE_PID);			
			providers.put(providerPID, provider);
		}
		return providers;
	}

	public List<ISearchResult> search(
			String paxleQuery,
			int maxResults,
			long timeout
	) throws InterruptedException, ExecutionException, SearchException {
		final ListResultCollector collector = new ListResultCollector();
		this.search(paxleQuery, maxResults, timeout, collector);
		return collector;
	}

	public void search(
			String paxleQuery,
			int maxResults,
			long timeout,
			ISearchResultCollector results
	) throws InterruptedException, ExecutionException, SearchException {
		final CompletionService<ISearchResult> execCompletionService = new ExecutorCompletionService<ISearchResult>(this.execService);

		// final List<AToken> queries = this.pqp.parse(paxleQuery);
		final AToken query = PaxleQueryParser.parse(paxleQuery);
		if (query == null)
			throw new SearchException(paxleQuery, "Illegal query (maybe too short?)");

		int n = providersRefs.size();

		int usedProviders = 0;
		if (n > 0) {
			for(ServiceReference providerRef : this.providersRefs.values()) {
				// final ISearchRequest searchRequest = new SearchRequest(queries.get(i), maxResults, timeout);
				final ISearchRequest searchRequest = new SearchRequest(query, maxResults, timeout);
				execCompletionService.submit(new SearchProviderCallable(this.ctx, providerRef, searchRequest));
				usedProviders++;
			}
		}
		for (int i = 0; i < usedProviders; ++i) {
			final long start = System.currentTimeMillis();

			final Future<ISearchResult> future = execCompletionService.poll(timeout, TimeUnit.MILLISECONDS);
			if (future != null) {
				final ISearchResult r = future.get();

				if (r != null) {
					results.collect(r);
				}
			}

			final long diff = System.currentTimeMillis() - start;
			if ((timeout-=diff)<= 0) break;
		}
	}

	/**
	 * @see ISearchProviderManager#disableProvider(String)
	 */
	public void disableProvider(String providerName) {
		// TODO: needs to be re-implemented
	}

	/**
	 * @see ISearchProviderManager#enableProvider(String)
	 */
	public void enableProvider(String providerName) {
		// TODO: needs to be re-implemented
	}

	/**
	 * @see ISearchProviderManager#disabledProviders()
	 */
	public Set<String> disabledProviders() {
		return Collections.emptySet();
	}
}
