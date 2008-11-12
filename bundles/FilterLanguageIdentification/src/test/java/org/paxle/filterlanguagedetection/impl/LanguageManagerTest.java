/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.filterlanguagedetection.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.ParserDocument;
import org.paxle.core.queue.Command;
import org.paxle.core.queue.ICommand;
import org.paxle.filter.languageidentification.impl.LanguageManager;

public class LanguageManagerTest extends TestCase {
	private LanguageManager lngmanager;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// create language manager
		this.lngmanager = new LanguageManager();
		
		// load profiles
		List<URL> profiles = getProfileFiles();
		for(URL profile : profiles) {
			this.lngmanager.loadNewLanguage(profile);
		}
	}
	
	/**
	 * @return the hibernate mapping files to use
	 * @throws MalformedURLException 
	 */
	private List<URL> getProfileFiles() throws MalformedURLException {
		final File profileFilesDir = new File("src/main/resources/profiles/");
		assertTrue(profileFilesDir.exists());
		
		final FileFilter profileFileFilter = new WildcardFileFilter("*.txt");		
		File[] profileFiles = profileFilesDir.listFiles(profileFileFilter);
		assertNotNull(profileFiles);
		
		List<URL> profileFileURLs = new ArrayList<URL>();
		for (File mappingFile : profileFiles) {
			profileFileURLs.add(mappingFile.toURL());
		}
		
		return profileFileURLs;
	}	
	
	private ICommand createTestCommand(File testText) throws IOException {
		ICommand command = Command.createCommand(URI.create("http://xyz.abc"));
		
		IParserDocument pdoc = new ParserDocument();
		pdoc.setOID((int) System.currentTimeMillis());
		pdoc.setStatus(IParserDocument.Status.OK);
		pdoc.setTextFile(testText);
		command.setParserDocument(pdoc);
		
		return command;
	}
	
	public void testProcessNullPDoc() {
		ICommand command = Command.createCommand(URI.create("http://xyz.abc"));
		this.lngmanager.filter(command, null);
	}
	
	public void testProcessNotOKPdoc() {
		ICommand command = Command.createCommand(URI.create("http://xyz.abc"));
		IParserDocument pdoc = new ParserDocument();
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
