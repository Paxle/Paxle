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
package org.paxle.parser.tika.impl;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

import junitx.framework.StringAssert;

import org.apache.commons.io.IOUtils;
import org.apache.tika.config.TikaConfig;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserException;
import org.paxle.parser.impl.AParserTest;

public class ParserManagerTest extends AParserTest {
	private static final String MIME_TYPE_PDF = "application/pdf";
	private static final String MIME_TYPE_HTML = "text/html";
	
	/**
	 * Directory containing test resources
	 */
	private final File resourcesDir = new File("src/test/resources");	
	
	private ParserManager tikaParser;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// init the tika parser manager
		this.tikaParser = new ParserManager();
		this.tikaParser.setTikaConfig(TikaConfig.getDefaultConfig());
	}
	
	public void testParsePdf() throws UnsupportedEncodingException, ParserException, IOException {
		final String charset = null;
		final URI location = URI.create("http://www.paxle.net/test.pdf");	
		final File testFile = new File(this.resourcesDir, "test.pdf");
		assertTrue(testFile.exists());
		
		// creating a paxle parser
		final ISubParser parser = this.tikaParser.createPaxleParser(MIME_TYPE_PDF);
		
		// parsing the data
		final IParserDocument pdoc = parser.parse(location, charset, testFile);
		assertNotNull(pdoc);
		assertEquals(IParserDocument.Status.OK, pdoc.getStatus());
		assertEquals("Paxle PDF Parser", pdoc.getTitle());
		assertEquals("Testdocument", pdoc.getSummary());		
				
		assertNotNull(pdoc.getSubDocs());
		assertEquals(0, pdoc.getSubDocs().size());
		
		final Reader text = pdoc.getTextAsReader();
		assertNotNull(text);
		
		final String content = IOUtils.toString(text);
		assertNotNull(content);
		StringAssert.assertContains("Test­Document for Paxle Parsers", content);
		text.close();	
	}
	
	public void testParseHtml() throws UnsupportedEncodingException, ParserException, IOException {
		final String charset = null;
		final URI location = URI.create("http://java.sun.com/j2se/1.5.0/docs/api/java/net/URI.html");	
		final File testFile = new File(this.resourcesDir, "test.html");
		assertTrue(testFile.exists());
		
		// creating a paxle parser
		final ISubParser parser = this.tikaParser.createPaxleParser(MIME_TYPE_HTML);
		
		// parsing the data
		final IParserDocument pdoc = parser.parse(location, charset, testFile);
		assertNotNull(pdoc);
		assertEquals(IParserDocument.Status.OK, pdoc.getStatus());
		
		final Reader text = pdoc.getTextAsReader();
		assertNotNull(text);
		
		final String content = IOUtils.toString(text);
		assertNotNull(content);
		
		final Map<URI, LinkInfo> links = pdoc.getLinks();
		assertNotNull(links);
		assertFalse(links.size() == 0);		
	}	
}
