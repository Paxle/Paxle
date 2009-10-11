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
package org.paxle.core.doc.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;

import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICommandProfile;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempDir;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.io.temp.impl.TempFileManager;

public class BasicDocumentFactoryTest extends TestCase {
	private ITempFileManager tmpFileManager;
	private IDocumentFactory docFactory;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.tmpFileManager = new TempFileManager();
		this.docFactory = new BasicDocumentFactory() {{
			this.tempFileManager = tmpFileManager;
		}};
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		// cleanup temp files
		final Map<File,ITempDir> tempFiles = ((TempFileManager)this.tmpFileManager).getFileMap();
		if (tempFiles != null) {
			for (File file : tempFiles.keySet()) {
				assertTrue(file.delete());
			}
		}
	}
	
	public void testCreateCommand() throws IOException {
		ICommand cmd = this.docFactory.createDocument(ICommand.class);
		assertNotNull(cmd);
		assertEquals(0, cmd.getOID());
		assertEquals(-1, cmd.getProfileOID());
	}
	
	public void testCreateCommandProfile() throws IOException {
		ICommandProfile profile = this.docFactory.createDocument(ICommandProfile.class);
		assertNotNull(profile);
	}
	
	public void testCreateCrawlerDocument() throws IOException {
		ICrawlerDocument cdoc = this.docFactory.createDocument(ICrawlerDocument.class);
		assertNotNull(cdoc);
	}
	
	public void testCreateParserDocument() throws IOException {
		IParserDocument pdoc = this.docFactory.createDocument(IParserDocument.class);
		assertNotNull(pdoc);
	}
	
	public void testCreateIndexerDocument() throws IOException {
		IIndexerDocument idoc = this.docFactory.createDocument(IIndexerDocument.class);
		assertNotNull(idoc);
	}
}
