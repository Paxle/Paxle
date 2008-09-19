
package org.paxle.parser.feed.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.IOTools;
import org.paxle.core.io.temp.ITempDir;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;

import junit.framework.TestCase;

public class FeedParserTest extends TestCase {
	
	protected ParserContext parserContext = null;
	private ITempFileManager tfm = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// create a parser context with a dummy temp-file-manager
		tfm = new ITempFileManager() {		
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

		};
		final ISubParserManager spm = new ISubParserManager() {
			public Set<String> disabledMimeTypes() { return null; }
			public void disableMimeType(String arg0) { }
			public void enableMimeType(String arg0) { }
			
			public Collection<String> getMimeTypes() {
				return Arrays.asList("text/html");
			}
			
			public ISubParser getSubParser(String arg0) {
				return (arg0.equals("text/html")) ? new ISubParser() {
					public List<String> getMimeTypes() {
						return Arrays.asList("text/html");
					}
					
					public IParserDocument parse(URI arg0, String arg1,
							File arg2) throws ParserException,
							UnsupportedEncodingException, IOException {
						final IParserDocument pdoc = new CachedParserDocument(tfm);
						pdoc.setTextFile(arg2);
						return pdoc;
					}
					
					public IParserDocument parse(URI arg0, String arg1,
							InputStream arg2) throws ParserException,
							UnsupportedEncodingException, IOException {
						final IParserDocument pdoc = new CachedParserDocument(tfm);
						final StringBuilder sb = new StringBuilder();
						IOTools.copy(new InputStreamReader(arg2), sb);
						pdoc.addText(sb);
						return pdoc;
					}
				} : null;
			}
			
			public Collection<ISubParser> getSubParsers() {
				return Arrays.asList(getSubParser("text/html"));
			}
			
			public boolean isSupported(String arg0) {
				return arg0.equals("text/html");
			}
		};
		IOTools.setTempFileManager(tfm);
		this.parserContext = new ParserContext(spm, null,null, tfm, new IReferenceNormalizer() {
			public URI normalizeReference(String reference) {
				return URI.create(reference);
			}
			public URI normalizeReference(String reference, Charset charset) {
				return normalizeReference(reference);
			}
		});
		ParserContext.setCurrentContext(this.parserContext);		
	}

	private static final File RESOURCES = new File("src/test/resources");
	
	public static void testParseRss() throws Exception {
		final FeedParser parser = new FeedParser();
		final IParserDocument pdoc = parser.parse(URI.create("http://www.virtualdub.org/"), null, new File(RESOURCES, "rss.xml"));
		assertEquals(2, pdoc.getSubDocs().size());
		/*
		System.out.println(pdoc);
		for (Map.Entry<String,IParserDocument> subdoc : pdoc.getSubDocs().entrySet()) {
			System.out.println(" ------------------------ " + subdoc.getKey() + " ------------------------");
			System.out.println(subdoc.getValue());
			final StringBuilder sb = new StringBuilder();
			Reader reader = subdoc.getValue().getTextAsReader();
			if (reader == null)
				reader = new FileReader(subdoc.getValue().getTextFile());
			try {
				IOTools.copy(reader, sb);
			} finally { reader.close(); }
			System.out.println("Text:");
			System.out.println(sb);
		}*/
	}
}
