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

package org.paxle.parser.feed.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ParserException;
import org.paxle.parser.html.impl.HtmlParser;
import org.paxle.parser.impl.AParserTest;

public class FeedParserTest extends AParserTest {
	/**
	 * Directory containing test resources
	 */
	private final File resourcesDir = new File("src/test/resources");	
	
	/**
	 * The parser
	 */
	private FeedParser parser = null;
	private HtmlParser htmlParser = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// init the html-parser
		this.htmlParser = new HtmlParser(){{
			this.activate(null);
		}};
		
		// the feed contains html code
		this.mimeTypeToParserMap.put("text/html",this.htmlParser);
		
		// create the parser(s)
		this.parser = new FeedParser();
		this.parser.setParserContextLocal(this.parserContextLocal);
	}
	
	public void testParseRss() throws Exception {
		// loading test-data
		final URI location = URI.create("http://www.virtualdub.org/");	
		final File testFile = new File(resourcesDir, "rss.xml");
		assertTrue(testFile.exists());
		
		// parsing document
		final IParserDocument pdoc = this.parser.parse(location, null, testFile);
		
		// testing result
		assertNotNull(pdoc);
		assertEquals(IParserDocument.Status.OK, pdoc.getStatus());
		assertEquals("application/rss+xml", pdoc.getMimeType());
		assertEquals("VirtualBlog", pdoc.getTitle());
		
		Map<String,IParserDocument> subDocs = pdoc.getSubDocs();
		assertNotNull(subDocs);
		assertEquals(2, subDocs.size());
		for (IParserDocument subDoc : subDocs.values()) {
			assertEquals("text/html", subDoc.getMimeType());
		}
	}
	
	public void testParseRdf() throws Exception {
		// loading test-data
		final URI location = URI.create("http://www.marginalrevolution.com/marginalrevolution/index.rdf");	
		final File testFile = new File(resourcesDir, "rdf.xml");
		assertTrue(testFile.exists());
		
		// parsing document
		final IParserDocument pdoc = this.parser.parse(location, null, testFile);
		
		// testing result
		assertNotNull(pdoc);
		assertEquals(IParserDocument.Status.OK, pdoc.getStatus());
		assertEquals("application/rdf+xml", pdoc.getMimeType());
		assertEquals("Marginal Revolution", pdoc.getTitle());
		
		Map<String,IParserDocument> subDocs = pdoc.getSubDocs();
		assertNotNull(subDocs);
		assertEquals(14, subDocs.size());
		for (IParserDocument subDoc : subDocs.values()) {
			assertEquals("text/html", subDoc.getMimeType());
		}
	}
	
	public void testAtom() throws UnsupportedEncodingException, ParserException, IOException {
		// loading test-data
		final URI location = URI.create("http://codefreak.de/feed/atom/");	
		final File testFile = new File(resourcesDir, "test.atom");
		assertTrue(testFile.exists());
		
		// parsing document
		final IParserDocument pdoc = this.parser.parse(location, null, testFile);
		
		// testing result
		assertNotNull(pdoc);
	}
}
