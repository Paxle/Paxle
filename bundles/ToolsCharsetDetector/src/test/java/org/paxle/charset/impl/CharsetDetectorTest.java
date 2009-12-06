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
package org.paxle.charset.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

public class CharsetDetectorTest  extends TestCase {
	private static final int TESTFILE_NAME = 0;
	private static final int TESTFILE_CHARSET = 1;

	/**
	 * Charset detector class
	 */
	private CharsetDetector detector;

	/**
	 * Testfiles to test
	 */
	private static final String[][] testCases = new String[][] {
		{"gb2312.html","GB2312"},
		{"ISO-2022-JP.html","ISO-2022-JP"},
		{"Shift_JIS.html","Shift_JIS"}
	};

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// set detector properties
		final File mimeTypesFile = new File("src/main/resources/mimeTypes");
		final Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put(CharsetDetector.MIMETYPE_FILE, mimeTypesFile.toURI().toURL().toExternalForm());
		
		// create and activate component
		this.detector = new CharsetDetector();
		this.detector.activate(props);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		// deactivate detector
		this.detector.deactivate(null);
	}

	public void testDetectCharsetsFromStream() throws Exception {

		for (String[] testCase : testCases) {
			File testFile = new File("src/test/resources/" + testCase[TESTFILE_NAME]);
			assertTrue(testFile.exists());

			// open file
			BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(testFile));
			assertNotNull(fileInput);
			
			// create dummy output stream
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			
			// create an charset-detector input-stream
			CharsetDetectorOutputStream output = this.detector.createOutputStream(bout);

			// just read all data
			int c;
			while ((c = fileInput.read()) != -1) {
				output.write(c);
			}
			fileInput.close();
			
			// get the detected charser
			String detectedCharset = output.getCharset();
			assertNotNull(detectedCharset);
			assertEquals(testCase[TESTFILE_CHARSET], detectedCharset);
		}
	}
	
	public void testDetectCharsetsFromFile() throws IOException {
		for (String[] testCase : testCases) {
			File testFile = new File("src/test/resources/" + testCase[TESTFILE_NAME]);
			assertTrue(testFile.exists());
			
			String detectedCharset = this.detector.detectCharset(testFile);
			assertNotNull(detectedCharset);
			assertEquals(testCase[TESTFILE_CHARSET], detectedCharset);
		}
	}
}
