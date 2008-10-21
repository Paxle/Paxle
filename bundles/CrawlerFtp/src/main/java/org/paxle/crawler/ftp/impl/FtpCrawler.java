/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.CrawlerDocument;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.crawler.CrawlerTools;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ftp.IFtpCrawler;

public class FtpCrawler implements IFtpCrawler {
	public static final String[] PROTOCOLS = new String[]{"ftp"};
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * @see ISubCrawler#getProtocols()
	 */	
	public String[] getProtocols() {
		return PROTOCOLS;
	}

	public ICrawlerDocument request(URI requestUri) {
		if (requestUri == null) throw new NullPointerException("URL was null");
		this.logger.info(String.format("Crawling URL '%s' ...", requestUri));		
		
		CrawlerDocument crawlerDoc = new CrawlerDocument();
		crawlerDoc.setCrawlerDate(new Date());
		
		try {
			FtpUrlConnection ftpConnection = new FtpUrlConnection(requestUri.toURL());

			// connect to host
			ftpConnection.connect();

			// get the modification date of the file
			long modTimeStamp = ftpConnection.getLastModified();
			if (modTimeStamp != 0) {
				crawlerDoc.setLastModDate(new Date(modTimeStamp));
			}
			
			// get input stream
			InputStream input = ftpConnection.getInputStream();
							
			// copy data into file
			CrawlerTools.saveInto(crawlerDoc, input);
			
			// close connection
			input.close();
				
			// finished
			crawlerDoc.setStatus(ICrawlerDocument.Status.OK);
		} catch(IOException e) {
			if (e instanceof FtpConnectionException) {
				crawlerDoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, e.getMessage());
			} else {
				crawlerDoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, "Unexpected Exception: " + e.getMessage());
			}
			
			this.logger.warn(String.format("Unexpected '%s' while trying to crawl resource '%s'.",
					e.getClass().getName(),
					requestUri
			),e);
		} 		

		return crawlerDoc;
	}
}
