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

package org.paxle.parser.gzip.impl;

import java.io.File;
import java.net.URI;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.impl.AParserTest;
import org.paxle.parser.plain.impl.PlainParser;

public class GzipParserTest extends AParserTest {
	private static final String TEST_LOCATION = "http://www.example.org/";
	
	/**
	 * Directory containing test resources
	 */
	private final File resourcesDir = new File("src/test/resources");
	
	/**
	 * The parser
	 */
	private GzipParser parser = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// the archive contains a simple text-file
		this.registerMimeTypeForFile(TEST_LOCATION, "text/plain");
		this.registerParserForMimeType("text/plain",new PlainParser(){{
			this.contextLocal = getParserContextLocal();
		}});
		
		// create the parser(s)
		this.parser = new GzipParser();	
		this.parser.contextLocal = this.getParserContextLocal();
	}
	
	public void testParseFile() throws Exception {
		final String charset = null;
		final URI location = URI.create(TEST_LOCATION);	
		final File testFile = new File(resourcesDir, "test.txt.gz");
		assertTrue(testFile.exists());
				
		final IParserDocument pdoc = this.parser.parse(location, charset, testFile);
		assertNotNull(pdoc);
		assertEquals(IParserDocument.Status.OK, pdoc.getStatus());
		assertEquals("text/plain", pdoc.getMimeType());
	}
}
