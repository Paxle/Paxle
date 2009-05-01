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
package org.paxle.core.doc.impl;

import java.io.IOException;

public class CachedParserDocumentTest extends AParserDocumentTest {	
	public void testParserDocInMemory() throws IOException {
		// creating an in-memory parser-doc
		long fileSize = TESTFILE.length();
		CachedParserDocument pdoc = new CachedParserDocument(this.tempFilemanager,(int)fileSize);
		
		// copying data
		this.copyData(TESTFILE, pdoc);
		assertTrue(pdoc.inMemory());
		assertFalse(this.outputFile.exists());
		assertEquals(TESTFILE, pdoc.getTextAsReader());
		assertEquals(TESTFILE, pdoc.getTextFile());
		assertTrue(pdoc.inMemory());
	}
	
	public void testParserDocInFile() throws IOException {
		// creating an in-memory parser-doc
		CachedParserDocument pdoc = new CachedParserDocument(this.tempFilemanager,0);
		
		// copying data
		this.copyData(TESTFILE, pdoc);
		assertFalse(pdoc.inMemory());
		assertTrue(this.outputFile.exists());
		assertEquals(TESTFILE, pdoc.getTextAsReader());
		assertEquals(TESTFILE, pdoc.getTextFile());
	}	
}
