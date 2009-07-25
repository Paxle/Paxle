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
package org.paxle.indexer.impl;

import java.io.File;
import java.net.URI;
import java.util.HashSet;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.impl.BasicCommand;
import org.paxle.core.doc.impl.BasicDocumentFactory;
import org.paxle.core.io.temp.impl.TempFileManager;

public class IndexerWorkerTest extends MockObjectTestCase {
	private static final File TESTFILE = new File("src/test/resources/paxle.txt");
	private IDocumentFactory docFactory;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.docFactory = new BasicDocumentFactory(){{
			this.tempFileManager = new TempFileManager();
		}};
	}
	
	public void testDeepConversion() throws Exception {
		final int depth = (int)(Math.random() * 10);
		System.out.println("testing deep conversion with depth " + depth);
		
		
		final HashSet<String> expectedTitles = new HashSet<String>();
		
		final IParserDocument pdoc = this.docFactory.createDocument(IParserDocument.class);
		String title = "container";
		expectedTitles.add(title);
		pdoc.setTitle(title);
		pdoc.setMimeType("test/mime-type");
		pdoc.setTextFile(TESTFILE);
		IParserDocument container = pdoc;
		for (int i=0; i<depth; i++) {
			final IParserDocument subpdoc = this.docFactory.createDocument(IParserDocument.class);
			title = "child_depth_" + i;
			expectedTitles.add(title);
			subpdoc.setTitle(title);
			subpdoc.setMimeType("test/mime-type");
			container.addSubDocument("child_" + i, subpdoc);
			container = subpdoc;
		}
		
		final ICommand cmd = new BasicCommand();
		cmd.setLocation(URI.create("http://www.example.org/"));
		cmd.setResult(ICommand.Result.Passed);
		
		final ICrawlerDocument cdoc = this.docFactory.createDocument(ICrawlerDocument.class);
		cdoc.setLocation(cmd.getLocation());
		cdoc.setStatus(ICrawlerDocument.Status.OK);
		cmd.setCrawlerDocument(cdoc);
		pdoc.setStatus(IParserDocument.Status.OK);
		cmd.setParserDocument(pdoc);
		
		final IndexerWorker iw = new IndexerWorker(this.docFactory);
		iw.execute(cmd);
		iw.destroy();
		
		assertEquals(ICommand.Result.Passed, cmd.getResult());
		assertNotNull(cmd.getIndexerDocuments());
		assertEquals(depth + 1, cmd.getIndexerDocuments().length);
		
		for (final IIndexerDocument idoc : cmd.getIndexerDocuments()) {
			title = idoc.get(IIndexerDocument.TITLE);
			assertNotNull(title);
			assertTrue(title, expectedTitles.remove(title));
		}
		assertEquals(0, expectedTitles.size());
	}
}
