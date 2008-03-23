package org.paxle.parser.msoffice.impl;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junitx.framework.ListAssert;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ParserException;


public class MsWordParserTest extends AMsOfficeParserTest {

	private MsWordParser parser = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// create parser
		this.parser = new MsWordParser();
	}

	public void testGetMimeType() {
		List<String> mimeTypes = this.parser.getMimeTypes();
		assertNotNull(mimeTypes);
		assertEquals(1, mimeTypes.size());
		ListAssert.assertContains(mimeTypes,"application/msword");
	}

	public void testParseMsWord() throws UnsupportedEncodingException, ParserException, IOException, URISyntaxException {
		IParserDocument parserDoc = null;
		try {
			List<String> mimeTypes = this.parser.getMimeTypes();
			assertNotNull(mimeTypes);
			assertTrue(mimeTypes.size() > 0);

			String mimeType = mimeTypes.get(0);
			assertTrue(mimeType.length() != 0);

			File testFile = new File("src/test/resources/test.doc");
			assertTrue(testFile.exists());

			parserDoc = this.parser.parse(new URI("http://mydummylocation.at"), "UTF-8", testFile);
			assertNotNull(parserDoc);
			assertEquals(IParserDocument.Status.OK, parserDoc.getStatus());		
			assertEquals("Paxle MsOffice Parser", parserDoc.getTitle());
			assertEquals("Testdocument", parserDoc.getSummary());
			assertEquals("Martin Thelian", parserDoc.getAuthor());
			ListAssert.assertEquals(Arrays.asList(new String[]{"paxle","tests","junit"}), new ArrayList<String>(parserDoc.getKeywords()));
		} finally {
			if (parserDoc != null) parserDoc.close();
		}		
	}

}
