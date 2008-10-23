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

package org.paxle.filterlanguagedetection.impl;

import org.paxle.filter.languageidentification.impl.NGramSet;

import junit.framework.TestCase;

public class TrigramSetTest extends TestCase {

	NGramSet ts = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ts = new NGramSet();
	}
	
	public void testInits() {
		ts.init("", -1);
		assertEquals(ts.getNumberOfNGrams(), 0);
		ts.init("xxx", -1);
		assertEquals(ts.getNumberOfNGrams(), 1);
		ts.init("xxxxxxxxxxxxxxxxxxxxx", -1);
		assertEquals(ts.getNumberOfNGrams(), 1);
		ts.init("abcd", -1);
		assertEquals(ts.getNumberOfNGrams(), 2);
	}
	
}
