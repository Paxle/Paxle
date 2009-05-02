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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import junitx.framework.FileAssert;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;

public abstract class AParserDocumentTest extends MockObjectTestCase {
	protected static final File TESTFILE = new File("src/test/resources/paxle.txt");
	
	/**
	 * A dummy temp file manager
	 */
	protected ITempFileManager tempFilemanager;
	
	/**
	 * The output file where the text appended to the {@link IParserDocument} should
	 * be written to.
	 */
	protected File outputFile = new File("target/test.txt");
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// deleting files from a previous run
		if (this.outputFile.exists()) outputFile.delete();
		
		// creating a dummy temp-file-manager
		this.tempFilemanager = mock(ITempFileManager.class);
		checking(new Expectations(){{
			atMost(1).of(tempFilemanager).createTempFile();
			will(returnValue(outputFile));
		}});
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.outputFile.exists()) outputFile.delete();
	}
	
	protected static void assertEquals(File expected, Reader actual) throws IOException {
		// reading source-string
		InputStreamReader sourceReader = new InputStreamReader(new FileInputStream(expected),Charset.forName("UTF-8"));
		final String sourceText = IOUtils.toString(sourceReader);
		sourceReader.close();
		
		// reading target string
		String targetText = IOUtils.toString(actual);
		actual.close();
		
		assertEquals(sourceText, targetText);
	}
	
	protected static void assertEquals(File expected, File actual) {
		FileAssert.assertBinaryEquals(expected, actual);
	}
	
	protected void copyData(File source, IParserDocument target) throws IOException {
		StringBuilder sourceText = new StringBuilder();
		
		// writing Data
		CharBuffer buffer = CharBuffer.allocate(50);
		InputStreamReader sourceReader = new InputStreamReader(new FileInputStream(source),Charset.forName("UTF-8"));
		while (sourceReader.read(buffer) != -1) {
			buffer.flip();
			sourceText.append(buffer);
			target.append(buffer.toString());
		}
		sourceReader.close();
		target.close();
	}
}
