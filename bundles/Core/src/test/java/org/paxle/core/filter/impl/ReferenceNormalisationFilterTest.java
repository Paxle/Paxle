package org.paxle.core.filter.impl;

import java.nio.charset.Charset;

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
		{"ftp://user@files.example.org/ex/../","ftp://user@files.example.org/"},
		// {"http://xn--bloah-nua.xn--brse-5qa.de/","http://bloah\u00F6.b\u00F6rse.de/"},
		// other protocols
		// {"smb://stuff/zeug/bla/../test.exe","smb://stuff/zeug/test.exe"},
		{"http://etienne.chouard.free.fr/Europe/forum/index.php?2007/10/27/86-le-candidat-giscard-d-estaing-confirme-la-scandaleuse-arnaque-du-mini-traite-simplifie-il-oublie-cependant-quelques-details","http://etienne.chouard.free.fr/Europe/forum/index.php?2007/10/27/86-le-candidat-giscard-d-estaing-confirme-la-scandaleuse-arnaque-du-mini-traite-simplifie-il-oublie-cependant-quelques-details"},
		{"http://ontolog.cim3.net/cgi-bin/wiki.pl?DatabaseAndOntology","http://ontolog.cim3.net/cgi-bin/wiki.pl?DatabaseAndOntology"}
	};

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testReferenceNormalisationFilter() throws Exception {
		int x = 0;
		while (x < testCases.length) {
			final ReferenceNormalizationFilter.OwnURL url = new ReferenceNormalizationFilter().new OwnURL();
			final String normalizationResult = url.parseBaseUrlString(testCases[x][0], Charset.defaultCharset());
			assertNotNull(normalizationResult);
			assertEquals(testCases[x][1], normalizationResult);
			
			x++;
		}
	}
}
