package org.paxle.core.filter.impl;

import junit.framework.TestCase;

public class ReferenceNormalisationFilterTest extends TestCase {

	/**
	 * Testcases to test
	 */
	private static final String[][] testCases = new String[][] {
		//don't touch "good" URLs
		{"http://www.example.org/","http://www.example.org/"},
		{"http://www.example.org/test/test.htm","http://www.example.org/test/test.htm"},
		//remove the default port
		{"http://www.example.org:80","http://www.example.org/"},
		{"https://www.example.org:443/./","https://www.example.org/"},
		//remove empty parameters
		{"https://www.example.org/x.php?","https://www.example.org/x.php"},
		//add the trailing slash
		{"http://www.example.org","http://www.example.org/"},
		{"http://www.example.org/test","http://www.example.org/test"},
		//resolve backpaths
		{"http://example.org/test/.././x/y.htm","http://example.org/x/y.htm"},
		{"http://example.org/test/../x.htm","http://example.org/x.htm"},
		{"http://example.org/test/.././x/.././","http://example.org/"},
		//some combined cases
		{"http://user:pw@www.eXamplE.orG:359/path/../path/doc.htM?k=v&k2=v2#fragment","http://user:pw@www.example.org:359/path/doc.htM?k=v&k2=v2"},
		//ftp
		{"ftp://user@files.example.org:21/ex/../","ftp://user@files.example.org/"},
	};

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testReferenceNormalisationFilter() throws Exception {
		int x = 0;
		while (x < testCases.length) {
			String normalizationResult = ReferenceNormalizationFilter.normalizeLocation(testCases[x][0]);
			assertNotNull(normalizationResult);
			assertEquals(testCases[x][1], normalizationResult);
			x++;
		}
	}
}
