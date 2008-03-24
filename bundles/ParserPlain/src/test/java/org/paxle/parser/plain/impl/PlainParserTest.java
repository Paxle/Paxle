
package org.paxle.parser.plain.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempDir;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.parser.ParserContext;

import junit.framework.TestCase;

public class PlainParserTest extends TestCase {
	
	protected ParserContext parserContext = null;
	
	// from ParserCore - org.paxle.parser.impl
	// not possible to access AParserTest because the test-folder isn't exported
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// create a parser context with a dummy temp-file-manager
		this.parserContext = new ParserContext(null,null,null, new ITempFileManager() {		
			public void setTempDirFor(ITempDir arg0, String... arg1) { }		
			public void removeTempDirFor(String... arg0) { }
			
			public void releaseTempFile(File arg0) throws FileNotFoundException, IOException {
				if (arg0 != null) arg0.delete();
			}
			
			public File createTempFile() throws IOException {
				File tempfile = File.createTempFile("parserTest", ".tmp");
				tempfile.deleteOnExit();
				return tempfile;
			}

		}, new IReferenceNormalizer() {
			public URI normalizeReference(String reference) {
				return URI.create(reference);
			}
		});
		ParserContext.setCurrentContext(this.parserContext);		
	}
	
	public void testPlainParser() throws Exception {
		final PlainParser pp = new PlainParser();
		final IParserDocument pdoc = pp.parse(new URI("http://www.example.org"), "UTF-8", new File("src/test/resources/threaddump"));
		assertNotNull(pdoc.getTitle());
		assertEquals("\"CrawlerWorker\" daemon prio=10 tid=0x081bb000 nid=0x286b in Object.wait() [0x97f1d000..0x97f1e030]", pdoc.getTitle());
		assertNull(pdoc.getAuthor());
		assertNull(pdoc.getLastChanged());
		assertNull(pdoc.getMimeType());
		assertNull(pdoc.getSummary());
		assertTrue(pdoc.getHeadlines().size() == 0);
		assertTrue(pdoc.getImages().size() == 0);
		assertTrue(pdoc.getKeywords().size() == 0);
		assertTrue(pdoc.getLanguages().size() == 0);
		assertTrue(pdoc.getLinks().size() == 1);
		assertEquals(URI.create("http://www.example.org/bla?blubb=#tmp"), pdoc.getLinks().entrySet().iterator().next().getKey());
		assertTrue(pdoc.getSubDocs().size() == 0);
	}
}
