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

import java.awt.Image;
import java.io.File;
import java.net.MalformedURLException;

import junit.framework.TestCase;

public class FaviconReaderTest extends TestCase {

	private FaviconReader faviconReader = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		this.faviconReader = new FaviconReader();
		this.faviconReader.activate();
	}

	@Override
	protected void tearDown() throws Exception {
		this.faviconReader.deactivate();
		super.tearDown();
	}
	
	public void testReadFavicon() throws MalformedURLException {
		final File paxleFavicon = new File("src/test/resources/paxle.ico");
		assertTrue(paxleFavicon.exists());
		
		final Image image = this.faviconReader.readIcoImage(paxleFavicon.toURI().toURL());
		assertNotNull(image);
		assertEquals(32,image.getHeight(null));
		assertEquals(32,image.getWidth(null));
	}
}
