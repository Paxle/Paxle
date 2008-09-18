
package org.paxle.parser.html.impl;

import java.io.File;
import java.net.URI;
import java.util.Iterator;

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
	
	// FIXME: <a name="#bla" /> is only recognized as a starting not an ending a-tag
	
	public static void testHtmlBaseHref() throws Exception {
		final File testResource = new File("src/test/resources/", "baseHrefTest.html");
		final HtmlParser parser = new HtmlParser();
		ParserContext.setCurrentContext(new ParserContext(null, null, null, new TempFileManager(), new ReferenceNormalizer()));
		final IParserDocument pdoc = parser.parse(URI.create("http://www.example.org/baseHrefTest.html"), null, testResource);
		assertNotNull(pdoc);
		final Iterator<URI> it = pdoc.getLinks().keySet().iterator();
		assertTrue(it.hasNext());
		assertEquals(URI.create("http://www.example.net/test/blubb"), it.next());
	}
	
	/** does not work as expected yet */
	public static void _testHtmlParser() throws Exception {
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
	
	private static final String[][] REPL_CASES = {
		{ "Il y a une &eacute;cole", "Il y a une \u00E9cole" },
		{ "Da &amp; dort passierte &quot;etwas&quot;.", "Da & dort passierte \"etwas\"." }
	};
	
	public static void testHtmlReplace() throws Exception {
		for (int i=0; i<REPL_CASES.length; i++) {
			final String repl = HtmlTools.deReplaceHTML(REPL_CASES[i][0]);
			final String exp = REPL_CASES[i][1];
			assertNotNull(repl);
			assertEquals(exp, repl);
		}
	}
}
