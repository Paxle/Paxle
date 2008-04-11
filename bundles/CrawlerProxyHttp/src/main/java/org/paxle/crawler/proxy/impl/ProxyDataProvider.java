package org.paxle.crawler.proxy.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.CrawlerDocument;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.crawler.CrawlerTools;
import org.xsocket.connection.http.HttpResponseHeader;

public class ProxyDataProvider extends Thread {
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	private final URI location;
	private final HttpResponseHeader resHdr;
	private final InputStream bodyInputStream;
	
	public ProxyDataProvider(URI location, HttpResponseHeader resHdr, InputStream bodyInputStream) {
		this.location = location;
		this.resHdr = resHdr;
		this.bodyInputStream = bodyInputStream;
	}
	
	@Override
	public void run() {
		final CrawlerDocument doc = new CrawlerDocument();
		try {									
			// getting the content location
			doc.setLocation(this.location);
			
			// extract headers
			this.postProcessHeaders(resHdr, doc);						
			
			// copy the content to file
			CrawlerTools.saveInto(doc, bodyInputStream, null);			

			doc.setStatus(ICrawlerDocument.Status.OK);
			logger.debug(String.format("Crawling of URL '%s' finished.", this.location));
		} catch (Throwable e) {
			String msg = e.getMessage();

			// FIXME re-enqueue command
			doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, msg);
		} finally {
			try { bodyInputStream.close(); } catch (IOException e) {/* ignore this */}
		}
	}
	
	private void postProcessHeaders(final HttpResponseHeader resHdr, final CrawlerDocument doc) throws IOException {
		// content-type and charset
		String contentType = resHdr.getContentType();
		int idx = contentType.indexOf(";");
		if (idx != -1) contentType = contentType.substring(idx+1).trim();		
		
		doc.setMimeType(contentType);
		doc.setCharset(resHdr.getCharacterEncoding());
		
		// getting the content language
		String contentLanguage = resHdr.getHeader(ProxyResponseHandler.HTTPHEADER_CONTENT_LANGUAGE);
		if (contentLanguage != null) {
			String[] languages = contentLanguage.split(",");
			doc.setLanguages(languages);
		}
		
		// crawling Date
		Date crawlingDate = null;
		String crawlingDateHeader = resHdr.getHeader(ProxyResponseHandler.HTTPHEADER_DATE);			
		if (crawlingDateHeader == null) {
			crawlingDate = new Date();
		} else {
			// TODO: parsing date
		}
		doc.setCrawlerDate(crawlingDate);
		
		// last mod date
		Date lastModDate = null;
		String lastModDateHeader = resHdr.getHeader(ProxyResponseHandler.HTTPHEADER_LAST_MODIFIED);
		if (lastModDateHeader != null) {
			// TODO: parsing date
		}
		doc.setLastModDate(lastModDate);			
		
		// ETAG
		String etageHeader = resHdr.getHeader(ProxyResponseHandler.HTTPHEADER_ETAG);
		if (etageHeader != null) {
			doc.setEtag(etageHeader);
		}
	}
}
