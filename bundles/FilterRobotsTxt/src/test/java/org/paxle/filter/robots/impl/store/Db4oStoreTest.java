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
		this.store = new Db4oStore(null, this.tempFile, 50000);
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
		
		assertEquals(10, store.size());
		
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
		assertEquals(1, store.size());
		
		// run cleanup task		
		((Db4oStore)this.store).runCleanup();
		
		// ensure that the outdated entry was deleted
		Thread.sleep(500);
		RobotsTxt found = this.store.read("outdatedRobots:80");
		assertNull(found);
		assertEquals(0, store.size());
	}
	
	   
	public void _testManyMoreRobotsEntries() throws IOException {
		final long MAX = 2000000;
		final long CHUNK = 500;

		long start = System.currentTimeMillis(), startChunk = System.currentTimeMillis();        

		String hostPortPrefix = "localhost:80";
		for (int i=1; i <= MAX; i++) {
			RobotsTxt test = new RobotsTxt(hostPortPrefix + i,RobotsTxt.RELOAD_INTERVAL_DEFAULT, "OK");
			this.store.write(test);
			if (i % CHUNK == 0) {                

				System.out.println(String.format(
						"%d entries written so far. %d entries written in %dms. %d ms/entry average",
						i,
						CHUNK,
						(System.currentTimeMillis()-startChunk),
						(System.currentTimeMillis()-start)/CHUNK
				));

				long startRead = System.currentTimeMillis();
				this.store.read(hostPortPrefix + i);
				System.out.println("Reading 1 entry took " + (System.currentTimeMillis()-startRead));

				startChunk = System.currentTimeMillis();
			}
		}
		System.out.println(String.format("Writing of %d entries took %s ms",MAX,System.currentTimeMillis()-start));

				// assertEquals(MAX, store.size());
	}
	
}
