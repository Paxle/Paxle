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
package org.paxle.crawler.http.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Dictionary;

import org.apache.commons.httpclient.Header;
import org.mortbay.jetty.testing.ServletTester;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.crawler.impl.ACrawlerTest;

public class HttpCrawlerTest extends ACrawlerTest {
	private static final String TESTFILE_MIMETYPE = "text/html";
	private static final String TESTFILE_NAME = "test1.html";
	
	private ServletTester tester;
	private HttpCrawler crawler = null;
	private String servletURL = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();		
		
		// create the crawler
		this.crawler = new HttpCrawler(null);
		this.crawler.setCrawlerContextLocal(this.crawlerContextLocal);

		// create a dummy server
		this.tester = new ServletTester();
		this.tester.setContextPath("/");
		this.tester.addServlet(DummyServlet.class, "/");		
		this.tester.start();				
		this.servletURL = tester.createSocketConnector(true);
	}
	
	@Override
	protected void tearDown() throws Exception {	
		super.tearDown();		
		this.tester.stop();
	}
	
	public void testDownloadHtmlResourceOK() throws Exception {
		this.tester.setAttribute(DummyServlet.ATTR_FILE_NAME, TESTFILE_NAME);
		this.tester.setAttribute(DummyServlet.ATTR_FILE_MIMETYPE, TESTFILE_MIMETYPE);
		this.tester.setAttribute(DummyServlet.ATTR_FILE_CHARSET, "utf-8");
		
		// do some crawling
		this.crawlerDoc = this.crawler.request(URI.create(this.servletURL));
		assertNotNull(this.crawlerDoc);
		assertEquals(ICrawlerDocument.Status.OK, this.crawlerDoc.getStatus());
		assertEquals(TESTFILE_MIMETYPE, this.crawlerDoc.getMimeType());
		assertEquals("utf-8", this.crawlerDoc.getCharset());
	}
	
	public void testDownloadNotFoundResource() {
		this.tester.setAttribute(DummyServlet.ATTR_STATUS_CODE, Integer.valueOf(404));		
		
		// do some crawling
		this.crawlerDoc = this.crawler.request(URI.create(this.servletURL));
		assertNotNull(this.crawlerDoc);
		assertEquals(ICrawlerDocument.Status.NOT_FOUND, this.crawlerDoc.getStatus());
	}
	
	public void testDownloadNotAllowedResource() {
		this.tester.setAttribute(DummyServlet.ATTR_STATUS_CODE, Integer.valueOf(403));		
		
		// do some crawling
		this.crawlerDoc = this.crawler.request(URI.create(this.servletURL));
		assertNotNull(this.crawlerDoc);
		assertEquals(ICrawlerDocument.Status.UNKNOWN_FAILURE, this.crawlerDoc.getStatus());
	}	
	
	public void testDownloadUnkownMimeTypeDisallowed() {
		this.tester.setAttribute(DummyServlet.ATTR_FILE_MIMETYPE, "xyz/unknown");
		
		// do some crawling
		this.crawlerDoc = this.crawler.request(URI.create(this.servletURL));
		assertNotNull(this.crawlerDoc);
		assertEquals(ICrawlerDocument.Status.UNKNOWN_FAILURE, this.crawlerDoc.getStatus());
	}
	
	public void testDownloadUnkownMimeTypeAllowed() {
		this.tester.setAttribute(DummyServlet.ATTR_FILE_NAME, TESTFILE_NAME);
		this.tester.setAttribute(DummyServlet.ATTR_FILE_MIMETYPE, "xyz/unknown");
		
		// change crawler settings
		Dictionary<String, Object> props = this.crawler.getDefaults();
		props.put(HttpCrawler.PROP_SKIP_UNSUPPORTED_MIMETYPES, Boolean.FALSE);
		this.crawler.updated(props);		
		
		// do some crawling
		this.crawlerDoc = this.crawler.request(URI.create(this.servletURL));
		assertNotNull(this.crawlerDoc);
		assertEquals(ICrawlerDocument.Status.OK, this.crawlerDoc.getStatus());
	}	
	
	public void testMaxDownloadSizeExceeded() {
		this.tester.setAttribute(DummyServlet.ATTR_FILE_SIZE, Integer.valueOf(1200));
		this.tester.setAttribute(DummyServlet.ATTR_FILE_MIMETYPE, TESTFILE_MIMETYPE);
		
		// change crawler settings
		Dictionary<String, Object> props = this.crawler.getDefaults();
		props.put(HttpCrawler.PROP_MAXDOWNLOAD_SIZE, Integer.valueOf(1000));
		this.crawler.updated(props);
		
		// do some crawling
		ICrawlerDocument doc = this.crawler.request(URI.create(this.servletURL));
		assertNotNull(doc);
		assertEquals(ICrawlerDocument.Status.UNKNOWN_FAILURE, doc.getStatus());		
	}
	
	public void testMaxDownloadSizeExceededTransferEncoding() {	
		this.tester.setAttribute(DummyServlet.ATTR_FILE_SIZE, Integer.valueOf(-1200));
		this.tester.setAttribute(DummyServlet.ATTR_FILE_MIMETYPE, TESTFILE_MIMETYPE);
		
		// change crawler settings
		Dictionary<String, Object> props = this.crawler.getDefaults();
		props.put(HttpCrawler.PROP_MAXDOWNLOAD_SIZE, Integer.valueOf(1000));
		this.crawler.updated(props);
		
		// do some crawling
		this.crawlerDoc = this.crawler.request(URI.create(this.servletURL));
		assertNotNull(this.crawlerDoc);
		assertEquals(ICrawlerDocument.Status.UNKNOWN_FAILURE, this.crawlerDoc.getStatus());		
	}
	
	public void testHandleContentTypeHeader() throws IOException {		
		final ICrawlerDocument cdoc = this.docFactory.createDocument(ICrawlerDocument.class);
		Header h = null;
		
		cdoc.setCharset(null);
		h = new Header("Content-Type","text/html; Charset=UTF-8");
		this.crawler.handleContentTypeHeader(h, cdoc);
		assertEquals("UTF-8", cdoc.getCharset());
		
		cdoc.setCharset(null);
		h = new Header("Content-Type","text/html; Charset=UTF-8");
		this.crawler.handleContentTypeHeader(h, cdoc);
		assertEquals("UTF-8", cdoc.getCharset());
		
		cdoc.setCharset(null);
		h = new Header("Content-Type","text/html; Charset=\"UTF-8\"");
		this.crawler.handleContentTypeHeader(h, cdoc);
		assertEquals("UTF-8", cdoc.getCharset());	
		
		cdoc.setCharset(null);
		h = new Header("Content-Type","text/html; Charset='UTF-8'");
		this.crawler.handleContentTypeHeader(h, cdoc);
		assertEquals("UTF-8", cdoc.getCharset());	
		
		cdoc.setCharset(null);
		h = new Header("Content-Type","text/rss; charset=UTF-8; type=feed");
		this.crawler.handleContentTypeHeader(h, cdoc);
		assertEquals("UTF-8", cdoc.getCharset());
		
		cdoc.setCharset(null);
		h = new Header("Content-Type","text/rss; charset=\"UTF-8\"; type=feed");
		this.crawler.handleContentTypeHeader(h, cdoc);
		assertEquals("UTF-8", cdoc.getCharset());		
	}
}
