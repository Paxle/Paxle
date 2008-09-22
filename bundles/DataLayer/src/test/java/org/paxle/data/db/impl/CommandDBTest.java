package org.paxle.data.db.impl;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandTracker;

public class CommandDBTest extends MockObjectTestCase {
	private ICommandTracker cmdTracker;
	private CommandDB cmdDB;
	
	/**
	 * @return the hibernate config file to use
	 * @throws MalformedURLException 
	 */
	private URL getConfigFile() throws MalformedURLException {
		//final File derbyConfigFile = new File("../DataLayerDerby/src/main/resources/resources/hibernate/derby.cfg.xml");
		final File derbyConfigFile = new File("../DataLayerH2/src/main/resources/resources/hibernate/H2.cfg.xml");
		assertTrue(derbyConfigFile.exists());
		return derbyConfigFile.toURL();
	}
	
	/**
	 * @return the hibernate mapping files to use
	 * @throws MalformedURLException 
	 */
	private List<URL> getMappingFiles() throws MalformedURLException {
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
		
		return mappingFileURLs;
	}
	
	/**
	 * @return additional properties that should be passed to hibernate
	 */
	private Properties getExtraProperties() {
		Properties props = new Properties();
		//props.put("connection.url", String.format("jdbc:derby:target/command-db;create=true"));
		//props.put("hibernate.connection.url", String.format("jdbc:derby:target/command-db;create=true"));
		props.put("connection.url", String.format("jdbc:h2:target/command-db/cdb"));
		props.put("hibernate.connection.url", String.format("jdbc:h2:target/command-db/cdb"));
		return props;
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// create a dummy command tracker
		this.cmdTracker = mock(ICommandTracker.class);
		
		// create and init the command-db
		this.cmdDB = new CommandDB(
				this.getConfigFile(),
				this.getMappingFiles(),
				this.getExtraProperties(),
				this.cmdTracker		
		);
		
		// startup DB
		this.cmdDB.start();
	}
	
	@Override
	protected void tearDown() throws Exception {
		// close DB
		this.cmdDB.close();
		
		// delete data directory
		File dbDir = new File("target/command-db");
		assertTrue(dbDir.exists());
		FileUtils.deleteDirectory(dbDir);

		super.tearDown();
	}
	
	/**
	 * A dummy data-sink which just prints out the data
	 */
	private class DummyDataSink implements IDataSink<ICommand> {
		private final Semaphore semaphore;
		public DummyDataSink(Semaphore semaphore) {
			this.semaphore = semaphore;
		}
		
		public void putData(ICommand cmd) throws Exception {
			System.out.println("New Command enqueued: " + cmd.getLocation().toASCIIString());
			this.semaphore.release();
		}
	}

	public void testStoreUnknownLocation() throws InterruptedException {
		final int MAX = 10;
		
		// command-tracker must be called MAX times
		checking(new Expectations() {{
			exactly(MAX).of(cmdTracker).commandCreated(with(equal("org.paxle.data.db.ICommandDB")), with(any(ICommand.class)));
		}});
		
		// store new commands
		ArrayList<URI> testURI = new ArrayList<URI>();
		for (int i=0; i < MAX; i++) {
			testURI.add(URI.create("http://test.paxle.net/" + i));
		}		
		int known = this.cmdDB.storeUnknownLocations(0, 1, testURI);
		assertEquals(0, known);

		// create a dummy data-sink
		Semaphore s = null;
		this.cmdDB.setDataSink(new DummyDataSink(s = new Semaphore(-MAX + 1)));		
		
		// wait for all commans to be enqueued
		boolean acquired = s.tryAcquire(3, TimeUnit.SECONDS);
		assertTrue(acquired);
	}
}
