package org.paxle.parser.msoffice.impl;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import junitx.framework.ArrayAssert;
import junitx.framework.ListAssert;
import junitx.framework.StringAssert;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempDir;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.msoffice.impl.MsWordParser;


public class MsWordParserTest extends TestCase {

	private MsWordParser parser = null;
	private ParserContext parserContext = null;

	protected void setUp() throws Exception {
		super.setUp();

		// create parser
		this.parser = new MsWordParser();

		// create a parser context with a dummy temp-file-manager
		this.parserContext = new ParserContext(null,null,null, new ITempFileManager() {		
			public void setTempDirFor(ITempDir arg0, String... arg1) { }		
			public void removeTempDirFor(String... arg0) { }

			public void releaseTempFile(File arg0) throws FileNotFoundException, IOException {
				if (arg0 != null) arg0.delete();
			}

			public File createTempFile() throws IOException {
				File tempfile = File.createTempFile("mswordParser", ".doc");
				tempfile.deleteOnExit();
				return tempfile;
			}

		});
		ParserContext.setCurrentContext(this.parserContext);
	}

	public void testGetMimeType() {
		List<String> mimeTypes = this.parser.getMimeTypes();
		assertNotNull(mimeTypes);
		assertEquals(1, mimeTypes.size());
		ListAssert.assertContains(mimeTypes,"application/msword");
	}

	public void testParseMsWord() throws UnsupportedEncodingException, ParserException, IOException {
		IParserDocument parserDoc = null;
		try {
			List<String> mimeTypes = this.parser.getMimeTypes();
			assertNotNull(mimeTypes);
			assertTrue(mimeTypes.size() > 0);

			String mimeType = mimeTypes.get(0);
			assertTrue(mimeType.length() != 0);

			File testFile = new File("src/test/resources/test.doc");
			assertTrue(testFile.exists());

			parserDoc = this.parser.parse("http://mydummylocation.at", "UTF-8", testFile);
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
