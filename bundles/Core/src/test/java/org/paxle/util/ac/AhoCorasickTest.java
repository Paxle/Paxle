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

package org.paxle.util.ac;

import junit.framework.TestCase;

public class AhoCorasickTest extends TestCase {
	
	public void testAdding() throws Exception {
    	final AhoCorasick<String> ac = new AhoCorasick<String>(new NodeFactoryFactory.LinkedNodeFactory<String>());
    	final String val1 = "str_ab"; ac.addPattern("ab".getBytes(), val1);
    	final String val2 = "str_bb"; ac.addPattern("bb".getBytes(), val2);
    	final String val3 = "str_abb"; ac.addPattern("abb".getBytes(), val3);
    	int i=0;
    	for (final SearchResult<String> r : ac.search("abbaab".getBytes())) {
    		switch (i++) {
    			case 0:
    				assertTrue(val1 == r.getValue());
    				assertTrue(0 == r.getMatchBegin());
    				break;
    			case 1:
    				assertTrue(val3 == r.getValue());
    				assertTrue(0 == r.getMatchBegin());
    				break;
    			case 2:
    				assertTrue(val2 == r.getValue());
    				assertTrue(1 == r.getMatchBegin());
    				break;
    			case 3:
    				assertTrue(val1 == r.getValue());
    				assertTrue(4 == r.getMatchBegin());
    				break;
    			default:
    				assertFalse("Matched too many patterns: " + i, true);
    		}
    	}
	}
}
