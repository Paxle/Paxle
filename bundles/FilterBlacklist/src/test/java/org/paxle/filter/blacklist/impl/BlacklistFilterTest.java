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
		testDir.deleteOnExit();
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
		
		BlacklistFilter.addPattern(".*asd.*", "testList");
		testCommand.setLocation(new URI("http://test/"));
		testFilter.filter(testCommand, null);
		assertTrue(testCommand.getResult().equals(ICommand.Result.Passed));
		
		testCommand.setLocation(new URI("http://asd/"));
		testFilter.filter(testCommand, null);
		assertTrue(testCommand.getResult().equals(ICommand.Result.Rejected));
	}

	public void testAddList() throws Exception {
		assertFalse(new File(testDir,"testList2").exists());
		
		BlacklistFilter.addList("testList2");
		assertTrue(new File(testDir,"testList2").exists());
	}
	
	protected void tearDown() throws Exception {
		new File(testDir,"testList").delete();
		new File(testDir,"testList2").delete();
	}
}
