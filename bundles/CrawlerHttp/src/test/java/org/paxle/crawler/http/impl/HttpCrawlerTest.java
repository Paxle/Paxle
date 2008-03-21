package org.paxle.crawler.http.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.io.temp.ITempDir;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.crawler.CrawlerContext;
import org.paxle.crawler.impl.CrawlerContextLocal;

public class HttpCrawlerTest extends MockObjectTestCase {

	private HttpCrawler crawler = null;
	
	protected void setUp() throws Exception {
		super.setUp();		
	}

	public void initCrawlerContext(String mimeType) {
		CrawlerContextLocal threadLocal = new CrawlerContextLocal();
		threadLocal.getSupportedMimeTypes().add(mimeType);
		threadLocal.setTempFileManager(new ITempFileManager() {
			public File createTempFile() throws IOException {
				return File.createTempFile("test", ".tmp");
			}
			public void releaseTempFile(File arg0) throws FileNotFoundException, IOException {
				arg0.delete();				
			}
			public void removeTempDirFor(String... arg0) { }
			public void setTempDirFor(ITempDir arg0, String... arg1) { }			
		});
		CrawlerContext.setThreadLocal(threadLocal);
	}
	
	public void testCrawlDummy() throws IOException {
		// get the testdata from file
		File testFile = new File("src/test/resources/test1.html");
		FileInputStream fileInput = new FileInputStream(testFile);

		// create a faked http-client
		HttpClientMockery mock = new HttpClientMockery();
		HttpClient client = mock.getHttpClient(fileInput);
		this.crawler = new HttpCrawler(client);
		
		// create a dummy crawler context
		this.initCrawlerContext("text/html");
		
		// do some crawling
		ICrawlerDocument doc = this.crawler.request("http://test.xyz");
		assertNotNull(doc);
		assertEquals(ICrawlerDocument.Status.OK, doc.getStatus());
		assertEquals("text/html", doc.getMimeType());
		assertEquals("iso-8859-1", doc.getCharset());
		
		mock.assertIsSatisfied();
	}
}
