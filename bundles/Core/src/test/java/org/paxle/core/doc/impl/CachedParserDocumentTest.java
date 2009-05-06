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

import java.io.File;
import java.io.IOException;
import java.io.Reader;

public class CachedParserDocumentTest extends AParserDocumentTest {	
	public void testParserDocInMemory() throws IOException {
		// creating an in-memory parser-doc
		long fileSize = TESTFILE.length();
		CachedParserDocument pdoc = new CachedParserDocument(this.tempFilemanager,(int)fileSize);
		
		// copying data
		this.copyData(TESTFILE, pdoc);
		
		// data must have been kept in memory
		assertTrue(pdoc.inMemory());
		assertFalse(this.outputFile.exists());
		
		// getting a reader without dumping data to disk
		assertEquals(TESTFILE, pdoc.getTextAsReader());
		assertFalse(this.outputFile.exists());
		assertTrue(pdoc.inMemory());
		
		// getting a file
		assertEquals(TESTFILE, pdoc.getTextFile());
		assertTrue(this.outputFile.exists());
		assertFalse(pdoc.inMemory());
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
		assertFalse(pdoc.inMemory());
	}	
	
	public void testEmptyContent() throws IOException {
		// creating an in-memory parser-doc
		CachedParserDocument pdoc = new CachedParserDocument(this.tempFilemanager,0);
		
		// if no content is available the reader should be null
		Reader reader = pdoc.getTextAsReader();
		assertNull(reader);
		
		// if no content is available the file should be null
		File file = pdoc.getTextFile();
		assertNull(file);
	}
}
