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

package org.paxle.core.norm.impl;

import java.net.URISyntaxException;

import junit.framework.TestCase;

public class ReferenceNormalizationFilterTest extends TestCase {
	
	/**
	 * Testcases to test
	 */
	private static final String[][] testCasesNotNull = new String[][] {
		//don't touch "good" URLs
		// {"http://www.example.org/bla?,","http://www.example.org/%2c"},
		{"http://www.example.org/test/test.htm","http://www.example.org/test/test.htm"},
		//remove the default port
		{"http://www.example.org:80","http://www.example.org/"},
		{"https://www.example.org:443/./","https://www.example.org/"},
		//remove empty parameters
		{"https://www.example.org/x.php?","https://www.example.org/x.php"},
		//add the trailing slash
		{"http://www.example.org","http://www.example.org/"},
		// {"http://www.example.org/test","http://www.example.org/test/"}, // should this work?
		//resolve backpaths
		{"http://example.org/test/.././x/y.htm","http://example.org/x/y.htm"},
		{"http://example.org/test/../x.htm","http://example.org/x.htm"},
		{"http://example.org/test/.././x/.././","http://example.org/"},
		//some combined cases
		{"http://user:pw@www.eXamplE.orG:359/path/../path/doc.htM?k=v&k2=v2#fragment","http://user:pw@www.example.org:359/path/doc.htM?k=v&k2=v2"},
		//ftp
		{"ftp://user@files.example.org/ex/../","ftp://user@files.example.org/"},
		// {"http://xn--bloah-nua.xn--brse-5qa.de/","http://bloah\u00F6.b\u00F6rse.de/"}, // should not work
		// other protocols
		{"smb://stuff/zeug/bla/../test.exe","smb://stuff/zeug/test.exe"},
		{"http://etienne.chouard.free.fr/Europe/forum/index.php?2007/10/27/86-le-candidat-giscard-d-estaing-confirme-" +
				"la-scandaleuse-arnaque-du-mini-traite-simplifie-il-oublie-cependant-quelques-details",
			"http://etienne.chouard.free.fr/Europe/forum/index.php?2007%2F10%2F27%2F86-le-candidat-giscard-d-estaing-confirme-" +
			"la-scandaleuse-arnaque-du-mini-traite-simplifie-il-oublie-cependant-quelques-details"},
		{"http://ontolog.cim3.net/cgi-bin/wiki.pl?DatabaseAndOntology","http://ontolog.cim3.net/cgi-bin/wiki.pl?DatabaseAndOntology"},
		{"http://www.semanlink.net/andkws.do;jsessionid=7A0F3800C5F466E0EA9405C483CB3341?&" +
				"kwuris=http%3A%2F%2Fwww.semanlink.net%2Ftag%2Fairbus&kwuris=http%3A%2F%2Fwww.semanlink.net%2Ftag%2Ftechnical_documentation",
			"http://www.semanlink.net/andkws.do;jsessionid=7A0F3800C5F466E0EA9405C483CB3341?" +
			"kwuris=http%3A%2F%2Fwww.semanlink.net%2Ftag%2Fairbus&kwuris=http%3A%2F%2Fwww.semanlink.net%2Ftag%2Ftechnical_documentation"},
		{"http://www.youtube.com/comment_servlet?all_comments&v=snaecrgdniU&fromurl=/watch%3Fv%3DsnaecrgdniU",
			"http://www.youtube.com/comment_servlet?all_comments&v=snaecrgdniU&fromurl=%2Fwatch%3Fv%3DsnaecrgdniU"},
 		{"http://fr.wikipedia.org/wiki/cat\u00e9gorie/wiki/Cat\u00e9gorie:Film_danois",
			"http://fr.wikipedia.org/wiki/cat%C3%A9gorie/wiki/Cat%C3%A9gorie:Film_danois"},
//		{"http://ftp://www.example.org/bla?blubb#blo", "ftp://www.example.org/bla?blubb"}, // TODO
		{"ftp://www.example.org/bla?blubb#blo", "ftp://www.example.org/bla?blubb"},
		{"http://ko.wikipedia.org/wiki/\ud68c\uc808", "http://ko.wikipedia.org/wiki/%ED%9A%8C%EC%A0%88"},
		{"http://test.example.org/bla?key%ze=value%2f","http://test.example.org/bla?key%25ze=value%2F"}
	};
	
	private static final String[] testCasesException = new String[] {"", "example.org"};
	
	
	public void testReferenceNormalisationFilter() throws Exception {
		final ReferenceNormalizer refNorm = new ReferenceNormalizer(false, false, false);
		for (int x=0; x<testCasesNotNull.length; x++) {
			final String input = testCasesNotNull[x][0];
			final String normalizationResult = refNorm.parseBaseUrlString(input, ReferenceNormalizer.UTF8).toASCIIString();
			assertNotNull(normalizationResult);
			assertEquals("for input: " + input, testCasesNotNull[x][1], normalizationResult);
		}
		
		//test for URLs without protocol
		for (int x=0; x<testCasesException.length; x++) {
			try {
				refNorm.parseBaseUrlString(testCasesException[x], ReferenceNormalizer.UTF8).toASCIIString();
				fail("Normalizing " + testCasesException[x] + " should have thrown an URISyntaxException exception!");
			} catch (URISyntaxException exp) {
				// expected, so ignore it
			}
		}
	}
}
