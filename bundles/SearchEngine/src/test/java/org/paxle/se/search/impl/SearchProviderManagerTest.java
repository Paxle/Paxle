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

import java.util.Arrays;

import org.jmock.api.Action;
import org.jmock.integration.junit3.MockObjectTestCase;

public class SearchProviderManagerTest extends MockObjectTestCase {
	
	private SearchProviderManager manager = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		this.manager = new SearchProviderManager();
	}
	
	public static <T> Action addElements(int paramIdx, T... newElements) {
	    return new AddElementsAction<T>(Arrays.asList(newElements), paramIdx);
	}	
	
	public void test() {
		// TODO:
	}
	
//	public void testAddProvider() {
//		// fake a search provider
//		final ISearchProvider sp = mock(ISearchProvider.class);
//		
//		// add it to the manager
//		Long id = new Long(System.currentTimeMillis());
//		this.manager.addProvider(id, sp);
//		
//		// fetch all providers
//		Collection<ISearchProvider> providers = this.manager.getSearchProviders();
//		assertNotNull(providers);
//		assertEquals(1, providers.size());
//		assertSame(sp, providers.toArray()[0]);
//	}
//	
//	public void testRemoveProvider() {
//		// fake a search provider
//		final ISearchProvider sp = mock(ISearchProvider.class);
//		
//		// add it to the manager
//		Long id = new Long(System.currentTimeMillis());
//		this.manager.addProvider(id, sp);
//		
//		// fetch all providers
//		Collection<ISearchProvider> providers = this.manager.getSearchProviders();
//		assertNotNull(providers);
//		assertEquals(1, providers.size());
//		assertSame(sp, providers.toArray()[0]);
//		
//		// remove provider
//		this.manager.removeProvider(id);
//		
//		// fetch all providers
//		providers = this.manager.getSearchProviders();
//		assertNotNull(providers);
//		assertEquals(0, providers.size());
//	}	
//	
//	@SuppressWarnings("unchecked")
//	public void testSearch() throws IOException, InterruptedException, ExecutionException, SearchException {
//		final int maxCount = 10;
//		final long timeout = 10000l;
//		
//		// fake a search provider
//		final ISearchProvider sp = mock(ISearchProvider.class);
//		this.manager.addProvider(new Long(System.currentTimeMillis()), sp);
//				
//		// fake search results
//		final IIndexerDocument doc1 = mock(IIndexerDocument.class,"doc1");
//		final IIndexerDocument doc2 = mock(IIndexerDocument.class,"doc2");
//		
//		checking(new Expectations(){{
//			// we expect that the provider is called once
//			one(sp).search(with(any(AToken.class)), with(any(List.class)),with(equal(maxCount)),with(equal(timeout)));
//			
//			// we append two result elements to the call
//			will(addElements(1, doc1, doc2));
//		}});
//	
//		List<ISearchResult> result = this.manager.search("test", maxCount, timeout);
//		assertNotNull(result);
//		assertEquals(1,result.size());
//		assertNotNull(result.get(0));
//		assertNotNull(result.get(0).getResult());
//		assertEquals(2, result.get(0).getResult().length);
//		assertSame(doc1, result.get(0).getResult()[0]);
//		assertSame(doc2, result.get(0).getResult()[1]);
//	} 
}

