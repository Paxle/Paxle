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

import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchRequest;

public class SearchRequest implements ISearchRequest {
	/**
	 * FIXME: unique ID of the request within the session (not used yet)
	 */
	private final int requestID = 0;
	
	/**
	 * FIXME: unique ID of search session (not used yet)
	 */
	private final int sessionID = 0;
	
	private final int offset = 0;
	private final int maxResults;
	private final long timeout;
	private AToken query;
	
	public SearchRequest(AToken query, int maxResults, long timeout) {
		this.maxResults = maxResults;
		this.query = query;
		this.timeout = timeout;
	}
	
	public int getMaxResultCount() {
		return this.maxResults;
	}
	
	public AToken getSearchQuery() {
		return this.query;
	}
	
	public long getTimeout() {
		return this.timeout;
	}
}
