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
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.crawler.CrawlerDocument;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.http.IHttpCrawler;

/**
 * TODO: configure the connection pool properly
 * TODO: set redirection follow, etc....
 * 
 */
public class HttpCrawler implements IHttpCrawler {
	public static final String PROTOCOL = "http";
	
	private static MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	private static HttpClient httpClient = new HttpClient(connectionManager); 
	
	
	/**
	 * @see ISubCrawler#getProtocol()
	 */
	public String getProtocol() {
		return HttpCrawler.PROTOCOL;
	}	
	
	public ICrawlerDocument request(String requestUrl) {
		if (requestUrl == null) throw new NullPointerException("URL was null");
		System.out.println("Crawling URL: " + requestUrl);
		
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
			
			// TODO: check the response status code
			if (statusCode != HttpStatus.SC_OK) {
				// TODO: what todo here?
				System.err.println("Method failed: " + method.getStatusLine());
				return null;
			}
			
			// getting the mimetype and charset
			Header contentTypeHeader = method.getResponseHeader("Content-Type");
			if (contentTypeHeader != null) {
				String contentCharset = null;
				String contentMimeType = contentTypeHeader.getValue();
				
				int idx = contentMimeType.indexOf(";");
				if (idx != -1) {
					contentCharset = contentMimeType.substring(idx+1);
					contentMimeType = contentMimeType.substring(0,idx);
				}	
				
				doc.setMimeType(contentMimeType);
				doc.setCharset(contentCharset);
			}
			
			// getting the document languages
			Header contentLanguageHeader = method.getResponseHeader("Content-Language");
			if (contentLanguageHeader != null) {
				String contentLanguage = contentLanguageHeader.getValue();
				String[] languages = contentLanguage.split(",");
				doc.setLanguages(languages);
			}
			
			// crawling Date
			Date crawlingDate = null;			
			try {
				Header crawlingDateHeader = method.getResponseHeader("Date");
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
				Header lastModDateHeader = method.getResponseHeader("Last-Modified");
				if (lastModDateHeader != null) {
					String dateStr = lastModDateHeader.getValue();
					lastModDate = DateUtil.parseDate(dateStr);
				}
			} catch (DateParseException e) {
				lastModDate = crawlingDate;
			}
			doc.setLastModDate(lastModDate);			
			
			// ETAG
			Header etageHeader = method.getResponseHeader("ETag");
			if (etageHeader != null) {
				String etag = etageHeader.getValue();
				doc.setLanguages(etag);
			}			
			
			// getting the response body			
			InputStream respBody = method.getResponseBodyAsStream();
			File content = createAndCopy(respBody);
			doc.setContent(content);
			respBody.close();
			
			// TODO: add gzip/deflate support
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (method != null) method.releaseConnection();
		}
		System.out.println("Crawling of URL '" + requestUrl + "' finished.");

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
