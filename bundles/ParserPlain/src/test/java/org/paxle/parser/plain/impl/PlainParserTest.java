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
package org.paxle.parser.plain.impl;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.parser.impl.AParserTest;

public class PlainParserTest extends AParserTest {

	
	public void _testPlainParser2() throws Exception {
		final PlainParser pp = new PlainParser();
		final IParserDocument pdoc = pp.parse(URI.create("http://www.w3.org/Protocols/HTTP/1.1/rfc2616bis/draft-lafon-rfc2616bis-latest.txt"),
				null, new File("src/test/resources/draft-lafon-rfc2616bis-latest.txt"));
		System.out.println(pdoc.getTitle());
		// System.out.println(pdoc.getLinks().toString().replace(", ", "\n\t"));
		System.out.println(pdoc.getLinks().size());
	}
	
	public void testPlainParser1() throws Exception {
		final PlainParser pp = new PlainParser();
		final IParserDocument pdoc = pp.parse(new URI("http://www.example.org"), "UTF-8", new File("src/test/resources/threaddump"));
		assertNotNull(pdoc.getTitle());
		assertEquals("bli bla blo", pdoc.getTitle());
		assertNull(pdoc.getAuthor());
		assertNull(pdoc.getLastChanged());
		assertNull(pdoc.getMimeType());
		assertNull(pdoc.getSummary());
		assertTrue(pdoc.getHeadlines().size() == 0);
		assertTrue(pdoc.getImages().size() == 0);
		assertTrue(pdoc.getKeywords().size() == 0);
		assertTrue(pdoc.getLanguages().size() == 0);
		assertTrue(pdoc.getLinks().size() == 2);
		final Iterator<Map.Entry<URI,LinkInfo>> it = pdoc.getLinks().entrySet().iterator();
		assertEquals(URI.create("http://www.example.org/bla?blubb=#tmp"), it.next().getKey());
		assertEquals(URI.create("http://lists.w3.org/Archives/Public/ietf-http-wg/"), it.next().getKey());
		assertTrue(pdoc.getSubDocs().size() == 0);
	}
}
