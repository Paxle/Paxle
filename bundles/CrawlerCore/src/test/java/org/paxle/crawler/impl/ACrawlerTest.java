/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.crawler.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.osgi.framework.InvalidSyntaxException;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.impl.BasicDocumentFactory;
import org.paxle.core.io.temp.ITempDir;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.crawler.CrawlerContext;

public abstract class ACrawlerTest extends MockObjectTestCase {

	protected ICrawlerDocument crawlerDoc;
	
	protected ITempFileManager aTempManager;
	
	protected IDocumentFactory docFactory;
	
	protected String[] mimeTypes = new String[] {"text/html"};
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
						
		// configuring some system properties
		System.setProperty("paxle.userAgent","PaxleFramework");
		System.setProperty("paxle.version", "0.1.0");		
		
		// create a dummy crawler context
		this.initCrawlerContext(mimeTypes);		
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		// cleanup temp-file
		if (this.aTempManager != null && this.crawlerDoc != null && this.crawlerDoc.getContent() != null) {
			this.aTempManager.releaseTempFile(this.crawlerDoc.getContent());
		}		
	}
	
	protected void initCrawlerContext(final String[] mimeTypes) {
		// creating a dummy temp-file manager
		this.aTempManager = new TestTempFileManager();

		// a dummy doc factory
		this.docFactory = new BasicDocumentFactory();
		
		// initializing the crawler context
		CrawlerContextLocal threadLocal = new TestCrawlerContextLocal(mimeTypes, this.aTempManager);
		CrawlerContext.setThreadLocal(threadLocal);
	}
	
	private static class TestTempFileManager implements ITempFileManager {
		public File createTempFile() throws IOException {
			File tmp = File.createTempFile("test", ".tmp");
			tmp.deleteOnExit();
			return tmp;
		}
		public void releaseTempFile(File file) throws FileNotFoundException, IOException {
			if (!file.delete()) throw new IOException("Unable to delete file: " + file);				
		}
		public void removeTempDirFor(String... arg0) { }
		public void setTempDirFor(ITempDir arg0, String... arg1) { }
		public boolean isKnown(File file) { return true; }		
	}
	
	private class TestCrawlerContextLocal extends CrawlerContextLocal {
		public TestCrawlerContextLocal(String[] mimeTypes, ITempFileManager tempFileManager) {
			// all mimetypes supported in the system
			for (String mimeType : mimeTypes) {
				this.supportedMimeTypes.add(mimeType);
			}
			
			this.tempFileManager = tempFileManager;	
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected <DOC> DOC createDocumentForInterface(Class<DOC> docInterface, String filter) throws InvalidSyntaxException {
			if (docInterface.isAssignableFrom(ICrawlerDocument.class)) {
				return (DOC) docFactory.createDocument(ICrawlerDocument.class);
			}
			throw new IllegalArgumentException("Unexpected invocation");
		}
	}
}
