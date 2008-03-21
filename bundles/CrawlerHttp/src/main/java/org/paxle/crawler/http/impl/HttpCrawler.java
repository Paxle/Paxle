
package org.paxle.crawler.http.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.httpclient.CircularRedirectException;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;
import org.paxle.core.doc.CrawlerDocument;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.crawler.CrawlerContext;
import org.paxle.crawler.CrawlerTools;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.http.IHttpCrawler;

/**
 * TODO: configure the connection pool properly
 * TODO: set redirection follow, etc....
 * 
 */
public class HttpCrawler implements IHttpCrawler, ManagedService {
	/* =========================================================
	 * Config Properties
	 * ========================================================= */
	private static final String PROP_CONNECTION_TIMEOUT = "connectionTimeout";
	private static final String PROP_SOCKET_TIMEOUT = "socketTimeout";
	private static final String PROP_MAXCONNECTIONS_PER_HOST = "maxConnectionsPerHost";
	private static final String PROP_MAXDOWNLOAD_SIZE = "maxDownloadSize";
	
	private static final String HTTPHEADER_ETAG = "ETag";
	private static final String HTTPHEADER_LAST_MODIFIED = "Last-Modified";
	private static final String HTTPHEADER_DATE = "Date";
	private static final String HTTPHEADER_CONTENT_LANGUAGE = "Content-Language";
	private static final String HTTPHEADER_CONTENT_TYPE = "Content-Type";
	private static final String HTTPHEADER_CONTENT_LENGTH = "Content-Length";

	/**
	 * The protocol supported by this crawler
	 */
	public static final String[] PROTOCOLS = new String[]{"http"};
	
	/**
	 * Connection manager used for http connection pooling
	 */
	private MultiThreadedHttpConnectionManager connectionManager = null;

	/**
	 * http client class
	 */
	private HttpClient httpClient = null;
	
	/**
	 * Logger class
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * The maximum size of a file. If set to <code>-1</code>, all files are fetched, otherwise
	 * (if the server provides the file-size) only files with a size equal to or less than this value.
	 */
	private int maxDownloadSize = -1;
	
	private final ReadLock robotsRLock;
	private final WriteLock robotsWLock;
	private Object robotsTxtManager;
	private Method robotsIsDisallowed;
	
	public HttpCrawler() {
		// init with default configuration
		this.updated(this.getDefaults());
		
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		robotsRLock = lock.readLock();
		robotsWLock = lock.writeLock();
	}
	
	public void cleanup() {
		// cleanup old settings
		if (this.connectionManager != null) {
			this.connectionManager.shutdown();
			this.connectionManager = null;
			this.httpClient = null;
		}
	}

	/**
	 * @return the default configuration of this service
	 */
	public Hashtable<String,Object> getDefaults() {
		Hashtable<String,Object> defaults = new Hashtable<String,Object>();
		
		defaults.put(PROP_CONNECTION_TIMEOUT, new Integer(15000));
		defaults.put(PROP_SOCKET_TIMEOUT, new Integer(15000));
		defaults.put(PROP_MAXCONNECTIONS_PER_HOST, new Integer(10));
		defaults.put(PROP_MAXDOWNLOAD_SIZE, Integer.valueOf(-1));
		
		defaults.put(Constants.SERVICE_PID, IHttpCrawler.class.getName());
		
		return defaults;
	}

	/**
	 * @see ManagedService#updated(Dictionary)
	 */
	public synchronized void updated(Dictionary configuration) {
		if ( configuration == null ) {
			/*
			 * Generate default configuration
			 */
			configuration = this.getDefaults();
		}
		
		/*
		 * Cleanup old config
		 */
		this.cleanup();
		
		/*
		 * Init with changed configuration
		 */
		this.connectionManager = new MultiThreadedHttpConnectionManager();
		
		// configure connections per host
		this.connectionManager.getParams().setDefaultMaxConnectionsPerHost(((Integer) configuration.get(PROP_MAXCONNECTIONS_PER_HOST)).intValue());
		
		// configuring timeouts
		this.connectionManager.getParams().setConnectionTimeout(((Integer) configuration.get(PROP_CONNECTION_TIMEOUT)).intValue());
		this.connectionManager.getParams().setSoTimeout(((Integer) configuration.get(PROP_SOCKET_TIMEOUT)).intValue());
		
		// set new http client
		this.httpClient = new HttpClient(connectionManager);
		
		this.maxDownloadSize = ((Integer)configuration.get(PROP_MAXDOWNLOAD_SIZE)).intValue();
	}
	
	/**
	 * @see ISubCrawler#getProtocols()
	 */
	public String[] getProtocols() {
		return HttpCrawler.PROTOCOLS;
	}	
	
	/**
	 * This method is synchronized with {@link #updated(Dictionary)} to avoid
	 * problems during configuration update.
	 * 
	 * @return the {@link HttpClient} to use
	 */
	private synchronized HttpClient getHttpClient() {
		return this.httpClient;
	}
	
	// package private, called by the RobotsFilterListener
	void setRobotsTxtFilter(final Object rmanager) {
		robotsWLock.lock();
		try {
			robotsTxtManager = rmanager;
			if (rmanager == null) {
				robotsIsDisallowed = null;
			} else try {
				robotsIsDisallowed = robotsTxtManager.getClass().getMethod("isDisallowed", String.class);
			} catch (NoSuchMethodException e) {
				robotsIsDisallowed = null;
				logger.error("Unable to access 'isDisallowed'-method of robots.txt-filter, has the interface changed?");
			}
		} finally { robotsWLock.unlock(); }
	}
	
	private boolean isLocationDisallowed(final String location) {
		Object result = null;
		robotsRLock.lock();
		try {
			if (robotsTxtManager == null || robotsIsDisallowed == null)
				return false;
			result = robotsIsDisallowed.invoke(robotsTxtManager, location);
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			logger.error("Error checking host of '%s' against robots.txt-filter", e);
		} finally { robotsRLock.unlock(); }
		
		if (result == null)
			return false;
		return ((Boolean)result).booleanValue();
	}
	
	/**
	 * Initializes the {@link HttpMethod} with common attributes for all requests this crawler
	 * initiates.
	 * <p>
	 * Currently the following attributes (represented as HTTP header values in the final request)
	 * are set:
	 * <ul>
	 *   <li>cookies shall be rejected ({@link CookiePolicy#IGNORE_COOKIES})</li>
	 * </ul>
	 * 
	 * @param method the method to set the standard attributes on
	 */
	private static void initRequestMethod(final HttpMethod method) {
		method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		
		// TODO: set some additional http headers
		//method.setRequestHeader("User-Agent","xxx");
		//method.setRequestHeader("Accept-Encoding","gzip");
	}
	
	/**
	 * This method handles the <code>Content-Type</code> {@link Header} of a HTTP-request.
	 * If available, the header is used to set the MIME-type of the {@link ICrawlerDocument}
	 * as well as the character set. The return value determines whether processing of the
	 * URL shall be continued or not
	 * <p>
	 * In case of a positive result, the <code>doc</code>'s MIME-type and - if available -
	 * charset will be set. Otherwise it's <code>result</code> will be set to
	 * {@link ICrawlerDocument.Status#UNKNOWN_FAILURE} and a warning message will be logged.
	 * 
	 * @see #HTTPHEADER_CONTENT_TYPE
	 * @param contentTypeHeader the HTTP-{@link Header}'s <code>Content-Type</code> attribute
	 * @param doc the {@link CrawlerDocument} the resulting MIME-type and charset shall be set in
	 * @return <code>true</code> if proceeding with the URL may continue or <code>false</code> if
	 *         it shall be aborted due to an unsupported MIME-type of the requested document
	 */
	private boolean handleContentTypeHeader(final Header contentTypeHeader, final CrawlerDocument doc) {
		// separate MIME-type and charset from the content-type specification
		String contentMimeType = null;
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
		
		// check if we support the mimetype
		final CrawlerContext context = CrawlerContext.getCurrentContext();
		if (context == null)
			throw new RuntimeException("Unexpected error. The crawler-context was null.");
		
		if (!context.getSupportedMimeTypes().contains(contentMimeType)) {
			// abort
			String msg = String.format(
					"Mimetype '%s' of resource '%s' not supported by any parser installed on the system.",
					contentMimeType,
					doc.getLocation()
			);
			
			this.logger.warn(msg);
			doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, msg);
			return false;
		}
		
		// continue
		return true;
	}
	
	/**
	 * This method handles the <code>Content-Length</code> {@link Header} of a HTTP-request.
	 * If given, and this {@link HttpCrawler} has a valid {@link #maxDownloadSize} set, both values
	 * are being compared and if the further exceeds the latter, <code>false</code> is retured,
	 * <code>true</code> otherwise
	 * <p>
	 * No values are set in the {@link CrawlerDocument} in case of a positive result, otherwise
	 * the document's <code>result</code> is set to {@link ICrawlerDocument.Status#UNKNOWN_FAILURE}
	 * and a warning message is logged.
	 * 
	 * @see #maxDownloadSize
	 * @param contentTypeLength the HTTP-{@link Header}'s <code>Content-Length</code> attribute
	 * @param doc the {@link CrawlerDocument} the resulting MIME-type and charset shall be set in
	 * @return <code>true</code> if proceeding with the URL may continue or <code>false</code> if
	 *         it shall be aborted due to an exceeded maximum file-size of the document
	 */
	private boolean handleContentTypeLength(final Header contentTypeLength, final CrawlerDocument doc) {
		if (contentTypeLength == null)
			// no Content-Length given, continue
			return true;
		
		final int maxDownloadSize = this.maxDownloadSize;
		if (maxDownloadSize < 0)
			// no maximum specified, continue
			return true;
		
		// extract the content length in bytes
		final String lengthString = contentTypeLength.getValue();
		if (lengthString.length() > 0 && lengthString.matches("\\d+")) {
			final int contentLength = Integer.parseInt(lengthString);
			if (contentLength > maxDownloadSize) {
				// reject the document
				final String msg = String.format(
						"Content-length %d of resource '%s' is larger than the max. allowed size of %d.",
						Integer.valueOf(contentLength),
						doc.getLocation(),
						Integer.valueOf(maxDownloadSize));
				
				this.logger.warn(msg);
				doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, msg);
				return false;
			}
		}
		
		// continue
		return true;
	}
	
	public ICrawlerDocument request(String requestUrl) {
		if (requestUrl == null) throw new NullPointerException("URL was null");
		this.logger.debug(String.format("Crawling URL '%s' ...",requestUrl));
		
		CrawlerDocument doc = new CrawlerDocument();
		
		// FIXME: we need to re-write the location on crawler-redirection
		doc.setLocation(requestUrl);
		
		HttpMethod method = null;
		try {
			// first use the HEAD method to determine whether the MIME-type is supported
			// and to compare the content-length with the maximum allowed download size
			// (both only if the server provides this information, if not, the file is
			// fetched)
			
			method = new HeadMethod(requestUrl);		// automatically follows redirects
			initRequestMethod(method);
			
			int statusCode = this.getHttpClient().executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				// RFC 2616 states that the GET and HEAD methods _must_ be supported by any
				// general purpose servers (which are in fact the ones we are connecting to here)
				
				if (statusCode == HttpStatus.SC_NOT_FOUND) {
					doc.setStatus(ICrawlerDocument.Status.NOT_FOUND);
				} else {
					doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, String.format("Server returned: %s", method.getStatusLine()));
				}
				
				this.logger.warn(String.format("Crawling of URL '%s' failed. Server returned: %s", requestUrl, method.getStatusLine()));
				return doc;
			}
			
			// XXX redirects
			
			Header contentTypeHeader = method.getResponseHeader(HTTPHEADER_CONTENT_TYPE);
			if (contentTypeHeader != null) {
				final boolean mimeTypeOk = handleContentTypeHeader(contentTypeHeader, doc);
				if (!mimeTypeOk)
					return doc;
			}
			
			Header contentTypeLength = method.getResponseHeader(HTTPHEADER_CONTENT_LENGTH);
			if (contentTypeLength != null) {
				final boolean contentLengthAccepted = handleContentTypeLength(contentTypeLength, doc);
				if (!contentLengthAccepted)
					return doc;
			}
			
			// secondly - if everything is alright up to now - proceed with getting the actual
			// document
			
			// generate the GET request method
			HttpMethod getMethod = new GetMethod(requestUrl);		// automatically follows redirects
			method.releaseConnection();
			
			method = getMethod;
			initRequestMethod(method);
			
			// send the request to the server
			statusCode = this.getHttpClient().executeMethod(method);
			
			// check the response status code
			if (statusCode != HttpStatus.SC_OK) {
				if (statusCode == HttpStatus.SC_NOT_FOUND) {
					doc.setStatus(ICrawlerDocument.Status.NOT_FOUND);
				} else {
					doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, String.format("Server returned: %s", method.getStatusLine()));
				}
				
				this.logger.warn(String.format("Crawling of URL '%s' failed. Server returned: %s", requestUrl, method.getStatusLine()));
				return doc;
			}
			
			// getting the mimetype and charset
			// XXX needed here again?
			contentTypeHeader = method.getResponseHeader(HTTPHEADER_CONTENT_TYPE);
			if (contentTypeHeader != null) {
				final boolean mimeTypeOk = handleContentTypeHeader(contentTypeHeader, doc);
				if (!mimeTypeOk)
					return doc;
			}
			
			// getting the document languages
			Header contentLanguageHeader = method.getResponseHeader(HTTPHEADER_CONTENT_LANGUAGE);
			if (contentLanguageHeader != null) {
				String contentLanguage = contentLanguageHeader.getValue();
				String[] languages = contentLanguage.split(",");
				doc.setLanguages(languages);
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
			// TODO: add gzip/deflate support
			
			// copy the content to file
			CrawlerTools.saveInto(doc, respBody);
			respBody.close();
			
			doc.setStatus(ICrawlerDocument.Status.OK);
			this.logger.info(String.format("Crawling of URL '%s' finished.", requestUrl));
		} catch (NoRouteToHostException e) {
			this.logger.error(String.format("Error crawling %s: %s", requestUrl, e.getMessage()));
			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
		} catch (UnknownHostException e) {
			this.logger.error(String.format("Error crawling %s: Unknown host.", requestUrl));
			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());	
		} catch (ConnectException e) {
			this.logger.error(String.format("Error crawling %s: Unable to connect to host.", requestUrl));
			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
		} catch (ConnectTimeoutException e) {
			this.logger.error(String.format("Error crawling %s: %s.", requestUrl, e.getMessage()));
			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
		} catch (SocketTimeoutException e) {
			this.logger.error(String.format("Error crawling %s: Connection timeout.", requestUrl));
			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
		} catch (CircularRedirectException e) {
			this.logger.error(String.format("Error crawling %s: %s", requestUrl, e.getMessage()));
			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
		} catch (Throwable e) {
			String errorMsg;
			if (e instanceof HttpException) {
				errorMsg = "Unrecovered protocol exception: [%s] %s";
			} else if (e instanceof IOException) {
				errorMsg = "Transport exceptions: [%s] %s";
			} else {
				errorMsg = "Unexpected exception: [%s] %s";
			}
			errorMsg = String.format(errorMsg, e.getClass().getName(), e.getMessage());
			
			this.logger.error(String.format("Error crawling %s: %s", requestUrl, errorMsg));
			doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, errorMsg);
			e.printStackTrace();
		} finally {
			if (method != null) method.releaseConnection();
		}
		
		return doc;
	}
}
