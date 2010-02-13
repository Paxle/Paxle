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

package org.paxle.se.index.lucene.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.query.PaxleQueryParser;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.SearchProviderContext;
import org.paxle.se.search.impl.SearchProviderContextLocal;
import org.paxle.se.search.impl.SearchRequest;

public class LuceneSearcherTest extends ALuceneTest {
		
	protected LuceneSearcher searcher;
	
	@Override
	protected void initDataDirectory() throws IOException {
		// create data directory
		final File targetetDir = new File(DATA_PATH + "/lucene-db");
		if (targetetDir.exists()) {
			FileUtils.deleteQuietly(targetetDir);
		}
		assertTrue(targetetDir.mkdirs());
		
		// copy the following directory to target
		final File sourceDir = new File("src/test/resources/lucene-db");
		FileUtils.copyDirectory(sourceDir, targetetDir);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// init a dummy search-provider context
		SearchProviderContext.setThreadLocal(new SearchProviderContextLocal(){
			@SuppressWarnings("unchecked")
			@Override
			protected <DOC> DOC createDocumentForInterface(Class<DOC> docInterface, String filter) throws InvalidSyntaxException, IOException {
				if (docInterface.isAssignableFrom(IIndexerDocument.class)) {
					return (DOC) docFactory.createDocument(IIndexerDocument.class);
				}
				throw new IllegalArgumentException("Unexpected invocation");
			}
		});
		
		// init searcher
		this.searcher = new LuceneSearcher(){{
			this.manager = LuceneSearcherTest.this.lmanager;
			this.stopWordsManager = LuceneSearcherTest.this.stopwordsManager;
			this.activate(null);
		}};
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		// stopping lucene-writer and -manager
		this.lmanager.deactivate();
		
		// delete files
		FileUtils.deleteDirectory(new File(LuceneWriterTest.DATA_PATH));
	}	
	
	public void testSearch() throws IOException, InterruptedException {
		assertEquals(1, this.lmanager.getDocCount());
		
		final AToken query = PaxleQueryParser.parse("Paxle");
		final ISearchRequest request = new SearchRequest(0,query, 10, 10000);
		final ArrayList<IIndexerDocument> results = new ArrayList<IIndexerDocument>();
		
		this.searcher.search(request, results);
		assertEquals(1, results.size());
		
		final IIndexerDocument doc = results.get(0);
		assertNotNull(doc);
		assertNotNull(doc.getFields());
		assertEquals("Paxle - PAXLE Search Framework", doc.get(IIndexerDocument.TITLE));
		assertEquals("http://www.paxle.net/en/start", doc.get(IIndexerDocument.LOCATION));
		assertEquals(Long.valueOf(11126l), doc.get(IIndexerDocument.SIZE));
		
		for (Map.Entry<Field<?>,Object> entry : doc)  {
			System.out.println(entry.getKey().getName() + ": " + entry.getValue());
		}
	}
}
