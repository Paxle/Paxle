package org.paxle.mimetype.impl;

import java.io.File;

import junit.framework.TestCase;

public class MimeTypeDetectorTest extends TestCase {
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
		{"rot3d.library",null},
	};
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.detector = new MimeTypeDetector(null);
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
