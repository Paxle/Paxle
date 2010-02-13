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

package org.paxle.crawler.smb.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;

import jcifs.smb.SmbFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.ICrawlerDocument.Status;
import org.paxle.crawler.ICrawlerContext;
import org.paxle.crawler.ICrawlerContextLocal;
import org.paxle.crawler.ICrawlerTools;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ICrawlerTools.DirlistEntry;

@Component(metatype=false)
@Service(ISubCrawler.class)
@Property(name=ISubCrawler.PROP_PROTOCOL, value={"smb"})
public class SmbCrawler implements ISubCrawler {

	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());

	@Reference
	protected ICrawlerContextLocal contextLocal;
	
	public ICrawlerDocument request(URI requestUri) {
		if (requestUri == null) throw new NullPointerException("URL was null");
		this.logger.info(String.format("Crawling URL '%s' ...", requestUri));	
		
		ICrawlerDocument crawlerDoc = null;
		InputStream input = null;
		try {
			final ICrawlerContext ctx = this.contextLocal.getCurrentContext();
			
			// creating an empty crawler-document
			crawlerDoc = ctx.createDocument();			
			crawlerDoc.setCrawlerDate(new Date());
			crawlerDoc.setLocation(requestUri);			
			
			/* 
			 * Create a temp URI to ensure that the port is set properly
			 * This is required otherwise jcifs throws an exception.
			 */
			URI temp = new URI(
					requestUri.getScheme(),
					requestUri.getUserInfo(),
					requestUri.getHost(),
					(requestUri.getPort()==-1)?445:requestUri.getPort(),
					requestUri.getPath(),
					requestUri.getQuery(),
					requestUri.getFragment()
			);
			
			SmbFile smbFile = new SmbFile(temp.toURL());
			if (!smbFile.exists()) {
				crawlerDoc.setStatus(Status.NOT_FOUND, "The resource does not exist");				
				this.logger.info(String.format("The resource '%s' does not exit.",requestUri));			
				return crawlerDoc;
			} else if (!smbFile.canRead()) {
				crawlerDoc.setStatus(Status.NOT_FOUND, "The resource can not be read.");				
				this.logger.info(String.format("The resource '%s' can not be read.",requestUri));			
				return crawlerDoc;				
			}
			
			final ICrawlerTools crawlerTools = ctx.getCrawlerTools();
			if (smbFile.isDirectory()) {
				/* Append '/' if necessary. Otherwise we will get:
				 * jcifs.smb.SmbException: smb://srver/dir directory must end with '/'
				 */
				// XXX still needed with the SmbFile(URL)-constructor?
				String uriString = requestUri.toASCIIString();
				if (!uriString.endsWith("/")) {
					uriString += "/";
					smbFile = new SmbFile(uriString);
				}
				
				// set the mimetype accordingly
				crawlerDoc.setMimeType("text/html");
				
				// using the dir creation date as last-mod date
				long creationTimeStamp = smbFile.createTime();
				if (creationTimeStamp != 0) {
					crawlerDoc.setLastModDate(new Date(creationTimeStamp));
				}
				
				// getting the content of the directory
				SmbFile[] smbFiles = smbFile.listFiles();
				final Iterator<DirlistEntry> dirlistIt = new DirlistIterator(smbFiles, false);				
				
				// generate & save dir listing
				crawlerTools.saveListing(
						crawlerDoc,
						dirlistIt,
						true,
						smbFiles.length > 50 // if more than 50 files, use compression
				);
			} else if (smbFile.isFile()) {
				// last modified timestamp
				long modTimeStamp = smbFile.getLastModified();
				if (modTimeStamp != 0) {
					crawlerDoc.setLastModDate(new Date(modTimeStamp));
				}
				
				// get file content
				input = smbFile.getInputStream();
			} 
			
			if (input != null) {
				// copy data into file
				crawlerTools.saveInto(crawlerDoc, input);
					
				// finished
				crawlerDoc.setStatus(Status.OK);
			} else {
				crawlerDoc.setStatus(Status.UNKNOWN_FAILURE, "Unable to determine the smb-file type");
			}
		} catch(Throwable e) {
			crawlerDoc.setStatus(Status.UNKNOWN_FAILURE, "Unexpected Exception: " + e.getMessage());
			
			this.logger.warn(String.format(
					"Unexpected '%s' while trying to crawl resource '%s'.",
					e.getClass().getName(),
					requestUri
			),e);
		} finally {
			if (input != null) try { input.close(); } catch (Exception e) {/* ignore this */}
		}

		return crawlerDoc;			
	}
	
	/**
	 * A wrapper class around a {@link SmbFile} which implements the methods necessary for
	 * the dirlist-generation.
	 */
	private static class DirlistEntryImpl implements DirlistEntry {		
		SmbFile file;
		
		public URI getFileURI() { return null; }
		public String getFileName() { return file.getName(); }
		public long getLastModified() { return file.getLastModified(); }
		public long getSize() { return file.getContentLength(); }
	};
	
	private static class DirlistIterator implements Iterator<DirlistEntry> {
		
		private final DirlistEntryImpl entry = new DirlistEntryImpl();
		private final boolean omitHidden;
		private final SmbFile[] list;
		
		private SmbFile next = null;
		private int idx = -1;
		
		public DirlistIterator(SmbFile[] list, boolean omitHidden) {
			this.list = list; 
			this.omitHidden = omitHidden;
			this.next = findNext();
		}
		
		private SmbFile findNext() {
			while (this.idx + 1 < this.list.length) {
				try {
					this.idx++;
					// check whether we are allowed to crawl this file
					if (this.omitHidden && this.list[this.idx].isHidden()) continue;
					return this.list[this.idx];
				} catch (IOException e) {
					// XXX: what to do in this case. aborting the whole operation?
				}
			}
			return null;
		}
		
		public boolean hasNext() {
			return this.next != null;
		}
		
		public DirlistEntry next() {
			this.entry.file = this.next;
			this.next = findNext();
			return this.entry;
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
