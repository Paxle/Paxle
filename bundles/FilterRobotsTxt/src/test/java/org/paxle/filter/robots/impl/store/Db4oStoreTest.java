package org.paxle.filter.robots.impl.store;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.paxle.filter.robots.impl.rules.RobotsTxt;

import junit.framework.TestCase;

public class Db4oStoreTest extends TestCase {

	private File tempFile;
	private IRuleStore store;
	
	protected void setUp() throws Exception {
		super.setUp();
		this.tempFile = new File("target/temp" + System.currentTimeMillis());
		this.store = new Db4oStore(null, this.tempFile);
	}

	@Override
	protected void tearDown() throws Exception {
		this.store.close();
		this.tempFile.delete();
		super.tearDown();
	}
	
	public void testReadWriteRobotsTxt() throws IOException {
		String hostPortPrefix = "localhost:80";
		for (int i=0; i < 10; i++) {
			RobotsTxt test = new RobotsTxt(hostPortPrefix + i,RobotsTxt.RELOAD_INTERVAL_DEFAULT, "OK");
			this.store.write(test);
		}
		
		for (int i=0; i < 10; i++) {
			RobotsTxt found = this.store.read(hostPortPrefix + i);
			assertNotNull(found);
		}
	}
	
	public void testOutdatedRobotsCleanup() throws IOException, InterruptedException {
		// create outdated robots.txt entry
		RobotsTxt test = new RobotsTxt("outdatedRobots:80",0, "OK");
		assertTrue(test.getExpirationDate().before(new Date(System.currentTimeMillis()+1)));
		
		// store it into db
		this.store.write(test);
		
		// run cleanup task		
		((Db4oStore)this.store).runCleanup();
		
		// ensure that the outdated entry was deleted
		Thread.sleep(500);
		RobotsTxt found = this.store.read("outdatedRobots:80");
		assertNull(found);
	}
}
