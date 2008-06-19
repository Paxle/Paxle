package org.paxle.core.doc;

import org.paxle.core.doc.LinkInfo.Status;

import junit.framework.TestCase;

public class LinkInfoTest extends TestCase {
	public void testDefaultStatus() {
		LinkInfo info = new LinkInfo();
		assertNotNull(info.getStatus());
		assertEquals(Status.OK, info.getStatus());
	}
	
	public void testNullStatusIsOK() {
		LinkInfo info = new LinkInfo();
		info.setStatus(null);
		assertNotNull(info.getStatus());
		assertEquals(Status.OK, info.getStatus());
	}
	
	public void testToString() {
		LinkInfo dummy = new LinkInfo("test", LinkInfo.Status.FILTERED, "blocked URI");
		assertNotNull(dummy.toString());
	}
}
