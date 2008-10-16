package org.paxle.crawler.http.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Dictionary;
import java.util.Set;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.mortbay.jetty.testing.ServletTester;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.io.temp.ITempDir;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.crawler.CrawlerContext;
import org.paxle.crawler.impl.CrawlerContextLocal;

public class HttpCrawlerTest extends MockObjectTestCase {

	private ServletTester tester;
	private HttpCrawler crawler = null;
	private String servletURL = null;
	private Set<String> supportedMimeTypes = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();		
				
		// create the crawler
		this.crawler = new HttpCrawler(null);		
		
		// create a dummy crawler context
		this.initCrawlerContext("text/html");		
		
		// create a dummy server
		this.tester = new ServletTester();
		this.tester.setContextPath("/");
		this.tester.addServlet(DummyServlet.class, "/");		
		this.tester.start();				
		this.servletURL = tester.createSocketConnector(true);
	}

	public void initCrawlerContext(String mimeType) {
		CrawlerContextLocal threadLocal = new CrawlerContextLocal();
		threadLocal.getSupportedMimeTypes().add(mimeType);
		threadLocal.setTempFileManager(new ITempFileManager() {
			public File createTempFile() throws IOException {
				File tmp = File.createTempFile("test", ".tmp");
				tmp.deleteOnExit();
				return tmp;
			}
			public void releaseTempFile(File arg0) throws FileNotFoundException, IOException {
				arg0.delete();				
			}
			public void removeTempDirFor(String... arg0) { }
			public void setTempDirFor(ITempDir arg0, String... arg1) { }			
		});
		CrawlerContext.setThreadLocal(threadLocal);
	}
	
	@Override
	protected void tearDown() throws Exception {	
		super.tearDown();		
		this.tester.stop();
	}
	
	public void testDownloadHtmlResourceOK() throws Exception {
		this.tester.setAttribute(DummyServlet.ATTR_FILE_NAME, "test1.html");
		this.tester.setAttribute(DummyServlet.ATTR_FILE_MIMETYPE, "text/html");
		this.tester.setAttribute(DummyServlet.ATTR_FILE_CHARSET, "iso-8859-1");
		
		// do some crawling
		ICrawlerDocument doc = this.crawler.request(URI.create(this.servletURL));
		assertNotNull(doc);
		assertEquals(ICrawlerDocument.Status.OK, doc.getStatus());
		assertEquals("text/html", doc.getMimeType());
		assertEquals("iso-8859-1", doc.getCharset());
	}
	
	public void testDownloadNotFoundResource() {
		this.tester.setAttribute(DummyServlet.ATTR_STATUS_CODE, new Integer(404));		
		
		// do some crawling
		ICrawlerDocument doc = this.crawler.request(URI.create(this.servletURL));
		assertNotNull(doc);
		assertEquals(ICrawlerDocument.Status.NOT_FOUND, doc.getStatus());
	}
	
	public void testDownloadNotAllowedResource() {
		this.tester.setAttribute(DummyServlet.ATTR_STATUS_CODE, new Integer(403));		
		
		// do some crawling
		ICrawlerDocument doc = this.crawler.request(URI.create(this.servletURL));
		assertNotNull(doc);
		assertEquals(ICrawlerDocument.Status.UNKNOWN_FAILURE, doc.getStatus());
	}	
	
	public void testDownloadUnkownMimeType() {
		this.tester.setAttribute(DummyServlet.ATTR_FILE_MIMETYPE, "xyz/unknown");
		
		// do some crawling
		ICrawlerDocument doc = this.crawler.request(URI.create(this.servletURL));
		assertNotNull(doc);
		assertEquals(ICrawlerDocument.Status.UNKNOWN_FAILURE, doc.getStatus());
	}
	
	public void testMaxDownloadSizeExceeded() {
		this.tester.setAttribute(DummyServlet.ATTR_FILE_SIZE, new Integer(1200));
		
		// change crawler settings
		Dictionary<String, Object> props = this.crawler.getDefaults();
		props.put(HttpCrawler.PROP_MAXDOWNLOAD_SIZE, new Integer(1000));
		this.crawler.updated(props);
		
		// do some crawling
		ICrawlerDocument doc = this.crawler.request(URI.create(this.servletURL));
		assertNotNull(doc);
		assertEquals(ICrawlerDocument.Status.UNKNOWN_FAILURE, doc.getStatus());		
	}
}
