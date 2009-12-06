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
package org.paxle.tools.mimetype.impl;

import java.io.File;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.tools.mimetype.impl.MimeTypeDetector;

public class MimeTypeDetectorTest extends MockObjectTestCase {
	private static final int TESTFILE_NAME = 0;
	private static final int TESTFILE_MIMETYPE = 1;
	
	/**
	 * Mimetype detector class
	 */
	private MimeTypeDetector detector;
	
	/**
	 * Testfiles to test
	 */
	private static final String[][] testCases = new String[][] {
		{"test.txt","text/plain"},
		{"test.html","text/html"},
		{"test.pdf","application/pdf"},
		{"test.zip","application/zip"},
		{"test.bz2","application/x-bzip2"},
		{"test.gz","application/x-gzip"},
		{"test.7z","application/x-7z-compressed"},
		{"test.lha","application/x-lha"},
		{"test.tar","application/x-tar"},
		{"Jameson.skp","application/vnd.sketchup.skp"},
		{"rot3d.library",null},
	};
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();		
		this.detector = new MimeTypeDetector();
		this.detector.activate(null);
	}
	
	public void testDetectHtml() throws Exception {
		
		for (String[] testCase : testCases) {
			File testFile = new File("src/test/resources/" + testCase[TESTFILE_NAME]);
			assertTrue(testFile.exists());
			
			String mimeType = this.detector.getMimeType(testFile);
			
			System.out.println(String.format(
					"%s has mimetype %s.",
					testCase[TESTFILE_NAME],
					mimeType
			));
			final String expected = testCase[TESTFILE_MIMETYPE];
			if (expected == null) {
				assertNull("at " + testFile, mimeType);
			} else {
				assertNotNull("at " + testFile, mimeType);
				assertEquals(expected, mimeType);
			}
		}
	}
}
