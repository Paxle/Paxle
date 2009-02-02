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
package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.net.URI;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.doc.ParserDocument;
import org.paxle.core.queue.Command;
import org.paxle.core.queue.ICommand;

public class BlacklistFilterTest extends TestCase {
	private static final String TESTDIR_NAME = "target/testDir";

	private File testDir = null;
	private BlacklistFilter testFilter;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.testDir = new File(TESTDIR_NAME);
		this.testDir.mkdir();
		
		System.out.println("Using data dir: " + testDir.getAbsolutePath());
		new File(testDir, "testList").createNewFile();
		this.testFilter = new BlacklistFilter(this.testDir);
	}
	
	@Override
	protected void tearDown() throws Exception {
		// delete data directory
		if(this.testDir.exists()) FileUtils.deleteDirectory(testDir);
	}	

	public void testCommandIsListed() throws Exception {
		ICommand testCommand = new Command();
		testCommand.setLocation(new URI("http://test/"));
		testFilter.filter(testCommand, null);
		assertEquals(ICommand.Result.Passed, testCommand.getResult());

		testCommand.setLocation(new URI("http://asd/"));
		testFilter.filter(testCommand, null);
		assertEquals(ICommand.Result.Passed, testCommand.getResult());

		Blacklist testList = testFilter.createList("testList");
		testList.addPattern(".*asd.*");
		testCommand.setLocation(new URI("http://test/"));
		testFilter.filter(testCommand, null);
		assertEquals(ICommand.Result.Passed, testCommand.getResult());

		testCommand.setLocation(new URI("http://asd/"));
		testFilter.filter(testCommand, null);
		assertEquals(ICommand.Result.Rejected, testCommand.getResult());

		testCommand = new Command();
		testCommand.setLocation(new URI("http://asd/"));		
		testList.removePattern(".*asd.*");
		testFilter.filter(testCommand, null);
		assertEquals(ICommand.Result.Passed, testCommand.getResult());
	}
	
	public void testPDocUriIsListed() throws InvalidFilenameException {
		final URI okURI = URI.create("http://test/");
		final URI blockedURI = URI.create("http://asd/");
		
		// creating test command
		ICommand testCommand = new Command();
		testCommand.setLocation(URI.create("http://xyz/"));
		
		// creating dummy p-doc
		IParserDocument pDoc = new ParserDocument();
		pDoc.addReference(okURI, new LinkInfo());
		pDoc.addReference(blockedURI, new LinkInfo());
		testCommand.setParserDocument(pDoc);
		
		// creating blacklist
		Blacklist testList = this.testFilter.createList("testList");
		testList.addPattern(".*asd.*");
		
		// filter command
		this.testFilter.filter(testCommand, null);
		assertTrue(testCommand.getResult().equals(ICommand.Result.Passed));
		
		Map<URI, LinkInfo> links = pDoc.getLinks();
		assertEquals(2, links.size());
		
		assertTrue(links.containsKey(okURI));
		assertEquals(LinkInfo.Status.OK, links.get(okURI).getStatus());
		
		assertTrue(links.containsKey(blockedURI));
		assertEquals(LinkInfo.Status.FILTERED, links.get(blockedURI).getStatus());
	}

	public void testAddList() throws Exception {
		assertFalse(new File(testDir,"testList2").exists());

		testFilter.createList("testList2");
		assertTrue(new File(testDir,"testList2").exists());
	}

	public void testIsListnameAllowed() throws Exception {
		try  {
			testFilter.createList("../test");
			fail("An InvalidFilenameException was expected");
		} catch (InvalidFilenameException e) {
			// this is ok
		}

		try  {
			testFilter.createList("./test");
			fail("An InvalidFilenameException was expected");
		} catch (InvalidFilenameException e) {
			// this is ok
		}

		try  {
			testFilter.createList("");
			fail("An InvalidFilenameException was expected");
		} catch (InvalidFilenameException e) {
			// this is ok
		}

		try  {
			testFilter.createList("     ");
			fail("An InvalidFilenameException was expected");
		} catch (InvalidFilenameException e) {
			// this is ok
		}

		testFilter.createList("test");
	}
}
