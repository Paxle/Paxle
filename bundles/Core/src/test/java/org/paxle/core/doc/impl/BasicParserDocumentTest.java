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

public class BasicParserDocumentTest extends AParserDocumentTest {	
	
	public void testAppendData() throws IOException {
		// creating a basic parser-doc
		final BasicParserDocument pdoc = new BasicParserDocument(this.tempFilemanager);
		assertFalse(pdoc.inMemory());
		assertEquals(0, pdoc.length());
		assertFalse(this.outputFile.exists());
		
		// copying data		
		this.appendData(TESTFILE, pdoc);
		
		// some testing
		assertFalse(pdoc.inMemory());
		assertTrue(this.outputFile.exists());
		assertEquals(TESTFILE, pdoc.getTextAsReader());
		assertEquals(TESTFILE, pdoc.getTextFile());
		assertEquals(this.fileSize, pdoc.length());
	}	
	
	public void testWriteData() throws IOException {
		// creating a basic parser-doc
		final BasicParserDocument pdoc = new BasicParserDocument(this.tempFilemanager);
		assertFalse(pdoc.inMemory());
		assertEquals(0, pdoc.length());
		assertFalse(this.outputFile.exists());
		
		// write data
		this.writeData(TESTFILE, pdoc);
		
		// some testing
		assertFalse(pdoc.inMemory());
		assertTrue(this.outputFile.exists());
		assertEquals(TESTFILE, pdoc.getTextAsReader());
		assertEquals(TESTFILE, pdoc.getTextFile());
		assertEquals(this.fileSize, pdoc.length());
	}
	
	public void testEmptyContent() throws IOException {
		// creating an in-memory parser-doc
		final BasicParserDocument pdoc = new BasicParserDocument(this.tempFilemanager);
		assertFalse(pdoc.inMemory());
		assertEquals(0, pdoc.length());
		assertFalse(this.outputFile.exists());		
		
		// if no content is available the reader should be null
		final Reader reader = pdoc.getTextAsReader();
		assertNull(reader);
		
		// if no content is available the file should be null
		final File file = pdoc.getTextFile();
		assertNull(file);
		assertFalse(this.outputFile.exists());
	}	
}
