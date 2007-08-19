package org.paxle.crawler.http.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.charset.ICharsetDetectorStream;
import org.paxle.core.doc.CrawlerDocument;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.crawler.CrawlerContext;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.http.IHttpCrawler;

/**
 * TODO: configure the connection pool properly
 * TODO: set redirection follow, etc....
 * 
 */
public class HttpCrawler implements IHttpCrawler {
	private static final String HTTPHEADER_ETAG = "ETag";
	private static final String HTTPHEADER_LAST_MODIFIED = "Last-Modified";
	private static final String HTTPHEADER_DATE = "Date";
	private static final String HTTPHEADER_CONTENT_LANGUAGE = "Content-Language";
	private static final String HTTPHEADER_CONTENT_TYPE = "Content-Type";

	/**
	 * The protocol supported by this crawler
	 */
	public static final String PROTOCOL = "http";
	
	/**
	 * Connection manager used for http connection pooling
	 */
	private static MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	
	/**
	 * http client class
	 */
	private static HttpClient httpClient = new HttpClient(connectionManager); 
	
	/**
	 * Logger class
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * @see ISubCrawler#getProtocol()
	 */
	public String getProtocol() {
		return HttpCrawler.PROTOCOL;
	}	
	
	public ICrawlerDocument request(String requestUrl) {
		if (requestUrl == null) throw new NullPointerException("URL was null");
		this.logger.info(String.format("Crawling URL '%s' ...",requestUrl));
		
		CrawlerDocument doc = new CrawlerDocument();
		
		HttpMethod method = null;
		try {
			// generate the request method
			method = new GetMethod(requestUrl);

			// TODO: set some additional http headers
			//method.setRequestHeader("User-Agent","xxx");
			//method.setRequestHeader("Accept-Encoding","gzip");

			// send the request to the server
			int statusCode = httpClient.executeMethod(method);
			
			// check the response status code
			if (statusCode != HttpStatus.SC_OK) {
				if (statusCode == HttpStatus.SC_NOT_FOUND) {
					doc.setStatus(ICrawlerDocument.Status.NOT_FOUND);
				} else {
					doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE,String.format("Server returned: %s", method.getStatusLine()));
				}
				
				this.logger.warn(String.format("Crawling of URL '%' failed. Server returned: %s", requestUrl, method.getStatusLine()));
				return doc;
			}
			
			// getting the mimetype and charset
			String contentMimeType = null;
			Header contentTypeHeader = method.getResponseHeader(HTTPHEADER_CONTENT_TYPE);
			if (contentTypeHeader != null) {
				String contentCharset = null;
				contentMimeType = contentTypeHeader.getValue();
				
				int idx = contentMimeType.indexOf(";");
				if (idx != -1) {
					contentCharset = contentMimeType.substring(idx+1).trim();
					contentMimeType = contentMimeType.substring(0,idx);
					
					if (contentCharset.startsWith("charset=")) {
						contentCharset = contentCharset.substring("charset=".length()).trim();
						if (contentCharset.matches("^['\"].*")) {
							contentCharset = contentCharset.substring(1);
						}
						if (contentCharset.matches(".*['\"]$")) {
							contentCharset = contentCharset.substring(0,contentCharset.length()-1);							
						}
					} else {
						contentCharset = null;
					}
				}	
				
				doc.setMimeType(contentMimeType);
				doc.setCharset(contentCharset);
			}
			
			// getting the document languages
			Header contentLanguageHeader = method.getResponseHeader(HTTPHEADER_CONTENT_LANGUAGE);
			if (contentLanguageHeader != null) {
				String contentLanguage = contentLanguageHeader.getValue();
				String[] languages = contentLanguage.split(",");
				doc.setLanguages(languages);
			} else {
				doc.setLanguages(new String[]{"de","en"});
			}
			
			// crawling Date
			Date crawlingDate = null;			
			try {
				Header crawlingDateHeader = method.getResponseHeader(HTTPHEADER_DATE);
				if (crawlingDateHeader != null) {
					String dateStr = crawlingDateHeader.getValue();
					crawlingDate = DateUtil.parseDate(dateStr);
				}
			} catch (DateParseException e) {
				crawlingDate = new Date();
			}
			doc.setCrawlerDate(crawlingDate);

			// last mod date
			Date lastModDate = null;			
			try {
				Header lastModDateHeader = method.getResponseHeader(HTTPHEADER_LAST_MODIFIED);
				if (lastModDateHeader != null) {
					String dateStr = lastModDateHeader.getValue();
					lastModDate = DateUtil.parseDate(dateStr);
				}
			} catch (DateParseException e) {
				lastModDate = crawlingDate;
			}
			doc.setLastModDate(lastModDate);			
			
			// ETAG
			Header etageHeader = method.getResponseHeader(HTTPHEADER_ETAG);
			if (etageHeader != null) {
				String etag = etageHeader.getValue();
				doc.setEtag(etag);
			}			
			
			// getting the response body			
			InputStream respBody = method.getResponseBodyAsStream();
			
			// getting a charset-detector
			CrawlerContext context = CrawlerContext.getCurrentContext();
			ICharsetDetector charsetDetector = context.getCharsetDetector();
			if (charsetDetector != null) {
				/* 
				 * Wrap the body-inputstream into a charset detector stream
				 * if the mimetype of the resource is inspectable
				 */
				if (charsetDetector.isInspectable(contentMimeType)) {
					respBody = charsetDetector.createInputStream(respBody);
				}
			} else {
				this.logger.warn("No charset detector found. Skipping charset detection.");
			}
			
			// copy the content to file
			// TODO: add gzip/deflate support
			File content = createAndCopy(respBody);
			
			// determine if charset detection succeeded
			if (respBody instanceof ICharsetDetectorStream) {
				String newCharset = ((ICharsetDetectorStream)respBody).getCharset();
				if (newCharset != null) {
					this.logger.info(String.format("Charset '%s' detected for URL %s", newCharset, requestUrl));
					doc.setCharset(newCharset);
				}
			}
			
			doc.setContent(content);
			respBody.close();
			
			doc.setStatus(ICrawlerDocument.Status.OK);			
		} catch (Exception e) {
			String errorMsg;
			if (e instanceof HttpException) {
				errorMsg = "Unrecovered protocol exception: %s";
			} else if (e instanceof IOException) {
				errorMsg = "Transport exceptions: %s";
			} else {
				errorMsg = "Unexpected exception: %s";
			}
			errorMsg = String.format(errorMsg, e.getMessage());
			doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, errorMsg);
		} finally {
			if (method != null) method.releaseConnection();
		}
		this.logger.info(String.format("Crawling of URL '%s' finished.",requestUrl));
		
		return doc;
	}
	
	private File createAndCopy(InputStream respBody) throws IOException {
		File temp = File.createTempFile("httpCrawler", "tmp");
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
}
