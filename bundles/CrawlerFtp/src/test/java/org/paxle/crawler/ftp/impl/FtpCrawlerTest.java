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

package org.paxle.crawler.ftp.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.osgi.service.cm.ConfigurationException;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.crawler.impl.ACrawlerTest;

public class FtpCrawlerTest extends ACrawlerTest {

	/**
	 * The port number for our test FTP server. Non-superusers can't bind low ports.
	 */
	private static int localServerPort = 49997;
	
	/**
	 * The complete local address of the test FTP server, without trailing slash, e.g. "ftp://127.0.0.1:49997"
	 */
	private static String localServerAddress = "ftp://127.0.0.1:" + localServerPort;

	static final String[] TEST_URIS = {
		localServerAddress + "/debian/README",
		localServerAddress + "/pub/div-sources/tex/code8859%231.tex",		// testing '#' contained in path of URI
	};

	private FtpCrawler crawler;
	/**
	 * The fake local FTP server for this test
	 */
	private FakeFtpServer ftpsrv;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// create crawler
		this.crawler = new FtpCrawler(){{
			this.contextLocal = crawlerContextLocal;
		}};

		//create a fake FTP server
		this.ftpsrv = new FakeFtpServer();

		this.ftpsrv.addUserAccount(new UserAccount("anonymous", "anonymous", "/")); //FTP crawler default

		FileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new FileEntry("/debian/README", "FTP crawler testcase content - 38 byte"));
		fileSystem.add(new FileEntry("/pub/div-sources/tex/code8859#1.tex", "FTP crawler testcase content - 38 byte"));
		this.ftpsrv.setFileSystem(fileSystem);

		this.ftpsrv.setServerControlPort(localServerPort);

		this.ftpsrv.start();
	}

	public void testReadDirectory() {
		URI testUri = URI.create(localServerAddress);
		this.crawlerDoc = this.crawler.request(testUri);		
		assertNotNull(crawlerDoc);		
		assertEquals(testUri, crawlerDoc.getLocation());
		assertEquals(ICrawlerDocument.Status.OK, crawlerDoc.getStatus());
		assertEquals("text/html",crawlerDoc.getMimeType());
		assertNotNull(crawlerDoc.getContent());
		assertTrue(crawlerDoc.getContent().exists());
		assertTrue(crawlerDoc.getContent().length() > 0);
	}

	public void testReadDocument() {
		for (final String testUriString : TEST_URIS) {
			URI testUri = URI.create(testUriString);
			this.crawlerDoc = this.crawler.request(testUri);
			assertNotNull(crawlerDoc);		
			assertEquals(testUri, crawlerDoc.getLocation());
			assertEquals(ICrawlerDocument.Status.OK, crawlerDoc.getStatus());
			assertNotNull(crawlerDoc.getContent());
			assertTrue(crawlerDoc.getContent().exists());
			assertTrue(crawlerDoc.getContent().length() > 0);
		}
	}

	public void testReadDocumentMaxDownloadSizeLimit() throws ConfigurationException {
		URI testUri = URI.create(localServerAddress + "/debian/README");

		// change crawler settings
		final Map<String, Object> props = new HashMap<String, Object>();
		props.put(FtpCrawler.PROP_MAXDOWNLOAD_SIZE, Integer.valueOf(37)); //smaller than the 38 byte testcase content
		this.crawler.activate(props);

		// download document
		this.crawlerDoc = this.crawler.request(testUri);
		assertNotNull(crawlerDoc);		
		assertEquals(testUri, crawlerDoc.getLocation());
		assertEquals(ICrawlerDocument.Status.UNKNOWN_FAILURE, crawlerDoc.getStatus());
		assertNull(crawlerDoc.getContent());
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		//stop the test FTP server and unbind from port
		this.ftpsrv.stop();
	}
}
