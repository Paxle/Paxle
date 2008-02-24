package org.paxle.core.filter.impl;

import junit.framework.TestCase;

public class ReferenceNormalisationFilterTest extends TestCase {

	/**
	 * Testcases to test
	 */
	private static final String[][] testCases = new String[][] {
		//don't touch "good" URLs
		{"http://www.example.org/","http://www.example.org/"},
		//add the trailing slash
		{"http://www.example.org","http://www.example.org/"},
		//resolve backpaths, see #29
		//{"http://example.org/test/.././x/y.htm","http://example.org/x/y.htm"},
		//{"http://example.org/test/../x.htm","http://example.org/x.htm"},
		//{"http://example.org/test/.././x/.././","http://example.org/"},
	};

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	

	public void testDetectCharsets() throws Exception {

		int x = 0;
		while (x < testCases.length) {
			String normalizationResult = ReferenceNormalizationFilter.normalizeLocation(testCases[x][0]);
			assertNotNull(normalizationResult);
			assertEquals(testCases[x][1], normalizationResult);
			x++;
		}
	}
}
