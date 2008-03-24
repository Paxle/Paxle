
package org.paxle.core.norm.impl;

import java.net.URI;

import org.paxle.core.norm.impl.ReferenceNormalizer;

import junit.framework.TestCase;

public class ReferenceNormalizationFilterTest extends TestCase {
	
	/**
	 * Testcases to test
	 */
	private static final String[][] testCases = new String[][] {
		//don't touch "good" URLs
		{"http://www.example.org/bla?,","http://www.example.org/%2c"},
		{"http://www.example.org/test/test.htm","http://www.example.org/test/test.htm"},
		//remove the default port
		{"http://www.example.org:80","http://www.example.org/"},
		{"https://www.example.org:443/./","https://www.example.org/"},
		//remove empty parameters
		{"https://www.example.org/x.php?","https://www.example.org/x.php"},
		//add the trailing slash
		{"http://www.example.org","http://www.example.org/"},
		{"http://www.example.org/test","http://www.example.org/test/"},
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
		{"http://etienne.chouard.free.fr/Europe/forum/index.php?2007/10/27/86-le-candidat-giscard-d-estaing-confirme-" +
				"la-scandaleuse-arnaque-du-mini-traite-simplifie-il-oublie-cependant-quelques-details",
			"http://etienne.chouard.free.fr/Europe/forum/index.php?2007/10/27/86-le-candidat-giscard-d-estaing-confirme-" +
			"la-scandaleuse-arnaque-du-mini-traite-simplifie-il-oublie-cependant-quelques-details"},
		{"http://ontolog.cim3.net/cgi-bin/wiki.pl?DatabaseAndOntology","http://ontolog.cim3.net/cgi-bin/wiki.pl?DatabaseAndOntology"},
		{"http://www.semanlink.net/andkws.do;jsessionid=7A0F3800C5F466E0EA9405C483CB3341?&" +
				"kwuris=http%3A%2F%2Fwww.semanlink.net%2Ftag%2Fairbus&kwuris=http%3A%2F%2Fwww.semanlink.net%2Ftag%2Ftechnical_documentation",
			"http://www.semanlink.net/andkws.do;jsessionid=7A0F3800C5F466E0EA9405C483CB3341?" +
			"kwuris=http://www.semanlink.net/tag/airbus&kwuris=http://www.semanlink.net/tag/technical_documentation"},
		{"http://www.youtube.com/comment_servlet?all_comments&v=snaecrgdniU&fromurl=/watch%3Fv%3DsnaecrgdniU",
			"http://www.youtube.com/comment_servlet?all_comments&v=snaecrgdniU&fromurl=/watch%3Fv%3DsnaecrgdniU"}, 
			
 		{"http://fr.wikipedia.org/wiki/cat%c3%a9gorie/wiki/Cat%c3%a9gorie:Film_danois",
			"http://fr.wikipedia.org/wiki/cat\u00e9gorie/wiki/Cat\u00e9gorie:Film_danois"},
// 		{"http://ftp://www.example.org/bla?blubb#blo", "ftp://www.example.org/bla?blubb"},
	};
	
	public void testReferenceNormalisationFilter() throws Exception {
		// TODO
		/*for (int x=0; x<testCases.length; x++) {
			final ReferenceNormalizer refNorm = new ReferenceNormalizer(false, true);
			final String normalizationResult = refNorm.parseBaseUrlString(testCases[x][0], ReferenceNormalizer.UTF8).toASCIIString();
			assertNotNull(normalizationResult);
			assertEquals(testCases[x][1], normalizationResult);
		}*/
	}
}
