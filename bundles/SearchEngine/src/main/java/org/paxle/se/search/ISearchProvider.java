/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
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
