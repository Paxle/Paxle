package org.paxle.se.index.lucene.impl;

import junit.framework.TestCase;
import junitx.framework.ArrayAssert;

public class PaxleNumberToolsTest extends TestCase {

	private int num = 1156 * 256 + 255;
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testToBytes() {				
		byte[] expected = {-1, -124, 4};
		byte[] actual = PaxleNumberTools.toBytes(num);
		ArrayAssert.assertEquals(expected, actual);
	}
	
	public void testToBinaryString() {
		byte[] d = PaxleNumberTools.toBytes(num);
		
		String actual = PaxleNumberTools.toBinaryString(d, true);
		String expected = "00000100 10000100 11111111";
		
		assertEquals(expected, actual);
	}
	
	public void testToLong() {
		byte[] d = PaxleNumberTools.toBytes(num);
		
		int actual = (int)PaxleNumberTools.toLong(d);
		int expected = 296191;
		
		assertEquals(expected, actual);
	}
}
