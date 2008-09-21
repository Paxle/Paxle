package org.paxle.data.db.impl;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.queue.ICommandTracker;

public class CommandDBTest extends MockObjectTestCase {
	private ICommandTracker cmdTracker;
	private CommandDB cmdDB;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.cmdTracker = mock(ICommandTracker.class);
		
		final File derbyConfigFile = new File("../DataLayerDerby/src/main/resources/resources/hibernate/derby.cfg.xml");
		assertTrue(derbyConfigFile.exists());
		
		final File mappingFilesDir = new File("src/main/resources/resources/hibernate/mapping/command/");
		assertTrue(mappingFilesDir.exists());
		
		final FileFilter mappingFileFilter = new WildcardFileFilter("*.hbm.xml");		
		File[] mappingFiles = mappingFilesDir.listFiles(mappingFileFilter);
		assertNotNull(mappingFiles);
		assertEquals(4, mappingFiles.length);
		
		List<URL> mappingFileURLs = new ArrayList<URL>();
		for (File mappingFile : mappingFiles) {
			mappingFileURLs.add(mappingFile.toURL());
		}
		
//		this.cmdDB = new CommandDB(
//				derbyConfigFile.toURL(),
//				mappingFileURLs,
//				this.cmdTracker
//		);
	}

	public void test() {
		// TODO:
	}
}
