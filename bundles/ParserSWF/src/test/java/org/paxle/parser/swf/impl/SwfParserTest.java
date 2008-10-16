package org.paxle.parser.swf.impl;

import java.io.File;
import java.net.URI;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.impl.AParserTest;

public class SwfParserTest extends AParserTest {
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
		
		// create the parser(s)
		this.parser = new SWFParser();		
	}
	
	public void testParseFile() throws Exception {
		final String charset = null;
		final URI location = URI.create("http://www.paxle.net/");	
		final File testFile = new File(resourcesDir, "test.swf");
		assertTrue(testFile.exists());
				
		final IParserDocument pdoc = this.parser.parse(location, charset, testFile);
		assertNotNull(pdoc);
		assertEquals(IParserDocument.Status.OK, pdoc.getStatus());
	}
}
