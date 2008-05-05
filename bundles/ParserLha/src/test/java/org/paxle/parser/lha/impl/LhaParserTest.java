
package org.paxle.parser.lha.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.ParserDocument;
import org.paxle.core.io.temp.ITempDir;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.parser.ASubParser;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;

public class LhaParserTest extends TestCase {
	
	public static final String[] TEST_FILES = {
		"test.lha"
	};
	
	private static final HashMap<String,String> EXT_TABLE = new HashMap<String,String>();
	static {
		EXT_TABLE.put("txt", "text/plain");
	}
	
	protected ParserContext parserContext = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// create a parser context with a dummy mime-type detector, temp-file-manager and reference-normalizer
		this.parserContext = new ParserContext(new ISubParserManager() {
			public Set<String> disabledMimeTypes() {
				// TODO Auto-generated method stub
				return null;
			}
			
			public void disableMimeType(String arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void enableMimeType(String arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public Collection<String> getMimeTypes() {
				// TODO Auto-generated method stub
				return null;
			}
			
			public ISubParser getSubParser(String arg0) {
				return new ASubParser() {
					public List<String> getMimeTypes() {
						// TODO Auto-generated method stub
						return null;
					}
					@Override
					public IParserDocument parse(URI location, String charset, InputStream is) throws ParserException, UnsupportedEncodingException, IOException {
						System.out.println("sub-parsing location: " + location);
						String line;
						final BufferedReader br = new BufferedReader(new InputStreamReader(is));
						while ((line = br.readLine()) != null)
							System.out.println(line);
						System.out.println("finished sub-parsing location: " + location);
						return new ParserDocument();
					}
				};
			}
			
			public Collection<ISubParser> getSubParsers() {
				// TODO Auto-generated method stub
				return null;
			}
		}, new IMimeTypeDetector() {
			public String getMimeType(byte[] arg0, String arg1) throws Exception {
				return EXT_TABLE.get(arg1.substring(arg1.lastIndexOf('.') + 1));
			}
			
			public String getMimeType(File arg0) throws Exception {
				return getMimeType(null, arg0.getName());
			}
		}, null, new ITempFileManager() {		
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
			public URI normalizeReference(String reference, Charset charset) {
				return normalizeReference(reference);
			}
		});
		ParserContext.setCurrentContext(this.parserContext);		
	}
	
	public void testFiles() throws Exception {
		final LhaParser lp = new LhaParser();
		final File resources = new File("src/test/resources");
		final URI location = URI.create("http://www.example.org/");
		final String charset = null;
		for (final String tf : TEST_FILES) {
			final File file = new File(resources, tf);
			final IParserDocument pdoc = lp.parse(location, charset, file);
			//System.out.println("file '" + tf + "':");
			// printParserDoc(pdoc, "ROOT");
		}
	}
	
	private static void printParserDoc(final IParserDocument pdoc, final String name) throws IOException {
		final Reader r = pdoc.getTextAsReader();
		System.out.println(name);
		if (r == null) {
			System.out.println("null");
			return;
		}
		final BufferedReader br = new BufferedReader(r);
		try {
			String line;
			while ((line = br.readLine()) != null)
				System.out.println(line);
		} finally { br.close(); }
		System.out.println();
		System.out.println("-----------------------------------");
		for (final Map.Entry<String,IParserDocument> sd : pdoc.getSubDocs().entrySet())
			printParserDoc(sd.getValue(), sd.getKey());
	}
}
