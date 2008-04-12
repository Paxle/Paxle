package org.paxle.crawler.proxy.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.CrawlerDocument;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.crawler.CrawlerTools;
import org.xsocket.connection.http.HttpResponseHeader;

public class ProxyDataProviderCallable implements Callable<ICrawlerDocument> {
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());	
	
	private final URI location;
	private final HttpResponseHeader responseHeaders;
	private final InputStream bodyInputStream;	
	
	public ProxyDataProviderCallable(URI location, HttpResponseHeader responseHeaders, InputStream bodyInputStream) {
		if (location == null) throw new NullPointerException("The command object was null.");
		if (responseHeaders == null) throw new NullPointerException("The response-headers were null.");
		if (bodyInputStream == null) throw new NullPointerException("The response-body wasnull.");
		
		this.location = location;
		this.responseHeaders = responseHeaders;
		this.bodyInputStream = bodyInputStream;
	}
	
	public ICrawlerDocument call() throws Exception {
		CrawlerDocument doc = null;
		try {					
			// create a new CrawlerDocument
			doc = new CrawlerDocument();
			
			// getting the content location
			doc.setLocation(this.location);
			
			// extract headers
			this.extractHeaderData(this.responseHeaders, doc);						
			
			// copy the content to file
			CrawlerTools.saveInto(doc, bodyInputStream, null);			

			doc.setStatus(ICrawlerDocument.Status.OK);
			this.logger.debug(String.format("Processing of URL '%s' finished.", this.location));
		} catch (Throwable e) {
			String msg = e.getMessage();
			
			// TODO: better error handling required
			
			doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, msg);		
		} finally {
			try { bodyInputStream.close(); } catch (IOException e) {/* ignore this */}
		}
		return doc;
	}

	
	private void extractHeaderData(final HttpResponseHeader resHdr, final CrawlerDocument doc) throws IOException {
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
