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

package org.paxle.se.search;

import org.osgi.framework.Constants;
import org.paxle.core.doc.IIndexerDocument;

public interface ISearchResult {	
	public IIndexerDocument[] getResult();
	public int getSize();
	public long getSearchTime();
	
	/**
	 * Returns the systemwidth unique {@link ISearchProvider search-provider}-ID.
	 * This ID can then be used, e.g. to get {@link org.paxle.core.metadata.IMetaData} 
	 * about the search-provider
	 * 
	 * @return the {@link ISearchProvider provider}-ID
	 * @see Constants#SERVICE_PID
	 */
	public String getProviderID();
	
	/* TODO: get search result metadata such as 
	 * - the name of the provider
	 * - total number of results
	 * - ranking?
	 */
}
