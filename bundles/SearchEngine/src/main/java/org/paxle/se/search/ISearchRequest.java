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

import org.osgi.framework.Constants;
import org.paxle.se.query.tokens.AToken;

public interface ISearchRequest {
	
	public long getTimeout();
	public AToken getSearchQuery();
	public int getMaxResultCount();
	
	public int getRequestID();
	
	/* TODO: other request metadata not directly available in the query string, e.g.
	 * - ranking parameter?
	 */
	
	/**
	 * @return the {@link Constants#SERVICE_PID IDs} of all {@link ISearchProvider providers} that have processed the search request
	 */
	public List<String> getProviderIDs();
	
	/**
	 * This function can be used to restrict the {@link ISearchProvider providers} that should be used to
	 * process a {@link ISearchRequest search-request} to a given list of providers. 
	 * 
	 * @param providerIDs the {@link Constants#SERVICE_PID IDs} of all providers that should be used to process the search request.
	 * If the list is empty or <code>null</code> all providers registered to the system are used.
	 */
	public void setProviderIDs(List<String> providerIDs);	
}
