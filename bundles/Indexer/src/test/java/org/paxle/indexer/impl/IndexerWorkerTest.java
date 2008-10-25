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

package org.paxle.indexer.impl;

import java.net.URI;
import java.util.HashSet;

import org.paxle.core.doc.CrawlerDocument;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.ParserDocument;
import org.paxle.core.queue.Command;
import org.paxle.core.queue.ICommand;

import junit.framework.TestCase;

public class IndexerWorkerTest extends TestCase {
	
	public void testDeepConversion() throws Exception {
		final int depth = (int)(Math.random() * 10);
		System.out.println("testing deep conversion with depth " + depth);
		
		
		final HashSet<String> expectedTitles = new HashSet<String>();
		
		final IParserDocument pdoc = new ParserDocument();
		String title = "container";
		expectedTitles.add(title);
		pdoc.setTitle(title);
		pdoc.setMimeType("test/mime-type");
		IParserDocument container = pdoc;
		for (int i=0; i<depth; i++) {
			final IParserDocument subpdoc = new ParserDocument();
			title = "child_depth_" + i;
			expectedTitles.add(title);
			subpdoc.setTitle(title);
			subpdoc.setMimeType("test/mime-type");
			container.addSubDocument("child_" + i, subpdoc);
			container = subpdoc;
		}
		
		final ICommand cmd = new Command();
		cmd.setLocation(URI.create("http://www.example.org/"));
		cmd.setResult(ICommand.Result.Passed);
		final ICrawlerDocument cdoc = new CrawlerDocument();
		cdoc.setLocation(cmd.getLocation());
		cdoc.setStatus(ICrawlerDocument.Status.OK);
		cmd.setCrawlerDocument(cdoc);
		pdoc.setStatus(IParserDocument.Status.OK);
		cmd.setParserDocument(pdoc);
		
		final IndexerWorker iw = new IndexerWorker();
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