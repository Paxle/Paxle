package org.paxle.crawler.ftp.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
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
		this.logger.info(String.format("Crawling URL '%s' ...", requestUrl));		
		
		CrawlerDocument crawlerDoc = new CrawlerDocument();
		FTPClient client = null;
		try {
			URL url = new URL(requestUrl);
			
			int reply;
			client = new FTPClient();
			
			/* =======================================================================
			 * Connect to host
			 * ======================================================================= */
			client.connect(url.getHost());
			reply = client.getReplyCode();
			if(!FTPReply.isPositiveCompletion(reply)) {
				client.disconnect();
				String msg = String.format("FTP server '%s' refused connection. ReplyCode: %s",url.getHost(),client.getReplyString());
				this.logger.warn(msg);
				crawlerDoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE,msg);
				return crawlerDoc;
			} else {
				this.logger.info(String.format("Connected to '%s'. ReplyCode: %s",url.getHost(),client.getReplyString()));
			}
			
			/* =======================================================================
			 * Login
			 * ======================================================================= */
			String userName = "anonymous", pwd = "anonymous";
			String userInfo = url.getUserInfo();
			if (userInfo != null) {
				int idx = userInfo.indexOf(":");
				if (idx != -1) {
					userName = userInfo.substring(0,idx).trim();
					pwd = userInfo.substring(idx+1).trim();
				}
			}
			client.login(userName, pwd);
			reply = client.getReplyCode();
			if(!FTPReply.isPositiveCompletion(reply)) {
				client.disconnect();
				String msg = String.format("FTP server '%s' login failed. ReplyCode: %s",url.getHost(),client.getReplyString());
				this.logger.warn(msg);
				crawlerDoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE,msg);
				return crawlerDoc;
			} 

			/* =======================================================================
			 * Change directory
			 * ======================================================================= */			
			String longpath = url.getPath();

            String file = "", path;              
            if (longpath.endsWith("/")) {
                file = "";
                path = longpath;
            } else {
                int pos = longpath.lastIndexOf("/");
                if (pos == -1) {
                    file = longpath;
                    path = "/";
                } else {            
                    path = longpath.substring(0,pos+1);
                    file = longpath.substring(pos+1);
                }
            }   
			
			client.changeWorkingDirectory(path);
			reply = client.getReplyCode();
			if(!FTPReply.isPositiveCompletion(reply)) {
				client.disconnect();
				String msg = String.format("FTP server '%s' change directory failed. ReplyCode: %s",url.getHost(),client.getReplyString());
				this.logger.warn(msg);
				crawlerDoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE,msg);
				return crawlerDoc;
			} 			
			
			// test if the specified file is also an directory
			if (file.length() > 0) {
				client.changeWorkingDirectory(file);
				reply = client.getReplyCode();
				if(FTPReply.isPositiveCompletion(reply)) {
                    longpath += "/";
                    file = "";
				}
			}
			
			File dataFile = null;
			if (file.length() == 0) {
//				// TODO: directory listing
				FTPFile[] files = client.listFiles(path);
				for (FTPFile nextFile : files) {
					System.out.println(nextFile);
				}

				// TODO: write the URLs into a file
				// which format should we use?
			} else {
				// switching to binary file transfer
				client.setFileType(FTPClient.BINARY_FILE_TYPE); 
				reply = client.getReplyCode();
				if(!FTPReply.isPositiveCompletion(reply)) {
					client.disconnect();
					String msg = String.format("FTP server '%s' changing transfer mode failed. ReplyCode: %s",url.getHost(),client.getReplyString());
					this.logger.warn(msg);
					crawlerDoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE,msg);
					return crawlerDoc;
				} 				
				
				// copy file
				InputStream fileInputStream = client.retrieveFileStream(file);
				reply = client.getReplyCode();
				if(!FTPReply.isPositiveCompletion(reply)) {
					client.disconnect();
					String msg = String.format("FTP server '%s' file transfer failed. ReplyCode: %s",url.getHost(),client.getReplyString());
					this.logger.warn(msg);
					crawlerDoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE,msg);
					return crawlerDoc;
				}
				
				// TODO: charset detection
				// TODO: mimetype detection
				// TODO: get file info: e.g. modification-date ...
				
				dataFile = createAndCopy(fileInputStream);
				fileInputStream.close();	
								
				client.completePendingCommand();
			}

			crawlerDoc.setContent(dataFile);
			
			// logout from ftp server
			client.logout();
			
			crawlerDoc.setStatus(ICrawlerDocument.Status.OK);
		} catch(IOException e) {
			crawlerDoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, "Unexpected Exception: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if((client != null) && (client.isConnected())) { try { client.disconnect(); } catch(IOException ioe) {/* ignore this */}}
		}		

		return crawlerDoc;
	}
	
	private File createAndCopy(InputStream respBody) throws IOException {
		File temp = this.createTempFile();
		FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(temp);
            byte[] buffer = new byte[4096];
            
            int c; 
            while ((c = respBody.read(buffer,0,buffer.length)) > 0) {
            	fos.write(buffer, 0, c);
            	fos.flush();
            }
            fos.flush();            
        } finally {
            if (fos != null) try {fos.close();} catch (Exception e) {}
        }	
        return temp;
	}	
	
	private File createTempFile() throws IOException {
		return File.createTempFile("ftpCrawler", "tmp");
	}
	
	public static void main(String[] args) {
		FtpCrawler ftp = new FtpCrawler();
		ftp.request("ftp://anonymous:anonymous@ftp.tuwien.ac.at/api");
		ftp.request("ftp://ftp.tuwien.ac.at/api/sane/README");
	}
}
