
package org.paxle.parser.html.impl;

import java.io.File;
import java.net.URI;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.impl.TempFileManager;
import org.paxle.parser.ParserContext;

import junit.framework.TestCase;

public class HtmlParserTest extends TestCase {
	
	private static final String[] TEST_CASES = {
		"javascript_tcom.html",
		"URI.html"
	};
	
	public static void testHtmlParser() throws Exception {
		final File testResources = new File("src/test/resources/");
		final HtmlParser parser = new HtmlParser();
		ParserContext.setCurrentContext(new ParserContext(null, null, null, new TempFileManager()));
		for (final String testCase : TEST_CASES) {
			final IParserDocument pdoc = parser.parse(new URI("http://www.example.org/" + testCase), null, new File(testResources, testCase));
			assertNotNull(pdoc);
			
			System.out.println(testCase);
			System.out.println(pdoc.getLinks());
			System.out.println();
		}
	}
}
