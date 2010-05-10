/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.parser.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.osgi.framework.InvalidSyntaxException;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.impl.BasicDocumentFactory;
import org.paxle.core.io.temp.ITempDir;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.parser.IParserContextLocal;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;
import org.paxle.parser.ParserContext;


public abstract class AParserTest extends MockObjectTestCase {
	private HashMap<String,String> fileNameToMimeTypeMap = null;
	private HashMap<String, ISubParser> mimeTypeToParserMap = null;
	
	/**
	 * A dummy {@link ITempFileManager}
	 */
	private ITempFileManager aTempFileManager = null;
	
	/**
	 * A dummy {@link IReferenceNormalizer}
	 */
	private IReferenceNormalizer aRefNormalizer = null;
	
	/**
	 * A dummy {@link IMimeTypeDetector}.
	 * 
	 * User {@link #registerMimeTypeForFile(String, String)} to register the
	 * mime-type that should be returned for a given file-name. 
	 */
	private IMimeTypeDetector aMimetypeDetector = null;
	
	/**
	 * A dummy {@link ISubParserManager}.
	 * 
	 * User {@link #registerParserForMimeType(String, ISubParser)} to register
	 * the {@link ISubParser} that should be returned for a given mime-type
	 */
	private ISubParserManager aSubParserManager = null;
	
	/**
	 * A {@link IDocumentFactory document-factory}.
	 * This just returns an instance of {@link BasicDocumentFactory}.
	 */
	private IDocumentFactory docFactory;
	
	/**
	 * A dummy {@link IParserContextLocal}
	 */
	private IParserContextLocal parserContextLocal = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.fileNameToMimeTypeMap = new HashMap<String,String>();
		this.mimeTypeToParserMap = new HashMap<String, ISubParser>();
		
		// a dummy temp-file managager
		this.aTempFileManager = new TestTempFileManager();
		
		// a dummy reference normalizer
		this.aRefNormalizer = new IReferenceNormalizer() {
			public URI normalizeReference(String reference) {
				try {
					return new URI(reference);
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException(e.getMessage() + " for '" + reference + "'", e);
				}
			}
			
			public URI normalizeReference(String reference, Charset charset) {
				return normalizeReference(reference);
			}
			
			public int getDefaultPort(String protocol) {
				return 0;
			}
		};
		
		// a dummy mime-type detector
		this.aMimetypeDetector = new IMimeTypeDetector() {
			public String getMimeType(byte[] arg0, String fileName) throws Exception {
				return fileNameToMimeTypeMap.get(fileName);
			}
			
			public String getMimeType(File file) throws Exception {
				return getMimeType(null, file.getName());
			}
		};
		
		this.aSubParserManager = new ISubParserManager() {			
			public void disableMimeType(String arg0) {
				throw new UnsupportedOperationException("Method not implemented");
			}			
			public void enableMimeType(String arg0) {
				throw new UnsupportedOperationException("Method not implemented");
			}
			
			public Set<String> disabledMimeTypes() {
				throw new UnsupportedOperationException("Method not implemented");
			}
			
			public Collection<String> getMimeTypes() {
				return mimeTypeToParserMap.keySet();
			}
			
			public ISubParser getSubParser(String mimeType) {
				return mimeTypeToParserMap.get(mimeType);
			}
			
			public Map<String, ISubParser> getSubParsers() {
				throw new UnsupportedOperationException("Method not implemented");
			}

			public Collection<ISubParser> getSubParsers(String mimeType) {
				ISubParser sp = this.getSubParser(mimeType);
				if (sp == null) return Collections.emptyList();
				return Arrays.asList(new ISubParser[]{sp});
			}			
			
			public boolean isSupported(String mimeType) {
				return true;
			}
			
			public void disableParser(String service) {
				throw new UnsupportedOperationException("Method not implemented");
			}
			
			public void enableParser(String service) {
				throw new UnsupportedOperationException("Method not implemented");				
			}
			
			public Set<String> enabledParsers() {
				throw new UnsupportedOperationException("Method not implemented");
			}
			
			public Map<String,Set<String>> getParsers() {
				throw new UnsupportedOperationException("Method not implemented");
			}
			public void close() {
				throw new UnsupportedOperationException("Method not implemented");
			}
		};
		
		// a document factory
		this.docFactory = new BasicDocumentFactory() {{
			this.tempFileManager = getTempFileManager();
			activate(Collections.EMPTY_MAP);
		}};
		
		// create a parser context with a dummy temp-file-manager	
		this.parserContextLocal = new TestParserContextLocale();
		ParserContext.setThreadLocal((TestParserContextLocale)this.parserContextLocal);
	}
	
	/**
	 * Returns a dummy {@link ITempFileManager}
	 */
	protected ITempFileManager getTempFileManager() {
		return this.aTempFileManager;
	}
	
	/**
	 * Returns a dummy {@link IReferenceNormalizer}
	 */
	protected IReferenceNormalizer getReferenceNormalizer() {
		return this.aRefNormalizer;
	}
	
	/**
	 * Returns a dummy {@link IMimeTypeDetector}.
	 * 
	 * User {@link #registerMimeTypeForFile(String, String)} to register the
	 * mime-type that should be returned for a given file-name. 
	 */
	protected IMimeTypeDetector getMimeTypeDetector() {
		return this.aMimetypeDetector;
	}
	
	/**
	 * Returns a dummy {@link ISubParserManager}.
	 * 
	 * User {@link #registerParserForMimeType(String, ISubParser)} to register
	 * the {@link ISubParser} that should be returned for a given mime-type
	 */
	protected ISubParserManager getSubParserManager() {
		return this.aSubParserManager;
	}
	
	protected IDocumentFactory getDocumentFactory() {
		return this.docFactory;
	}
	
	protected IParserContextLocal getParserContextLocal() {
		return this.parserContextLocal;
	}
	
	protected void registerParserForMimeType(String mimeType, ISubParser parser) {
		this.mimeTypeToParserMap.put(mimeType,parser);
	}
	
	protected void registerMimeTypeForFile(String fileName, String mimeType) {
		this.fileNameToMimeTypeMap.put(fileName, mimeType);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		// deleting all previously created temp-file
		// cleanup temp-file
		for (File tempFile : ((TestTempFileManager)aTempFileManager).tempFiles)	{
			if (tempFile.exists() && !tempFile.delete()) 
				throw new IOException("Unable to delte file: " + tempFile);
		}	
	}
	
	/**
	 * A dummy temp-file-manager which deletes all his files on VM exit
	 */	
	private static class TestTempFileManager implements ITempFileManager {
		public List<File> tempFiles = new ArrayList<File>();
		
		public File createTempFile() throws IOException {
			File tmp = File.createTempFile("test", ".tmp");
			tmp.deleteOnExit();
			this.tempFiles.add(tmp);
			return tmp;
		}
		public void releaseTempFile(File file) throws FileNotFoundException, IOException {
			this.tempFiles.remove(file);
			if (file.exists() && !file.delete()) throw new IOException("Unable to delete file: " + file);				
		}
		public boolean isKnown(File file) { 
			return this.tempFiles.contains(file); 
		}			
		public void removeTempDirFor(String... arg0) { }
		public void setTempDirFor(ITempDir arg0, String... arg1) { }	
	}	
	
	/**
	 * A dummy {@link ParserContextLocal}
	 */
	private class TestParserContextLocale extends ParserContextLocal {
		public TestParserContextLocale() {
			this.subParserManager = getSubParserManager();
			this.mimeTypeDetector = getMimeTypeDetector();
			this.charsetDetector = null;
			this.tempFileManager = getTempFileManager();
			this.ioTools = new org.paxle.core.io.impl.IOTools();
			this.referenceNormalizer = getReferenceNormalizer();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected <DOC> DOC createDocumentForInterface(Class<DOC> docInterface, String filter) throws InvalidSyntaxException, IOException {
			if (docInterface.isAssignableFrom(IParserDocument.class)) {
				return (DOC) docFactory.createDocument(IParserDocument.class);
			}
			throw new IllegalArgumentException("Unexpected invocation");
		}
	}
	
	
	protected void printParserDoc(final IParserDocument pdoc, final String name) throws IOException {
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
