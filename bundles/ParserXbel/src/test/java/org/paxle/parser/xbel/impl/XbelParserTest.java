package org.paxle.parser.xbel.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import junitx.framework.ListAssert;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ParserException;
import org.paxle.parser.impl.AParserTest;

public class XbelParserTest extends AParserTest {
	
	private XbelParser parser = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// create parser
		this.parser = new XbelParser();
	}	
	
	public void testParseXbel() throws UnsupportedEncodingException, ParserException, IOException, URISyntaxException {
		IParserDocument parserDoc = null;
		try {
			List<String> mimeTypes = this.parser.getMimeTypes();
			assertNotNull(mimeTypes);
			assertTrue(mimeTypes.size() > 0);

			String mimeType = mimeTypes.get(0);
			assertTrue(mimeType.length() != 0);

			File testFile = new File("src/test/resources/test.xbel");
			assertTrue(testFile.exists());

			parserDoc = this.parser.parse(new URI("http://mydummylocation.at"), "UTF-8", testFile);
			assertNotNull(parserDoc);
			assertEquals(IParserDocument.Status.OK, parserDoc.getStatus());		
			assertEquals("Some of David's Bookmarks", parserDoc.getTitle());
			assertNotNull(parserDoc.getLinks());
			assertEquals(14, parserDoc.getLinks().size());
		} finally {
			if (parserDoc != null) parserDoc.close();
		}		
	}
}
