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

package org.paxle.parser.feed.impl;

import java.io.File;
import java.net.URI;

import org.paxle.core.doc.IParserDocument;
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
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// the feed contains html code
		this.mimeTypeToParserMap.put("text/html",new HtmlParser());
		
		// create the parser(s)
		this.parser = new FeedParser();	
	}
	
	public void testParseRss() throws Exception {
		final URI location = URI.create("http://www.virtualdub.org/");	
		final File testFile = new File(resourcesDir, "rss.xml");
		assertTrue(testFile.exists());
		
		final IParserDocument pdoc = this.parser.parse(location, null, testFile);
		assertNotNull(pdoc);
		assertEquals(IParserDocument.Status.OK, pdoc.getStatus());
		assertEquals("VirtualBlog", pdoc.getTitle());
		
		assertNotNull(pdoc.getSubDocs());
		assertEquals(2, pdoc.getSubDocs().size());
	}
}
