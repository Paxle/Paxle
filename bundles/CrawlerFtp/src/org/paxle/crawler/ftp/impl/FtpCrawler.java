package org.paxle.crawler.ftp.impl;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.paxle.core.doc.CrawlerDocument;
import org.paxle.core.doc.ICrawlerDocument;
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
		this.logger.info(String.format("Crawling URL: %s", requestUrl));		
		
		CrawlerDocument crawlerDoc = new CrawlerDocument();
		FTPClient ftp = null;
		try {
			URL url = new URL(requestUrl);
			
			int reply;
			ftp = new FTPClient();
			ftp.connect(url.getHost());
			// TODO: ftp.login(arg0, arg1)
			this.logger.debug(String.format("Connected to %s. ReplyCode=%s",url.getHost(),ftp.getReplyString()));

			// Check for login success
			reply = ftp.getReplyCode();

			if(!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				this.logger.debug(String.format("FTP server %s refused connection. ReplyCode=%s",url.getHost(),ftp.getReplyString()));
				crawlerDoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE);
				return crawlerDoc;
			}
			
			// TODO transfer files
			
			
			// logout from ftp server
			ftp.logout();
		} catch(IOException e) {
			crawlerDoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE);
			e.printStackTrace();
		} finally {
			if((ftp != null) && (ftp.isConnected())) { try { ftp.disconnect(); } catch(IOException ioe) {/* ignore this */}}
		}		

		return crawlerDoc;
	}

}
