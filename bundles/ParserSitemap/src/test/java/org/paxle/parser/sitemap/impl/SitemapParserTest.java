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
package org.paxle.parser.sitemap.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;
import junitx.framework.ListAssert;

import org.paxle.parser.sitemap.SitemapParser;
import org.paxle.parser.sitemap.api.Url;
import org.paxle.parser.sitemap.api.Urlset;

public class SitemapParserTest extends TestCase {
	private SitemapParser parser = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.parser = new SitemapParserImpl();
	}
	
	public void testParseSitemapXml() throws XMLStreamException, IOException {
		File testFile = new File("src/test/resources/sitemap/0.9/sitemap.xml");
		this.testParserSitemap(testFile);
	}	
	
	public void testParseSitemapXmlGzip() throws XMLStreamException, IOException {
		File testFile = new File("src/test/resources/sitemap/0.9/sitemap.xml.gz");
		this.testParserSitemap(testFile);
	}	
	
	private void testParserSitemap(File testFile) throws IOException, XMLStreamException {
		assertTrue(testFile.exists());

		ArrayList<URI> urls = new ArrayList<URI>();
		Urlset urlset = this.parser.getUrlSet(testFile);
		assertNotNull(urlset);
		
		for (Url url : urlset) {
			if (url == null) break;
			
			assertNotNull(url.getLocation());
			urls.add(url.getLocation());
		}		
		assertEquals(34, urls.size());
		ListAssert.assertContains(urls, URI.create("http://www.content-space.de/dokuwiki/blog/2008/my_favorite_desktop_linux_software_-_i_got_tagged"));		
	}
}
