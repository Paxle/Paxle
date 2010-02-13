/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
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

@Component
@Service(ISearchProviderManager.class)
@Reference(
	name="searchProviders",
	referenceInterface=ISearchProvider.class,
	cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
	policy=ReferencePolicy.DYNAMIC,
	bind="addProvider",
	unbind="removeProvider",
	target="(service.pid=*)"
)
public class SearchProviderManager implements ISearchProviderManager {
	private final AtomicInteger requestIdGenerator = new AtomicInteger(0);
	
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
	private final LinkedHashMap<String,ServiceReference> providersRefs = new LinkedHashMap<String, ServiceReference>();

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
		String providerPID = (String) providerRef.getProperty(Constants.SERVICE_PID);
		
		this.providersRefs.put(providerPID, providerRef);
		this.logger.info(String.format(
				"Search provider with PID '%s' from bundle '%s' registered.",
				providerPID,
				providerRef.getBundle().getSymbolicName()
		));
	}

	protected void removeProvider(ServiceReference providerRef) {
		String providerPID = (String) providerRef.getProperty(Constants.SERVICE_PID);
		this.providersRefs.remove(providerPID);
		
		this.logger.info(String.format(
				"Search provider with PID '%s' from bundle %s unregistered.",
				providerPID,
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
	
	public List<ISearchResult> search(ISearchRequest request) throws InterruptedException, ExecutionException, SearchException {
		final ListResultCollector collector = this.createResultCollector();
		this.search(request, collector);
		return collector;
	}

	public List<ISearchResult> search(
			String paxleQuery,
			int maxResults,
			long timeout
	) throws InterruptedException, ExecutionException, SearchException {
		final ListResultCollector collector = this.createResultCollector();
		this.search(paxleQuery, maxResults, timeout, collector);
		return collector;
	}
	
	public void search(
			String paxleQuery,
			int maxResults,
			long timeout,
			ISearchResultCollector results
	) throws InterruptedException, ExecutionException, SearchException {
		ISearchRequest request = this.createRequest(paxleQuery, maxResults, timeout);
		this.search(request, results);
	}
	
	public ISearchRequest createRequest(String paxleQuery, int maxResults, long timeout) throws SearchException {
		if (paxleQuery == null) throw new NullPointerException("The search-query was null");
		if (maxResults <= 0) throw new IllegalArgumentException("The maxResult value must be greater than zero.");
		if (timeout < 0) throw new IllegalArgumentException("The timeout must not be negative.");
		
		// parsing the query-string into tokens
		final AToken query = PaxleQueryParser.parse(paxleQuery);
		if (query == null) throw new SearchException(paxleQuery, "Illegal query (maybe too short?)");
		
		// creating the search request object
		int requestID = this.requestIdGenerator.incrementAndGet();
		ISearchRequest searchRequest = new SearchRequest(requestID, query, maxResults, timeout);		
		return searchRequest;
	}
	
	private ListResultCollector createResultCollector() {
		return new ListResultCollector();
	}

	private void search(
			ISearchRequest request,
			ISearchResultCollector results
	) throws InterruptedException, ExecutionException, SearchException {
		if (request == null) throw new NullPointerException("The search-request object must not be null");
		
		final CompletionService<ISearchResult> execCompletionService = new ExecutorCompletionService<ISearchResult>(this.execService);

		// determining all search-providers that should be used for the query
		HashSet<String> allowedProviderPIDs = new HashSet<String>(request.getProviderIDs());
		
		// loop through all providers and pass the request to each one
		List<String> usedProviderPIDs = new ArrayList<String>();
		for(Entry<String, ServiceReference> providerEntry : this.providersRefs.entrySet()) {
			final String providerPID = providerEntry.getKey();
			final ServiceReference providerRef = providerEntry.getValue();
			
			if (allowedProviderPIDs.size() > 0 && !allowedProviderPIDs.contains(providerPID)) {
				this.logger.debug(String.format(
						"SEProvider '%s' is skipped for search request '%d'.",
						providerPID,
						Integer.valueOf(request.getRequestID())
				));
				continue;
			}
			
			usedProviderPIDs.add(providerPID);
			execCompletionService.submit(new SearchProviderCallable(this.ctx, providerRef, request));
		}
		
		if (allowedProviderPIDs.size() == 0) {
			// store the providers we have used to process the search-request
			request.setProviderIDs(usedProviderPIDs);
		}
		
		// loop through all providers and collect the results
		long searchTimeout = request.getTimeout();
		for (int i = 0; i < usedProviderPIDs.size(); ++i) {
			final long start = System.currentTimeMillis();

			// waiting for the next search result
			final Future<ISearchResult> future = execCompletionService.poll(searchTimeout, TimeUnit.MILLISECONDS);
			if (future != null) {
				final ISearchResult r = future.get();

				if (r != null) {
					final String providerPID = r.getProviderID();
					final int size = r.getSize();
					this.logger.debug(String.format(
							"SEProvider '%s' returned '%d' results for search-request '%d'.",
							providerPID,
							Integer.valueOf(size),
							Integer.valueOf(request.getRequestID())
					));					
					
					results.collect(r);
				}
			}

			final long diff = System.currentTimeMillis() - start;
			if ((searchTimeout-=diff)<= 0) break;
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
