package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.net.URI;

import org.paxle.core.queue.Command;
import org.paxle.core.queue.ICommand;

import junit.framework.TestCase;

public class BlacklistFilterTest extends TestCase {

	private File testDir = null;
	private BlacklistFilter testFilter;

	protected void setUp() throws Exception {
		super.setUp();
		testDir = new File("testDir");
		testDir.mkdir();
		System.out.println(testDir.getAbsolutePath());
		new File(testDir, "testList").createNewFile();
		testFilter = new BlacklistFilter(testDir);
	}

	public void testIsListed() throws Exception {
		ICommand testCommand = new Command();
		testCommand.setLocation(new URI("http://test/"));
		testFilter.filter(testCommand, null);
		assertTrue(testCommand.getResult().equals(ICommand.Result.Passed));

		testCommand.setLocation(new URI("http://asd/"));
		testFilter.filter(testCommand, null);
		assertTrue(testCommand.getResult().equals(ICommand.Result.Passed));

		Blacklist testList = testFilter.createList("testList");
		testList.addPattern(".*asd.*");
		testCommand.setLocation(new URI("http://test/"));
		testFilter.filter(testCommand, null);
		assertTrue(testCommand.getResult().equals(ICommand.Result.Passed));

		testCommand.setLocation(new URI("http://asd/"));
		testFilter.filter(testCommand, null);
		assertTrue(testCommand.getResult().equals(ICommand.Result.Rejected));

		testCommand = new Command();
		testCommand.setLocation(new URI("http://asd/"));		
		testList.removePattern(".*asd.*");
		testFilter.filter(testCommand, null);
		assertTrue(testCommand.getResult().equals(ICommand.Result.Passed));
	}

	public void testAddList() throws Exception {
		assertFalse(new File(testDir,"testList2").exists());

		testFilter.createList("testList2");
		assertTrue(new File(testDir,"testList2").exists());
	}

	public void testIsListnameAllowed() throws Exception {
		InvalidFilenameException ex = null;

		try  {
			testFilter.createList("../test");
		} catch (InvalidFilenameException e) {
			ex = e;
		}
		assertNotNull(ex);
		ex = null;

		try  {
			testFilter.createList("./test");
		} catch (InvalidFilenameException e) {
			ex = e;
		}
		assertNotNull(ex);
		ex = null;

		try  {
			testFilter.createList("");
		} catch (InvalidFilenameException e) {
			ex = e;
		}
		assertNotNull(ex);
		ex = null;

		try  {
			testFilter.createList("     ");
		} catch (InvalidFilenameException e) {
			ex = e;
		}
		assertNotNull(ex);
		ex = null;

		try {
			testFilter.createList("test");
		} catch (InvalidFilenameException e) {
			ex = e;
		}
		assertNull(ex);
	}

	protected void tearDown() throws Exception {
		new File(testDir,"testList").delete();
		new File(testDir,"testList2").delete();
		new File(testDir,"test").delete();
		testDir.delete();
	}
}
