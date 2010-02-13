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

package org.paxle.parser.msoffice.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import junitx.framework.ListAssert;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ParserException;
import org.paxle.parser.impl.AParserTest;

public class MsPowerpointParserTest extends AParserTest {

	private MsPowerpointParser parser = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// create parser
		this.parser = new MsPowerpointParser();
	}
	
	public void testParseMsPowerpoint() throws UnsupportedEncodingException, ParserException, IOException, URISyntaxException {
		IParserDocument parserDoc = null;
		try {
			File testFile = new File("src/test/resources/test.ppt");
			assertTrue(testFile.exists());

			parserDoc = this.parser.parse(new URI("http://mydummylocation.at"), "UTF-8", testFile);
			assertNotNull(parserDoc);
			assertEquals(IParserDocument.Status.OK, parserDoc.getStatus());		
			assertEquals("Paxle MsOffice Parser", parserDoc.getTitle());
			assertEquals("Martin Thelian", parserDoc.getAuthor());
			ListAssert.assertEquals(Arrays.asList(new String[]{"paxle","tests","junit"}), new ArrayList<String>(parserDoc.getKeywords()));
		} finally {
			if (parserDoc != null) parserDoc.close();
		}		
	}
}
