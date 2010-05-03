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

package org.paxle.parser.pdf.impl;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

import junitx.framework.StringAssert;

import org.apache.commons.io.IOUtils;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.parser.ParserException;
import org.paxle.parser.impl.AParserTest;
import org.paxle.parser.plain.impl.PlainParser;

public class PdfParserTest extends AParserTest {
	/**
	 * Directory containing test resources
	 */
	private final File resourcesDir = new File("src/test/resources");
	
	/**
	 * The parser
	 */
	private PdfParser parser = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// create the parser(s)
		this.parser = new PdfParser();
		this.mimeTypeToParserMap.put("text/plain", new PlainParser());
	}
	
	public void testParsePdf() throws Exception {
		final String charset = null;
		final URI location = URI.create("http://www.paxle.net/");	
		final File testFile = new File(this.resourcesDir, "test.pdf");
		assertTrue(testFile.exists());
				
		final IParserDocument pdoc = this.parser.parse(location, charset, testFile);
		assertNotNull(pdoc);
		
		// check status
		assertEquals(IParserDocument.Status.OK, pdoc.getStatus());
		
		// check metadata
		assertEquals("Paxle PDF Parser", pdoc.getTitle());
		assertEquals("Testdocument", pdoc.getSummary());		
				
		// check sub-docs
		assertNotNull(pdoc.getSubDocs());
		assertEquals(0, pdoc.getSubDocs().size());
		
		// check links
		final Map<URI, LinkInfo> links = pdoc.getLinks();
		assertNotNull(links);
		assertEquals(2, links.size());
		assertTrue(links.containsKey(URI.create("http://trac.paxle.net/trac/browser/trunk/bundles/ParserMsOffice")));
		
		// check content
		final Reader text = pdoc.getTextAsReader();
		assertNotNull(text);
		
		final String content = IOUtils.toString(text);
		assertNotNull(content);
		StringAssert.assertContains("Test­Document for Paxle Parsers", content);
		text.close();
	}
	
	public void testParseEmbeddedFiles() throws UnsupportedEncodingException, ParserException, IOException {
		final String charset = null;
		final URI location = URI.create("http://www.paxle.net/");	
		final File testFile = new File(this.resourcesDir, "test2.pdf");
		assertTrue(testFile.exists());
				
		final IParserDocument pdoc = this.parser.parse(location, charset, testFile);
		assertNotNull(pdoc);		
	}
}
