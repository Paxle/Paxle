package org.paxle.icon.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.paxle.core.io.IOTools;

import junit.framework.TestCase;
import junitx.framework.ArrayAssert;


public class IconToolTest extends TestCase {
	private static final int TESTCASE_FILENAME = 0;
	private static final int TESTCASE_MIMETYPE = 1;
	
	/**
	 * Testfiles to test
	 */
	private static final String[][] testCases = new String[][] {
		{"test.html","text/html"},
		{"test.txt","text/plain"},
		{"test.pdf","application/pdf"}
	};
	
	public void _testInvalidFavicon() throws MalformedURLException {
		// this website delivers the favicon.ico as html!
		URL url = new URL("http://www.forum.nokia.com/document/Java_ME_Developers_Library_v1/index.html?content=GUID-545CA84A-8378-4DFA-9035-94479F5BE26E.html");
		
		// try to fetch the favicon
		IconTool.getIcon(url);
	}
	
	private byte[] loadIconData(String contentType) throws IOException {
		String fileName = IconTool.iconMap.getProperty(contentType);
		assertNotNull(fileName);
		
		// get icon stream
		InputStream iconInput = IconTool.class.getResourceAsStream("/resources/icons/" + fileName);
		assertNotNull(iconInput);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		IOTools.copy(iconInput, bout);
		iconInput.close();
		bout.close();
		
		assertTrue(bout.size() > 0);
		return bout.toByteArray();
	}
	
	public void testNullURL() {
		IconData iconData = IconTool.getIcon(null);
		assertNotNull(iconData);
		assertNotNull(iconData.data);
		assertEquals(IconTool.defaultIcon, iconData);
	}
	
	public void testFileURLs() throws IOException {

		for (String[] testcase : testCases) {
			String fileName = testcase[TESTCASE_FILENAME];
			String mime = testcase[TESTCASE_MIMETYPE];

			IconData iconData = IconTool.getIcon(new URL("file:///xyz/" + fileName));
			assertNotNull(iconData);
			assertNotNull(iconData.data);

			byte[] actualBytes = iconData.data;
			byte[] expectedBytes = this.loadIconData(mime);

			ArrayAssert.assertEquals(expectedBytes, actualBytes);
		}
	}
}
