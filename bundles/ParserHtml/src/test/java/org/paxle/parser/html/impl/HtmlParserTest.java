
package org.paxle.parser.html.impl;

import java.io.File;
import java.net.URI;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.impl.TempFileManager;
import org.paxle.core.norm.impl.ReferenceNormalizer;
import org.paxle.parser.ParserContext;

import junit.framework.TestCase;

public class HtmlParserTest extends TestCase {
	
	private static final String[] TEST_CASES = {
		"svgopen.org_index.html",
		"javascript_test.html"
	};
	
	public static void testHtmlParser() throws Exception {
		final File testResources = new File("src/test/resources/");
		final HtmlParser parser = new HtmlParser();
		ParserContext.setCurrentContext(new ParserContext(null, null, null, new TempFileManager(), new ReferenceNormalizer()));
		for (final String testCase : TEST_CASES) {
			System.out.println(testCase);
			final IParserDocument pdoc = parser.parse(new URI("http://www.example.org/" + testCase), null, new File(testResources, testCase));
			assertNotNull(pdoc);
			
			System.out.println(pdoc.getLinks());
			System.out.println();
		}
	}
}
