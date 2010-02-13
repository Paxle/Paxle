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

package org.paxle.core.doc.impl;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

public class CachedParserDocumentTest extends AParserDocumentTest {	

	public void testAppendDataInMemory() throws IOException {
		// creating an in-memory parser-doc
		final CachedParserDocument pdoc = new CachedParserDocument(this.tempFilemanager,(int)this.fileSize);
		assertTrue(pdoc.inMemory());
		assertEquals(0, pdoc.length());
		assertFalse(this.outputFile.exists());
		
		// copying data
		this.appendData(TESTFILE, pdoc);
		
		// data must have been kept in memory
		assertTrue(pdoc.inMemory());
		assertEquals(this.fileSize, pdoc.length());
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
	
	public void testWriteDataInMemory() throws IOException {
		// creating an in-memory parser-doc
		final CachedParserDocument pdoc = new CachedParserDocument(this.tempFilemanager,(int)this.fileSize);
		assertTrue(pdoc.inMemory());
		assertEquals(0, pdoc.length());
		assertFalse(this.outputFile.exists());
		
		// copying data
		this.writeData(TESTFILE, pdoc);
		
		// data must have been kept in memory
		assertTrue(pdoc.inMemory());
		assertEquals(this.fileSize, pdoc.length());
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
	
	public void testAppendDataInFile() throws IOException {
		// creating an in-memory parser-doc
		final CachedParserDocument pdoc = new CachedParserDocument(this.tempFilemanager,0);
		assertTrue(pdoc.inMemory());
		assertEquals(0, pdoc.length());
		assertFalse(this.outputFile.exists());
		
		// copying data
		this.appendData(TESTFILE, pdoc);
		
		// some tests
		assertFalse(pdoc.inMemory());
		assertEquals(this.fileSize, pdoc.length());
		assertTrue(this.outputFile.exists());
		assertEquals(TESTFILE, pdoc.getTextAsReader());
		assertEquals(TESTFILE, pdoc.getTextFile());
		assertFalse(pdoc.inMemory());
	}	
	
	public void testEmptyContent() throws IOException {
		// creating an in-memory parser-doc
		final CachedParserDocument pdoc = new CachedParserDocument(this.tempFilemanager,0);
		assertTrue(pdoc.inMemory());
		assertEquals(0, pdoc.length());
		assertFalse(this.outputFile.exists());
		
		// if no content is available the reader should be null
		final Reader reader = pdoc.getTextAsReader();
		assertNull(reader);
		
		// if no content is available the file should be null
		final File file = pdoc.getTextFile();
		assertNull(file);
	}
}
