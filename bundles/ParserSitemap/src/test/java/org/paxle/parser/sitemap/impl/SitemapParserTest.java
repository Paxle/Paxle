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
