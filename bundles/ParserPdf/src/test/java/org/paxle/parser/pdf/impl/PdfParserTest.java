package org.paxle.parser.pdf.impl;

import java.io.File;
import java.net.URI;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.impl.AParserTest;

public class PdfParserTest extends AParserTest {
	/**
	 * Directory containing test resources
	 */
	private final File resourcesDir = new File("src/test/resources");
	
	/**
	 * The parser
	 */
	private PdfParser parser = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// create the parser(s)
		this.parser = new PdfParser();		
	}
	
	public void testParsePdf() throws Exception {
		final String charset = null;
		final URI location = URI.create("http://www.paxle.net/");	
		final File testFile = new File(resourcesDir, "test.pdf");
		assertTrue(testFile.exists());
				
		final IParserDocument pdoc = this.parser.parse(location, charset, testFile);
		assertNotNull(pdoc);
		assertEquals(IParserDocument.Status.OK, pdoc.getStatus());
		assertEquals("Paxle PDF Parser", pdoc.getTitle());
		assertEquals("Testdocument", pdoc.getSummary());		
		
		assertNotNull(pdoc.getSubDocs());
		assertEquals(0, pdoc.getSubDocs().size());
	}
}
