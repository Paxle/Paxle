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
package org.paxle.se.search;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public interface ISearchProviderManager {
	/**
	 * @return the list of installed {@link ISearchProvider search-providers}
	 */
	public Map<String, ISearchProvider> getSearchProviders();
	
	/**
	 * @return a list of known but disabled search-providers
	 */
	public Set<String> disabledProviders();
	
	/**
	 * Disable a search provider
	 * @param providerName the classname of the provider
	 */
	public void enableProvider(String providerName);
	
	/**
	 * Enables a provider
	 * @param providerName the classname of the provider
	 */
	public void disableProvider(String providerName);	
	
	public List<ISearchResult> search(ISearchRequest request) throws InterruptedException, ExecutionException, SearchException;
	public List<ISearchResult> search(String paxleQuery, int maxResults, long timeout) throws InterruptedException, ExecutionException, SearchException;
	public void search(String paxleQuery, int maxResults, long timeout, ISearchResultCollector collector) throws InterruptedException, ExecutionException, SearchException;

	/**
	 * Function to create a new search-request object. The result of this function call can be used to start a new search
	 * via {@link #search(ISearchRequest)}. 
	 */
	public ISearchRequest createRequest(String paxleQuery, int maxResults, long timeout) throws SearchException;
}
