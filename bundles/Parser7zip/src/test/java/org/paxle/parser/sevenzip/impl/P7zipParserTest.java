
package org.paxle.parser.sevenzip.impl;

import java.io.File;
import java.net.URI;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.impl.AParserTest;
import org.paxle.parser.plain.impl.PlainParser;

public class P7zipParserTest extends AParserTest {
	private static final String TEST_LOCATION = "http://www.example.org/";
	
	/**
	 * Directory containing test resources
	 */
	private final File resourcesDir = new File("src/test/resources");
	
	/**
	 * The parser
	 */
	private ISubParser parser = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// the archive contains a simple text-file
		this.fileNameToMimeTypeMap.put("test.txt", "text/plain");
		this.mimeTypeToParserMap.put("text/plain",new PlainParser());
		
		// create the parser(s)
		this.parser = new P7zipParser();		
	}
	
	public void testParseFile() throws Exception {
		final String charset = null;
		final URI location = URI.create(TEST_LOCATION);	
		final File testFile = new File(resourcesDir, "test.7z");
		assertTrue(testFile.exists());
				
		final IParserDocument pdoc = this.parser.parse(location, charset, testFile);
		assertNotNull(pdoc);
		assertEquals(IParserDocument.Status.OK, pdoc.getStatus());
		assertNotNull(pdoc.getSubDocs());
		assertEquals(1, pdoc.getSubDocs().size());

		IParserDocument subDoc = pdoc.getSubDocs().get("test.txt");
		assertNotNull(subDoc);
		assertEquals(IParserDocument.Status.OK, subDoc.getStatus());
		assertEquals("text/plain", subDoc.getMimeType());
	}
}
