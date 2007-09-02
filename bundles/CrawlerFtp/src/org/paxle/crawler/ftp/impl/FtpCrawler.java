package org.paxle.crawler.ftp.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.CrawlerDocument;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.crawler.CrawlerTools;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ftp.IFtpCrawler;

public class FtpCrawler implements IFtpCrawler {
	public static final String PROTOCOL = "ftp";
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * @see ISubCrawler#getProtocol()
	 */	
	public String getProtocol() {
		return PROTOCOL;
	}

	public ICrawlerDocument request(String requestUrl) {
		if (requestUrl == null) throw new NullPointerException("URL was null");
		this.logger.info(String.format("Crawling URL '%s' ...", requestUrl));		
		
		CrawlerDocument crawlerDoc = new CrawlerDocument();
		crawlerDoc.setCrawlerDate(new Date());
		
		try {
			FtpUrlConnection ftpConnection = new FtpUrlConnection(new URL(requestUrl));

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
				e.printStackTrace();
			}
		} 		

		return crawlerDoc;
	}
	
	public static void main(String[] args) {
		FtpCrawler ftp = new FtpCrawler();
		
		ftp.request("ftp://anonymous:anonymous@ftp.tuwien.ac.at/api");
		ftp.request("ftp://ftp.tuwien.ac.at/api/sane/README");
	}
}
