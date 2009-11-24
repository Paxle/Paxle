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
package org.paxle.parser.pdf.impl;

import java.io.File;
import java.io.Reader;
import java.net.URI;

import junitx.framework.StringAssert;

import org.apache.commons.io.IOUtils;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.impl.IOTools;
import org.paxle.parser.impl.AParserTest;

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
	}
	
	public void testParsePdf() throws Exception {
		final String charset = null;
		final URI location = URI.create("http://www.paxle.net/");	
		final File testFile = new File(resourcesDir, "test.pdf");
		assertTrue(testFile.exists());
				
		final IParserDocument pdoc = this.parser.parse(location, charset, testFile);
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
}
