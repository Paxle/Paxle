/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
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
