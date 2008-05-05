
package org.paxle.parser.plain.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

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
				try {
					return new URI(reference);
				} catch (URISyntaxException e) {
					System.err.println("error normalizing '" + reference + "': " + e.getMessage());
					return null;
				}
			}

			public URI normalizeReference(String reference, Charset charset) {
				return normalizeReference(reference);
			}
		});
		ParserContext.setCurrentContext(this.parserContext);		
	}
	
	public void _testPlainParser2() throws Exception {
		final PlainParser pp = new PlainParser();
		final IParserDocument pdoc = pp.parse(URI.create("http://www.w3.org/Protocols/HTTP/1.1/rfc2616bis/draft-lafon-rfc2616bis-latest.txt"),
				null, new File("src/test/resources/draft-lafon-rfc2616bis-latest.txt"));
		System.out.println(pdoc.getTitle());
		// System.out.println(pdoc.getLinks().toString().replace(", ", "\n\t"));
		System.out.println(pdoc.getLinks().size());
	}
	
	public void testPlainParser1() throws Exception {
		final PlainParser pp = new PlainParser();
		final IParserDocument pdoc = pp.parse(new URI("http://www.example.org"), "UTF-8", new File("src/test/resources/threaddump"));
		assertNotNull(pdoc.getTitle());
		// assertEquals("\"CrawlerWorker\" daemon prio=10 tid=0x081bb000 nid=0x286b in Object.wait() [0x97f1d000..0x97f1e030]", pdoc.getTitle());
		assertEquals("bli bla blo", pdoc.getTitle());
		assertNull(pdoc.getAuthor());
		assertNull(pdoc.getLastChanged());
		assertNull(pdoc.getMimeType());
		assertNull(pdoc.getSummary());
		assertTrue(pdoc.getHeadlines().size() == 0);
		assertTrue(pdoc.getImages().size() == 0);
		assertTrue(pdoc.getKeywords().size() == 0);
		assertTrue(pdoc.getLanguages().size() == 0);
		assertTrue(pdoc.getLinks().size() == 2);
		final Iterator<Map.Entry<URI,String>> it = pdoc.getLinks().entrySet().iterator();
		assertEquals(URI.create("http://www.example.org/bla?blubb=#tmp"), it.next().getKey());
		assertEquals(URI.create("http://lists.w3.org/Archives/Public/ietf-http-wg/"), it.next().getKey());
		assertTrue(pdoc.getSubDocs().size() == 0);
	}
}
