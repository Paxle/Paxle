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
package org.paxle.filter.languageidentification.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;
import java.util.Set;

import junit.framework.TestCase;

import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.impl.BasicCommand;
import org.paxle.core.doc.impl.BasicParserDocument;
import org.paxle.core.io.temp.impl.TempFileManager;

public class LanguageManagerTest extends TestCase {
	private LanguageManager lngmanager;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		Hashtable<String, Object> config = new Hashtable<String, Object>();
		
		config.put(LanguageManager.SDT, new Float(0.6));
		
		// create language manager
				this.lngmanager = new LanguageManager();
				this.lngmanager.init(config);
	}
	
	private ICommand createCommand(URI location) {
		ICommand cmd = new BasicCommand();
		cmd.setLocation(location);
		return cmd;
	}
	
	private ICommand createTestCommand(File testText) throws IOException {
		ICommand command = this.createCommand(URI.create("http://xyz.abc"));
		
		IParserDocument pdoc = new BasicParserDocument(new TempFileManager());
		pdoc.setOID((int) System.currentTimeMillis());
		pdoc.setStatus(IParserDocument.Status.OK);
		pdoc.setTextFile(testText);
		command.setParserDocument(pdoc);
		
		return command;
	}
	
	public void testProcessNullPDoc() {
		ICommand command = this.createCommand(URI.create("http://xyz.abc"));
		this.lngmanager.filter(command, null);
	}
	
	public void testProcessNotOKPdoc() {
		ICommand command = this.createCommand(URI.create("http://xyz.abc"));
		IParserDocument pdoc = new BasicParserDocument(new TempFileManager());
		pdoc.setStatus(IParserDocument.Status.FAILURE);
		command.setParserDocument(pdoc);
		this.lngmanager.filter(command, null);
	}
	
	public void testDetectDePdoc() throws IOException {
		final File testFile = new File("src/test/resources/text_de.txt");		
		final ICommand command = this.createTestCommand(testFile);
		
		this.lngmanager.filter(command, null);
		Set<String> lngs = command.getParserDocument().getLanguages();
		assertNotNull(lngs);
		assertEquals(1, lngs.size());
		assertTrue(lngs.contains("de"));
	}
	
	public void testDetectEnPdoc() throws IOException {
		final File testFile = new File("src/test/resources/text_en.txt");		
		final ICommand command = this.createTestCommand(testFile);
		
		this.lngmanager.filter(command, null);
		Set<String> lngs = command.getParserDocument().getLanguages();
		assertNotNull(lngs);
		assertEquals(1, lngs.size());
		assertTrue(lngs.contains("en"));
	}
}
