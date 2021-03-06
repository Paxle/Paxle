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

package org.paxle.core.io.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import junitx.framework.ArrayAssert;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.osgi.framework.Bundle;
import org.paxle.core.io.IResourceBundleTool;

public class ResourceBundleToolTest extends MockObjectTestCase {
	private static final String RESOURCEBUNDLE_BASE = "IFilterManager";
	
	private static final URL[] RESOURCEBUNDLE_FILES = new URL[2];
	static {
		try {
			RESOURCEBUNDLE_FILES[0] = new File("src/main/resources/OSGI-INF/l10n/IFilterManager.properties").toURI().toURL();
			RESOURCEBUNDLE_FILES[1] = new File("src/main/resources/OSGI-INF/l10n/IFilterManager_de.properties").toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
	};

	private Bundle bundle = null;
	private ResourceBundleTool rbTool = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// a dummy bundle
		this.bundle = mock(Bundle.class);
		checking(new Expectations(){{
			allowing(bundle).findEntries(IResourceBundleTool.LOCALIZATION_LOCATION_DEFAULT, RESOURCEBUNDLE_BASE + "*.properties",false);
			will(returnValue(Collections.enumeration(Arrays.asList(RESOURCEBUNDLE_FILES))));
			
			allowing(bundle).findEntries(IResourceBundleTool.LOCALIZATION_LOCATION_DEFAULT, RESOURCEBUNDLE_BASE + ".properties", false);
			will(returnValue(Collections.enumeration(Arrays.asList(new URL[]{RESOURCEBUNDLE_FILES[0]}))));				
			
			allowing(bundle).findEntries(IResourceBundleTool.LOCALIZATION_LOCATION_DEFAULT, RESOURCEBUNDLE_BASE + "_de.properties", false);
			will(returnValue(Collections.enumeration(Arrays.asList(new URL[]{RESOURCEBUNDLE_FILES[1]}))));
			
			ignoring(bundle);
		}});
		
		// the rb-tool
		this.rbTool = new ResourceBundleTool(this.bundle);
	}
	
	public void testGetLocaleURL() {
		final List<URL> localeURL = this.rbTool.getLocaleURL(IResourceBundleTool.LOCALIZATION_LOCATION_DEFAULT + '/' + RESOURCEBUNDLE_BASE);
		assertNotNull(localeURL);
		ArrayAssert.assertEquals(RESOURCEBUNDLE_FILES, localeURL.toArray());
	}
	
	public void testGetLocaleURL2() {
		final List<URL> localeURL = this.rbTool.getLocaleURL(RESOURCEBUNDLE_BASE);
		assertNotNull(localeURL);
		ArrayAssert.assertEquals(RESOURCEBUNDLE_FILES, localeURL.toArray());
	}	
	
	public void testGetLocaleArray() {
		final String[] localeArray = this.rbTool.getLocaleArray(RESOURCEBUNDLE_BASE, Locale.ENGLISH);
		assertNotNull(localeArray);
		assertEquals("en", localeArray[0]);
		assertEquals("de", localeArray[1]);
	}
	
	public void testGetLocaleVariants() {
		Iterator<String> variants = this.rbTool.getLocaleVariants("en_GB");
		assertNotNull(variants);
		assertEquals("en_GB", variants.next());
		assertEquals("en", variants.next());
		assertEquals("", variants.next());
		
		variants = this.rbTool.getLocaleVariants("en");
		assertNotNull(variants);
		assertEquals("en", variants.next());
		assertEquals("", variants.next());
		
		variants = this.rbTool.getLocaleVariants("");
		assertNotNull(variants);
		assertEquals("", variants.next());
	}
	
	public void testGetLocalization() {
		final ResourceBundle rbundle = this.rbTool.getLocalization(IResourceBundleTool.LOCALIZATION_LOCATION_DEFAULT + '/' + RESOURCEBUNDLE_BASE, "de_de");
		assertNotNull(rbundle);
		assertEquals(new Locale("de"), rbundle.getLocale());
		assertFalse(rbundle.getString("filterManager.name") == null);
	}
	
	public void testGetLocalization2() {
		final ResourceBundle rbundle = this.rbTool.getLocalization(RESOURCEBUNDLE_BASE, "de_de");
		assertNotNull(rbundle);
		assertEquals(new Locale("de"), rbundle.getLocale());
		assertFalse(rbundle.getString("filterManager.name") == null);
	}	
}

class BundleURLStreamHandlerFactory implements URLStreamHandlerFactory {
	public URLStreamHandler createURLStreamHandler(String protocol) {
		if (protocol != null && protocol.equalsIgnoreCase("bundleentry")) {
			return new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL u) throws IOException {
					throw new RuntimeException("Not implemented.");
				}					
			};
		}
		return null;
	}
	
}