/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.tools.icon.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;
import junitx.framework.ArrayAssert;

import org.paxle.core.io.IIOTools;
import org.paxle.core.io.impl.IOTools;
import org.paxle.tools.icon.IIconData;
import org.paxle.tools.icon.impl.IconTool;


public class IconToolTest extends TestCase {
	private static final int TESTCASE_FILENAME = 0;
	private static final int TESTCASE_MIMETYPE = 1;
	
	private IconTool iconTool;
	private IIOTools iotools = new IOTools();
	
	/**
	 * Testfiles to test
	 */
	private static final String[][] testCases = new String[][] {
		{"test.html","text/html"},
		{"test.txt","text/plain"},
		{"test.pdf","application/pdf"}
	};
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// init icon tool
		this.iconTool = new IconTool(){{
			this.ioTool = iotools;
			this.activate(null);
		}};		
	}
	
	public void _testInvalidFavicon() throws MalformedURLException {
		// this website delivers the favicon.ico as html!
		URL url = new URL("http://www.forum.nokia.com/document/Java_ME_Developers_Library_v1/index.html?content=GUID-545CA84A-8378-4DFA-9035-94479F5BE26E.html");
		
		// try to fetch the favicon
		IIconData data = this.iconTool.getIcon(url);
		assertNotNull(data);
	}
	
	private byte[] loadIconData(String contentType) throws IOException {
		String fileName = this.iconTool.iconMap.getProperty(contentType);
		assertNotNull(fileName);
		
		// get icon stream
		InputStream iconInput = IconTool.class.getResourceAsStream("/resources/icons/" + fileName);
		assertNotNull(iconInput);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		iotools.copy(iconInput, bout);
		iconInput.close();
		bout.close();
		
		assertTrue(bout.size() > 0);
		return bout.toByteArray();
	}
	
	public void testNullURL() {
		IIconData iconData = this.iconTool.getIcon(null);
		assertNotNull(iconData);
		assertNotNull(iconData.getData());
		assertEquals(this.iconTool.defaultIcon, iconData);
	}
	
	public void testFileURLs() throws IOException {

		for (String[] testcase : testCases) {
			String fileName = testcase[TESTCASE_FILENAME];
			String mime = testcase[TESTCASE_MIMETYPE];

			IIconData iconData = this.iconTool.getIcon(new URL("file:///xyz/" + fileName));
			assertNotNull(iconData);
			assertNotNull(iconData.getData());

			byte[] actualBytes = iconData.getData();
			byte[] expectedBytes = this.loadIconData(mime);

			ArrayAssert.assertEquals(expectedBytes, actualBytes);
		}
	}
}
