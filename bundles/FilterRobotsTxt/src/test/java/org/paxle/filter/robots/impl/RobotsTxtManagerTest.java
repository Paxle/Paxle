package org.paxle.filter.robots.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

public class RobotsTxtManagerTest extends TestCase {
	
	private RobotsTxtManager manager = null;

	protected void setUp() throws Exception {
		super.setUp();
		
		this.manager = new RobotsTxtManager(new File("target/temp"));
	}

	protected void tearDown() throws Exception {
		this.manager.terminate();
		
		super.tearDown();
	}
	
	private RobotsTxt getRobotsTxt(File robotsTxtFile) throws IOException {
		assertTrue(robotsTxtFile.exists());
		assertTrue(robotsTxtFile.canRead());
		
		URL robotsTxtURL = robotsTxtFile.toURL();
		return this.manager.parseRobotsTxt(robotsTxtURL.toString());	
	}
	
	public void testCache() {
		Cache cache = this.manager.getCache();
		assertNotNull(cache);
		assertEquals(0, cache.getSize());
		
		String hostport = "test";
		RobotsTxt rtxt = new RobotsTxt(hostport,-1,"OK");
		Element rtxtE = new Element(hostport,rtxt);
		cache.put(rtxtE);
		assertEquals(1, cache.getSize());
		
		rtxtE = cache.get(hostport);
		assertNotNull(rtxtE);
		assertEquals(hostport, rtxtE.getKey());
		rtxt = (RobotsTxt) rtxtE.getValue();
		assertNotNull(rtxt);
	}
	
	public void testParseRobotsTxt() throws IOException {
		File robotsTxtFile = new File("src/test/resources/robots.txt");
		RobotsTxt rtxt = this.getRobotsTxt(robotsTxtFile);
		assertNotNull(rtxt);
	}
	
	public void testParseRobotsTxtWithEmptyAgent() throws IOException {
		File robotsTxtFile = new File("src/test/resources/robots2.txt");
		RobotsTxt rtxt = this.getRobotsTxt(robotsTxtFile);
		assertNotNull(rtxt);
		assertEquals(2, rtxt.size());
		assertNotNull(rtxt.getRuleBlock(1));
		assertEquals(1, rtxt.getRuleBlock(1).agentsCount());
	}
	
	
	public void testIsDisallowed() throws IOException {
		// parse the robots.txt
		File robotsTxtFile = new File("src/test/resources/robots.txt");
		RobotsTxt rtxt = this.getRobotsTxt(robotsTxtFile);
		
		// append it to the cache to avoid real downloading of the robots.txt file
		Element e = new Element("xxxxx",rtxt);
		this.manager.getCache().put(e);
		
		// check disallowed
		boolean disallowed = this.manager.isDisallowed("http://xxxxx/");
		assertFalse(disallowed);
		
		disallowed = this.manager.isDisallowed("http://xxxxx/secret");
		assertFalse(disallowed);
		
		disallowed = this.manager.isDisallowed("http://xxxxx//secret/");
		assertTrue(disallowed);
	}
}
