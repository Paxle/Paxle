
package org.paxle.parser.gzip.impl;

import java.io.File;
import java.net.URI;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.impl.AParserTest;
import org.paxle.parser.plain.impl.PlainParser;

public class GzipParserTest extends AParserTest {
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
		this.fileNameToMimeTypeMap.put(TEST_LOCATION, "text/plain");
		this.mimeTypeToParserMap.put("text/plain",new PlainParser());
		
		// create the parser(s)
		this.parser = new GzipParser();		
	}
	
	public void testParseFile() throws Exception {
		final String charset = null;
		final URI location = URI.create(TEST_LOCATION);	
		final File testFile = new File(resourcesDir, "test.txt.gz");
		assertTrue(testFile.exists());
				
		final IParserDocument pdoc = this.parser.parse(location, charset, testFile);
		assertNotNull(pdoc);
		assertEquals(IParserDocument.Status.OK, pdoc.getStatus());
		assertEquals("text/plain", pdoc.getMimeType());
	}
}