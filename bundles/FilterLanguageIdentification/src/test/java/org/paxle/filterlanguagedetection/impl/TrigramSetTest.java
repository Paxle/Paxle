package org.paxle.filterlanguagedetection.impl;

import org.paxle.filter.languageidentification.impl.TrigramSet;

import junit.framework.TestCase;

public class TrigramSetTest extends TestCase {

	TrigramSet ts = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ts = new TrigramSet();
	}
	
	public void testInits() {
		ts.init("", -1);
		assertEquals(ts.getNumberOfTrigrams(), 0);
		ts.init("xxx", -1);
		assertEquals(ts.getNumberOfTrigrams(), 1);
		ts.init("xxxxxxxxxxxxxxxxxxxxx", -1);
		assertEquals(ts.getNumberOfTrigrams(), 1);
		ts.init("abcd", -1);
		assertEquals(ts.getNumberOfTrigrams(), 2);
	}
	
}
