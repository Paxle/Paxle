
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
