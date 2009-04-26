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
package org.paxle.crawler.ftp.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPFile;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.crawler.CrawlerContext;
import org.paxle.crawler.CrawlerTools;
import org.paxle.crawler.ICrawlerContext;
import org.paxle.crawler.CrawlerTools.DirlistEntry;
import org.paxle.crawler.ftp.IFtpCrawler;

public class FtpCrawler implements IFtpCrawler, ManagedService {
	/* =========================================================
	 * Config Properties
	 * ========================================================= */
	static final String PID = IFtpCrawler.class.getName();
	
	static final String PROP_CONNECTION_TIMEOUT 		= PID + '.' + "connectionTimeout";
	static final String PROP_SOCKET_TIMEOUT 			= PID + '.' + "socketTimeout";
	static final String PROP_MAXDOWNLOAD_SIZE 			= PID + '.' + "maxDownloadSize";	
	
	private int connectionTimeout = 15000;
	private int socketTimeout = 15000;
	private int maxDownloadSize = 10485760;
	
	/**
	 * The protocol(s) supported by this crawler
	 */
	static final String[] PROTOCOLS = new String[]{"ftp"};
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());

	public ICrawlerDocument request(URI requestUri) {
		if (requestUri == null) throw new NullPointerException("URL was null");
		this.logger.info(String.format("Crawling URL '%s' ...", requestUri));		
		
		ICrawlerDocument crawlerDoc = null;
		try {
			final ICrawlerContext ctx = CrawlerContext.getCurrentContext();
			if (ctx == null) throw new IllegalStateException("Cannot access CrawlerContext from " + Thread.currentThread().getName());
			
			// creating a crawler-doc and set some basic properties
			crawlerDoc = ctx.createDocument();
			crawlerDoc.setCrawlerDate(new Date());
			crawlerDoc.setLocation(requestUri);
			
			FtpUrlConnection ftpConnection = new FtpUrlConnection(requestUri.toURL());
			if (this.connectionTimeout >= 0) ftpConnection.setConnectTimeout(this.connectionTimeout);
			if (this.socketTimeout >= 0) ftpConnection.setReadTimeout(this.socketTimeout);

			// connect to host
			ftpConnection.connect();

			// get the modification date of the file
			long modTimeStamp = ftpConnection.getLastModified();
			if (modTimeStamp != 0) {
				crawlerDoc.setLastModDate(new Date(modTimeStamp));
			}
			
			// getting content-type if available
			String contentType = ftpConnection.getContentType();
			if (contentType != null) {
				crawlerDoc.setMimeType(contentType);
			}			
			
			// checking download size limit
			if (this.maxDownloadSize > 0) {
				int contentLength = ftpConnection.getContentLength();
				if (contentLength > this.maxDownloadSize) {
					// reject the document
					final String msg = String.format(
							"Content-length '%d' of resource '%s' is larger than the max. allowed size of '%d' bytes.",
							Integer.valueOf(contentLength),
							requestUri,
							Integer.valueOf(this.maxDownloadSize)
					);
					
					this.logger.warn(msg);
					crawlerDoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, msg);
					return crawlerDoc;
				}
			}
			
			if (ftpConnection.isDirectory()) {
				final FTPFile[] list = ftpConnection.listFiles();
				final Iterator<DirlistEntry> dirlistIt = new DirlistIterator(list);
				
				// generate & save dir-listing into file
				CrawlerTools.saveListing(crawlerDoc, dirlistIt, true, list.length > 50);
			} else {
				// get input stream
				InputStream input = ftpConnection.getInputStream();
				
				// copy data into file
				CrawlerTools.saveInto(crawlerDoc, input);
				
				// close connection
				input.close();
			}
				
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
	
	/**
	 * A wrapper class around a {@link FTPFile} which implements the methods necessary for
	 * the dirlist-generation.
	 */
	private static class DirlistEntryImpl implements DirlistEntry {		
		FTPFile file;
		
		public URI getFileURI() { return null; }
		public String getFileName() { return file.getName(); }
		public long getLastModified() { return file.getTimestamp().getTimeInMillis(); }
		public long getSize() { return file.getSize(); }
	};
	
	private static class DirlistIterator implements Iterator<DirlistEntry> {
		
		private final DirlistEntryImpl entry = new DirlistEntryImpl();
		private final FTPFile[] list;
		private int idx = 0;
		
		public DirlistIterator(final FTPFile[] list) {
			this.list = list;
		}
		
		public boolean hasNext() {
			return idx < list.length;
		}
		
		public DirlistEntry next() {
			entry.file = list[idx++];
			return entry;
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public Hashtable<String,Object> getDefaults() {
		Hashtable<String,Object> defaults = new Hashtable<String,Object>();
		
		defaults.put(PROP_CONNECTION_TIMEOUT, Integer.valueOf(15000));
		defaults.put(PROP_SOCKET_TIMEOUT, Integer.valueOf(15000));
		defaults.put(PROP_MAXDOWNLOAD_SIZE, Integer.valueOf(10485760));		
		defaults.put(Constants.SERVICE_PID, IFtpCrawler.class.getName());
		
		return defaults;
	}	
	
	/**
	 * @see ManagedService#updated(Dictionary)
	 */
	@SuppressWarnings("unchecked")
	public void updated(Dictionary configuration) throws ConfigurationException {
		if (configuration == null ) {
			logger.debug("Configuration is null. Using default configuration ...");
			
			/*
			 * Generate default configuration
			 */
			configuration = this.getDefaults();
		}
		
		// configuring timeouts
		final Integer connectionTimeout = (Integer) configuration.get(PROP_CONNECTION_TIMEOUT);
		if (connectionTimeout != null) this.connectionTimeout = connectionTimeout.intValue();
		
		final Integer socketTimeout = (Integer) configuration.get(PROP_SOCKET_TIMEOUT);
		if (socketTimeout != null) this.socketTimeout = socketTimeout.intValue();
		
		// download limit in bytes
		final Integer maxDownloadSize = (Integer)configuration.get(PROP_MAXDOWNLOAD_SIZE);
		if (maxDownloadSize != null) this.maxDownloadSize = maxDownloadSize.intValue();		
	}
}
