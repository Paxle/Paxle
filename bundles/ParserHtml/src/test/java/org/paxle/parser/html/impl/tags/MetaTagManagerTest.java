
package org.paxle.parser.html.impl.tags;

import java.util.regex.Matcher;

import junit.framework.TestCase;

public class MetaTagManagerTest extends TestCase {
	
	private static final String RESULT = "http://de.selfhtml.org/";
	
	private static final String[] TEST_CASES = {
		" 75; URL=http://de.selfhtml.org/\t",
		"URL=http://de.selfhtml.org/",
		" http://de.selfhtml.org/   "
	};
	
	public static void testRefresh() {
		for (final String test : TEST_CASES) {
			final Matcher m = MetaTagManager.REFRESH_PATTERN.matcher(test);
			assertTrue(m.find());
			assertEquals(m.group(3), RESULT);
		}
	}
}
