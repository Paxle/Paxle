package org.paxle.filter.robots.impl.store;

import java.io.File;
import java.io.IOException;

import org.paxle.filter.robots.impl.rules.RobotsTxt;

import junit.framework.TestCase;

public class Db4oStoreTest extends TestCase {

	private IRuleStore store;
	
	protected void setUp() throws Exception {
		super.setUp();
		this.store = new Db4oStore(null, new File("target/temp" + System.currentTimeMillis()));
	}

	@Override
	protected void tearDown() throws Exception {
		this.store.close();
		super.tearDown();
	}
	
	public void testReadWriteRobotsTxt() throws IOException {
		String hostPortPrefix = "localhost:808";
		for (int i=0; i < 10; i++) {
			RobotsTxt test = new RobotsTxt(hostPortPrefix + i,RobotsTxt.RELOAD_INTERVAL_DEFAULT, "OK");
			this.store.write(test);
		}
		
		RobotsTxt found = this.store.read(hostPortPrefix + "9");
		assertNotNull(found);
		System.out.println(found);
	}
}
