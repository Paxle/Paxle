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

package org.paxle.parser.plain.impl;

import java.io.File;
import java.io.Reader;
import java.net.URI;
import java.util.Map;

import junitx.framework.StringAssert;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.parser.impl.AParserTest;

public class PlainParserTest extends AParserTest {
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	private PlainParser parser;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.parser = new PlainParser();
		this.parser.contextLocal = this.getParserContextLocal();
	}
	
	public void _testPlainParser2() throws Exception {		
		final IParserDocument pdoc = this.parser.parse(
				URI.create("http://www.w3.org/Protocols/HTTP/1.1/rfc2616bis/draft-lafon-rfc2616bis-latest.txt"),
				null, 
				new File("src/test/resources/draft-lafon-rfc2616bis-latest.txt")
		);
		assertNotNull(pdoc);
		assertNotNull(pdoc.getLinks());
		
		this.logger.info(pdoc.getTitle());
		assertEquals("grep '://' finds 146 occurrences (are these correct URIs?)", 146, pdoc.getLinks().size());
	}
	
	public void testPlainParser1() throws Exception {
		final IParserDocument pdoc = this.parser.parse(
			new URI("http://www.paxle.net/en/start"), 
			"UTF-8", 
			new File("src/test/resources/paxle.txt"
		));
		assertNotNull(pdoc);
		assertNotNull(pdoc.getTitle());
		assertEquals("What is Paxle?", pdoc.getTitle());
		assertNull(pdoc.getAuthor());
		assertNull(pdoc.getLastChanged());
		assertNull(pdoc.getMimeType());
		assertNull(pdoc.getSummary());
		assertEquals(0,pdoc.getHeadlines().size());
		assertEquals(0,pdoc.getImages().size());
		assertEquals(0,pdoc.getKeywords().size());
		assertEquals(0,pdoc.getLanguages().size());
		assertEquals(0,pdoc.getSubDocs().size());
		
		assertEquals(2,pdoc.getLinks().size());		
		final Map<URI,LinkInfo> links = pdoc.getLinks();
		assertTrue(links.containsKey(URI.create("http://localhost:8080")));
		assertTrue(links.containsKey(URI.create("http://www.paxle.net/en/start#what_is_paxle")));		
		
		Reader reader = pdoc.getTextAsReader();
		assertNotNull(reader);
		String content = IOUtils.toString(reader);
		assertNotNull(content);
		StringAssert.assertStartsWith("Paxle is a framework targeted", content);
		StringAssert.assertContains("Paxle as a crawler for textual content in the web", content);
	}
}
