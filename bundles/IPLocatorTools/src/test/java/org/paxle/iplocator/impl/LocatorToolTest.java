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
package org.paxle.iplocator.impl;

import java.io.InputStream;
import java.util.Locale;

import org.paxle.iplocator.ILocatorTool;

import junit.framework.TestCase;

public class LocatorToolTest extends TestCase {
	private ILocatorTool locatorTool;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.locatorTool = new LocatorTool();
	}
	
	public void testGetLocale() {
		Locale locale = this.locatorTool.getLocale("java.sun.com");
		assertNotNull(locale);
		assertEquals("US", locale.getCountry());
		assertEquals("en", locale.getLanguage());
	}
	
	public void testGetIconForHost() {
		InputStream icon = this.locatorTool.getIcon("java.sun.com");
		assertNotNull(icon);
	}
	
	public void testGetIconForLocale() {
		Locale locale = this.locatorTool.getLocale("java.sun.com");
		assertNotNull(locale);
		
		InputStream icon = this.locatorTool.getIcon(locale);
		assertNotNull(icon);
	}	
}
