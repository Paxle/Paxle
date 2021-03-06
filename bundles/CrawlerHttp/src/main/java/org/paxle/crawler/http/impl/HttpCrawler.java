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

package org.paxle.crawler.http.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.apache.commons.httpclient.CircularRedirectException;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.osgi.framework.BundleContext;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.prefs.IPropertiesStore;
import org.paxle.core.prefs.Properties;
import org.paxle.crawler.ContentLengthLimitExceededException;
import org.paxle.crawler.ICrawlerContext;
import org.paxle.crawler.ICrawlerContextAware;
import org.paxle.crawler.ICrawlerContextLocal;
import org.paxle.crawler.ICrawlerTools;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ICrawlerTools.ILimitedRateCopier;

@Component(
	name=HttpCrawler.PID,
	immediate=true,
	metatype=false	
)
@Services({
	@Service(ISubCrawler.class),
	@Service(ICrawlerContextAware.class)
})
@Property(name=ISubCrawler.PROP_PROTOCOL, value={"http","https"})
public class HttpCrawler implements ISubCrawler, ICrawlerContextAware {
	/* =========================================================
	 * Config Properties
	 * ========================================================= */
	static final String PID = "org.paxle.crawler.http.IHttpCrawler";
	
	@Property(intValue=15000)
	static final String PROP_CONNECTION_TIMEOUT 		= PID + '.' + "connectionTimeout";
	@Property(intValue=15000)
	static final String PROP_SOCKET_TIMEOUT 			= PID + '.' + "socketTimeout";
	@Property(intValue=15)
	static final String PROP_MAXCONNECTIONS_PER_HOST 	= PID + '.' + "maxConnectionsPerHost";
	@Property(intValue=10485760)
	static final String PROP_MAXDOWNLOAD_SIZE 			= PID + '.' + "maxDownloadSize";
	@Property(boolValue=true)
	static final String PROP_ACCEPT_ENCODING 			= PID + '.' + "acceptEncodings";
	@Property(boolValue=true)
	static final String PROP_SKIP_UNSUPPORTED_MIMETYPES = PID + '.' + "skipUnsupportedMimeTypes";
	@Property(intValue=-1)
	static final String PROP_TRANSFER_LIMIT 			= PID + '.' + "transferLimit";			// in KB/s
	@Property(value=CookiePolicy.BROWSER_COMPATIBILITY)
	static final String PROP_COOKIE_POLICY 			    = PID + '.' + "cookiePolicy";
	@Property(value="Mozilla/5.0 (compatible; ${paxle.userAgent}/${paxle.version}; +http://www.paxle.net/en/bot)")
	static final String PROP_USER_AGENT 			    = PID + '.' + "userAgent";
	
	@Property(boolValue=false)
	static final String PROP_PROXY_USE 					= PID + '.' + "useProxy";
	@Property(value="")
	static final String PROP_PROXY_HOST 				= PID + '.' + "proxyHost";
	@Property(intValue=3128)
	static final String PROP_PROXY_PORT 				= PID + '.' + "proxyPort";
	@Property(value="")
	static final String PROP_PROXY_USER 				= PID + '.' + "proxyUser";
	@Property(value="")
	static final String PROP_PROXY_PASSWORD 			= PID + '.' + "proxyPassword";
	
	/* =========================================================
	 * Constants for HTTP headers
	 * ========================================================= */
	private static final String HTTPHEADER_LAST_MODIFIED = "Last-Modified";
	private static final String HTTPHEADER_DATE = "Date";
	private static final String HTTPHEADER_CONTENT_LANGUAGE = "Content-Language";
	private static final String HTTPHEADER_CONTENT_TYPE = "Content-Type";
	private static final String HTTPHEADER_CONTENT_LENGTH = "Content-Length";
	private static final String HTTPHEADER_CONTENT_ENCODING = "Content-Encoding";
	private static final String HTTPHEADER_ACCEPT_ENCODING = "Accept-Encoding";
	
	private static final int PREF_NO_ENCODING = 1;
	
	/**
	 * The MIME-type detection of some servers is not that mature, i.e. Apache often tends to
	 * report 'text/plain' for binary files or 'application/x-tar' for compressed tar-archives,
	 * which does not help us at all. This set contains MIME-types, known to be reported erroneously
	 * by servers in general.
	 * Of course this list could be divided further by extending it into a per-server map, but
	 * our means to determine the type (and possibly version) of the servers here are limited, so
	 * this shall suffice for now.
	 */
	private static final HashSet<String> ERRONEOUS_MIME_TYPES = new HashSet<String>(Arrays.asList(
		"text/plain",
		"application/x-tar"
	));
	
	protected ICrawlerContextLocal contextLocal;
	
	@Reference
	protected IPropertiesStore propstore;
	
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
	
	private boolean acceptEncoding = true;
	
	private boolean skipUnsupportedMimeTypes = true;
	
	private ILimitedRateCopier lrc = null;
	
	/**
	 * The Cookie policy to use for crawling
	 * @see CookiePolicy
	 */
	private String cookiePolicy = null;
	
	/**
	 * The User-Agent header the crawler should use
	 */
	private String userAgent = null;
	
	private Properties props = null;
	
	private ConcurrentHashMap<String,Integer> hostSettings;
	
	@Activate
	public void activate(BundleContext context, Map<String,Object> config) {
		// reading the configuration
		this.modified(config);
		
		// registering the protocol handler for https 
	    Protocol.registerProtocol("https", new Protocol("https", new AllSSLProtocolSocketFactory(), 443));
		
		// getting the component preferences	
		if (this.propstore != null) {
			this.props = this.propstore.getProperties(context);
			if (props != null) {
				final Set<Object> keySet = props.keySet();
				this.hostSettings = new ConcurrentHashMap<String,Integer>(keySet.size(), 0.75f, 10);
				for (final Object o : keySet) {
					final String key = (String)o;
					this.hostSettings.put(key, Integer.valueOf(props.getProperty(key)));
				}
			}
		}
		
		if(this.hostSettings == null) {
			this.hostSettings = new ConcurrentHashMap<String,Integer>(10, 0.75f, 10);
		}
	}
	
	@Deactivate
	public void deactivate() {
		this.cleanup();
		this.saveProperties();
	}
	
	public void setCrawlerContextLocal(ICrawlerContextLocal contextLocal) {
		this.contextLocal = contextLocal;		
	}
	
	public void saveProperties() {
		if (props != null) {
			for (Map.Entry<String,Integer> e : hostSettings.entrySet())
				props.setProperty(e.getKey(), Integer.toString(e.getValue().intValue()));
		}
	}
	
	private boolean isHostSettingSet(final String host, final int pref) {
		final Integer i = hostSettings.get(host);
		if (i == null) return false;
		return (i.intValue() & pref) != 0;
	}
	
	private void setHostSetting(final String host, final int pref) {
		final Integer i = hostSettings.get(host);
		final int val = (i == null) ? pref : (i.intValue() | pref);
		this.hostSettings.put(host, Integer.valueOf(val));
	}
	
	/**
	 * cleanup old settings
	 */
	private void cleanup() {
		if (this.connectionManager != null) {
			this.connectionManager.shutdown();
			this.connectionManager = null;
		}
		if (this.httpClient != null) {
			this.httpClient = null;
		}
	}
	
	@Modified
	public synchronized void modified(Map<String,Object> configuration) {
		/*
		 * Cleanup old config
		 */
		this.cleanup();
		
		/*
		 * Init with changed configuration
		 */
		this.connectionManager = new MultiThreadedHttpConnectionManager();
		final HttpConnectionManagerParams connectionManagerParams = connectionManager.getParams();
		
		// configure connections per host
		final Integer maxConnections = (Integer) configuration.get(PROP_MAXCONNECTIONS_PER_HOST);
		if (maxConnections != null) {
			connectionManagerParams.setDefaultMaxConnectionsPerHost(maxConnections.intValue());
		}
		
		// configuring timeouts
		final Integer connectionTimeout = (Integer) configuration.get(PROP_CONNECTION_TIMEOUT);
		if (connectionTimeout != null) {
			connectionManagerParams.setConnectionTimeout(connectionTimeout.intValue());
		}
		final Integer socketTimeout = (Integer) configuration.get(PROP_SOCKET_TIMEOUT);
		if (socketTimeout != null) {
			connectionManagerParams.setSoTimeout(socketTimeout.intValue());
		}
		
		// set new http client
		this.httpClient = new HttpClient(connectionManager);		
		
		// the crawler should request and accept content-encoded data
		final Boolean acceptEncoding = (Boolean)configuration.get(PROP_ACCEPT_ENCODING);
		if (acceptEncoding != null) {
			this.acceptEncoding = acceptEncoding.booleanValue();
		}
		
		// specifies if the crawler should skipp unsupported-mime-types
		final Boolean skipUnsupportedMimeTypes = (Boolean)configuration.get(PROP_SKIP_UNSUPPORTED_MIMETYPES);
		if (skipUnsupportedMimeTypes != null) {
			this.skipUnsupportedMimeTypes = skipUnsupportedMimeTypes.booleanValue();
		}
		
		// the cookie policy to use for crawling
		final String propCookiePolicy = (String)configuration.get(PROP_COOKIE_POLICY);
		this.cookiePolicy = (propCookiePolicy == null || propCookiePolicy.length()==0) ? CookiePolicy.BROWSER_COMPATIBILITY : propCookiePolicy;
		
		// the http-user-agent string that should be used
		final String userAgent = (String)configuration.get(PROP_USER_AGENT);
		if (userAgent != null) {
			StringBuffer buf = new StringBuffer();
			Pattern pattern = Pattern.compile("\\$\\{[^\\}]*}");
			Matcher matcher = pattern.matcher(userAgent);

			// replacing property placeholders with system-properties
			while (matcher.find()) {
				String placeHolder = matcher.group();
				String propName = placeHolder.substring(2,placeHolder.length()-1);					
				String propValue = System.getProperty(propName);
				if (propValue != null) matcher.appendReplacement(buf, propValue);
			}
			matcher.appendTail(buf);
			
			this.userAgent = buf.toString();
		} else {
			// Fallback
			this.userAgent = "PaxleFramework";
		}
		
		// download limit in bytes
		final Integer maxDownloadSize = (Integer)configuration.get(PROP_MAXDOWNLOAD_SIZE);
		if (maxDownloadSize != null) {
			this.maxDownloadSize = maxDownloadSize.intValue();
		}
		
		// limit data transfer rate
		final Integer transferLimit = (Integer)configuration.get(PROP_TRANSFER_LIMIT);
		int limitKBps = 0;
		if (transferLimit != null)
			limitKBps = transferLimit.intValue();
		this.logger.debug("transfer rate limit: " + limitKBps + " kb/s");
		// TODO: lrc = (limitKBps > 0) ? new CrawlerTools.LimitedRateCopier(limitKBps) : null;
		
		// proxy configuration
		final Boolean useProxyVal = (Boolean)configuration.get(PROP_PROXY_USE);
		final String host = (String)configuration.get(PROP_PROXY_HOST);
		final Integer portVal = (Integer)configuration.get(PROP_PROXY_PORT);
		
		if (useProxyVal != null && useProxyVal.booleanValue() && host != null && host.length() > 0 && portVal != null) {
			this.logger.info(String.format(
				"Proxy is enabled: %s:%d",
				host, portVal
			));
			
			final int port = portVal.intValue();
			final ProxyHost proxyHost = new ProxyHost(host, port);
			this.httpClient.getHostConfiguration().setProxyHost(proxyHost);
			
			final String user = (String)configuration.get(PROP_PROXY_HOST);
			final String pwd = (String)configuration.get(PROP_PROXY_PASSWORD);
			if (user != null && user.length() > 0 && pwd != null && pwd.length() > 0)
				this.httpClient.getState().setProxyCredentials(
						new AuthScope(host, port),
						new UsernamePasswordCredentials(user, pwd));
		} else {
			this.logger.info("Proxy is disabled");
			
			this.httpClient.getHostConfiguration().setProxyHost(null);
			this.httpClient.getState().clearCredentials();
		}
	}
	
	/**
	 * This method is synchronized with {@link #modified(Dictionary)} to avoid
	 * problems during configuration update.
	 * 
	 * @return the {@link HttpClient} to use
	 */
	private synchronized HttpClient getHttpClient() {
		return this.httpClient;
	}
	
	/**
	 * Initializes the {@link HttpMethod} with common attributes for all requests this crawler
	 * initiates.
	 * <p>
	 * Currently the following attributes (represented as HTTP header values in the final request)
	 * are set:
	 * <ul>
	 *   <li>the cookie policy to use ({@link #PROP_COOKIE_POLICY})</li>
	 *   <li>
	 *     if enabled, content-transformation using <code>compress</code>, <code>gzip</code> and
	 *     <code>deflate</code> is supported</li>
	 *   </li> 
	 * </ul>
	 * 
	 * @param method the method to set the standard attributes on
	 */
	private void initRequestMethod(final HttpMethod method) throws URIException {
		method.getParams().setCookiePolicy(this.cookiePolicy==null?CookiePolicy.BROWSER_COMPATIBILITY:this.cookiePolicy);
		if (acceptEncoding && !isHostSettingSet(method.getURI().getHost(), PREF_NO_ENCODING))
			method.setRequestHeader(HTTPHEADER_ACCEPT_ENCODING, "compress, gzip, identity, deflate");	// see RFC 2616, section 14.3
		
		// set some additional http headers
		if (this.userAgent != null) {
			method.setRequestHeader("User-Agent",this.userAgent);
		}
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
	 * @param doc the {@link ICrawlerDocument} the resulting MIME-type and charset shall be set in
	 * @return <code>true</code> if proceeding with the URL may continue or <code>false</code> if
	 *         it shall be aborted due to an unsupported MIME-type of the requested document
	 */
	boolean handleContentTypeHeader(final Header contentTypeHeader, final ICrawlerDocument doc) {
		if (contentTypeHeader == null) {
			// might be ok, might be not, we don't know yet
			return true;
		}
		
		// separate MIME-type and charset from the content-type specification
		String contentMimeType = null;
		String contentCharset = null;
		contentMimeType = contentTypeHeader.getValue();
		
		int idx = contentMimeType.indexOf(";");
		if (idx != -1) {
			contentCharset = contentMimeType.substring(idx+1).trim();
			contentMimeType = contentMimeType.substring(0,idx);
			
			if (contentCharset.toLowerCase().startsWith("charset=")) {
				contentCharset = contentCharset.substring("charset=".length()).trim();
				
				idx = contentCharset.indexOf(";");
				if (idx != -1) contentCharset = contentCharset.substring(0,idx);
				
				if (contentCharset.matches("^['\"].*")) {
					contentCharset = contentCharset.substring(1);
				}
				if (contentCharset.matches(".*['\"]$")) {
					contentCharset = contentCharset.substring(0,contentCharset.length()-1);							
				}
				doc.setCharset(contentCharset.trim());
			} else {
				contentCharset = null;
			}
		}
		
		// check against common MIME-types wrongly attributed to files by servers
		// if this is one of them, we just ignore the MIME-type and let the MimeType-bundle do the job
		if (!ERRONEOUS_MIME_TYPES.contains(contentMimeType)) {
			doc.setMimeType(contentMimeType);
			
			// getting the crawler-context
			final ICrawlerContext context = this.contextLocal.getCurrentContext();

			// check if there is any parser installed, supporting this mime-type
			if (this.skipUnsupportedMimeTypes) {				
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
			}
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
	 * No values are set in the {@link ICrawlerDocument} in case of a positive result, otherwise
	 * the document's <code>result</code> is set to {@link ICrawlerDocument.Status#UNKNOWN_FAILURE}
	 * and a warning message is logged.
	 * 
	 * @see #maxDownloadSize
	 * @param contentTypeLength the HTTP-{@link Header}'s <code>Content-Length</code> attribute
	 * @param doc the {@link ICrawlerDocument} the resulting MIME-type and charset shall be set in
	 * @return <code>true</code> if proceeding with the URL may continue or <code>false</code> if
	 *         it shall be aborted due to an exceeded maximum file-size of the document
	 */
	private boolean handleContentLengthHeader(final Header contentTypeLength, final ICrawlerDocument doc) {
		if (contentTypeLength == null) {
			// no Content-Length given, continue
			return true;
		}
		
		final int maxDownloadSize = this.maxDownloadSize;
		if (maxDownloadSize < 0) {
			// no maximum specified, continue
			return true;
		}
		
		// extract the content length in bytes
		final String lengthString = contentTypeLength.getValue();
		if (lengthString.length() > 0 && lengthString.matches("\\d+")) {
			final int contentLength = Integer.parseInt(lengthString);
			if (contentLength > maxDownloadSize) {
				// reject the document
				final String msg = String.format(
						"Content-length '%d' of resource '%s' is larger than the max. allowed size of '%d' bytes.",
						Integer.valueOf(contentLength),
						doc.getLocation(),
						Integer.valueOf(maxDownloadSize)
				);
				
				this.logger.warn(msg);
				doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, msg);
				return false;
			}
		}
		
		// continue
		return true;
	}
	
	public ICrawlerDocument request(URI requestUri) {
		if (requestUri == null) throw new NullPointerException("URL was null");
		this.logger.debug(String.format("Crawling URL '%s' ...",requestUri));
		
		ICrawlerDocument doc = null;
		HttpMethod method = null;
		try {
			final ICrawlerContext ctx = this.contextLocal.getCurrentContext();
			
			// creating an empty crawler-document
			doc = ctx.createDocument();				
			doc.setLocation(requestUri);
			
			final String uriAsciiString = requestUri.toASCIIString();
			
			/* ==============================================================================
			 * HTTP HEAD request
			 * 
			 * first use the HEAD method to determine whether the MIME-type is supported
			 * and to compare the content-length with the maximum allowed download size
			 * (both only if the server provides this information, if not, the file is
			 * fetched)
			 * ============================================================================== */
			method = new HeadMethod(uriAsciiString);		// automatically follows redirects
			this.initRequestMethod(method);			
			int statusCode = this.getHttpClient().executeMethod(method);
			
			final boolean headUnsupported = (statusCode == HttpStatus.SC_METHOD_FAILURE || statusCode == HttpStatus.SC_METHOD_NOT_ALLOWED);
			if (!headUnsupported) {
				if (statusCode != HttpStatus.SC_OK) {
					// RFC 2616 states that the GET and HEAD methods _must_ be supported by any
					// general purpose servers (which are in fact the ones we are connecting to here)
					
					if (statusCode == HttpStatus.SC_NOT_FOUND) {
						doc.setStatus(ICrawlerDocument.Status.NOT_FOUND);
					} else {
						doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, String.format("Server returned: %s", method.getStatusLine()));
					}
					
					this.logger.warn(String.format("Crawling of URL '%s' failed. Server returned: %s", requestUri, method.getStatusLine()));
					return doc;
				}
				
				// getting the mimetype and charset
				Header contentTypeHeader = method.getResponseHeader(HTTPHEADER_CONTENT_TYPE);
				if (!handleContentTypeHeader(contentTypeHeader, doc))
					return doc;
				
				// reject the document if content-length is above our limit
				Header contentLengthHeader = method.getResponseHeader(HTTPHEADER_CONTENT_LENGTH);
				if (!handleContentLengthHeader(contentLengthHeader, doc))
					return doc;
				
				// FIXME: we've been redirected, re-enqueue the new URL and abort processing
				//if (!requestUri.equals(method.getURI())) ;				
			}
			
			/* ==============================================================================
			 * HTTP GET request
			 * 
			 * secondly - if everything is alright up to now - proceed with getting the 
			 * actual document
			 * ============================================================================== */
			HttpMethod getMethod = new GetMethod(uriAsciiString);		// automatically follows redirects
			method.releaseConnection();
			
			method = getMethod;
			this.initRequestMethod(method);
			
			// send the request to the server
			statusCode = this.getHttpClient().executeMethod(method);
			
			// check the response status code
			if (statusCode != HttpStatus.SC_OK) {
				if (statusCode == HttpStatus.SC_NOT_FOUND) {
					doc.setStatus(ICrawlerDocument.Status.NOT_FOUND);
				} else {
					doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, String.format("Server returned: %s", method.getStatusLine()));
				}
				
				this.logger.warn(String.format("Crawling of URL '%s' failed. Server returned: %s", requestUri, method.getStatusLine()));
				return doc;
			}
			
			// FIXME: we've been redirected, re-enqueue the new URL and abort processing
			// if (!requestUri.equals(method.getURI())) ; 
			
			/*
			 * HTTP Content-Type
			 * - getting the mimetype and charset
			 */
			Header contentTypeHeader = method.getResponseHeader(HTTPHEADER_CONTENT_TYPE);
			if (!handleContentTypeHeader(contentTypeHeader, doc))
				return doc;
			
			/* 
			 * HTTP Content-Length
			 * - Reject the document if content-length is above our limit
			 * 
			 *   We do this a second time here because some servers may have set the content-length
			 *   of the head response to <code>0</code>
			 */
			Header contentLengthHeader = method.getResponseHeader(HTTPHEADER_CONTENT_LENGTH);
			if (!handleContentLengthHeader(contentLengthHeader, doc))
				return doc;			
			
			extractHttpHeaders(method, doc);		// externalised into this method to cleanup here a bit
			
			// getting the response body
			InputStream respBody = method.getResponseBodyAsStream();
			
			// handle the content-encoding, i.e. decompress the server's response
			Header contentEncodingHeader = method.getResponseHeader(HTTPHEADER_CONTENT_ENCODING);
			try {
				respBody = handleContentEncoding(contentEncodingHeader, respBody);
				
				/* Limit the max allowed length of the content to copy. -1 is used for no limit.
				 * 
				 * We need to set a limit if:
				 * a) the user has configured a max-download-size AND
				 * b) the server returned no content-length header
				 */
				int copyLimit = (this.maxDownloadSize <= 0 || contentLengthHeader != null) ? -1 : this.maxDownloadSize;
				
				// copy the content to file
				final ICrawlerTools crawlerTools = ctx.getCrawlerTools();
				crawlerTools.saveInto(doc, respBody, lrc, copyLimit);
				
				doc.setStatus(ICrawlerDocument.Status.OK);
				this.logger.debug(String.format("Crawling of URL '%s' finished.", requestUri));
			} catch (IOException e) {
				String msg = e.getMessage();
				if (msg == null || !msg.equals("Corrupt GZIP trailer"))
					throw e;
				
				setHostSetting(method.getURI().getHost(), PREF_NO_ENCODING);
				msg = String.format("server sent a corrupt gzip trailer at URL '%s'", requestUri);
				logger.warn(msg);
				
				// FIXME re-enqueue command
				doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, msg);
			} finally {
				respBody.close();
			}
		} catch (NoRouteToHostException e) {
			this.logger.warn(String.format("Error crawling %s: %s", requestUri, e.getMessage()));
			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
		} catch (UnknownHostException e) {
			this.logger.warn(String.format("Error crawling %s: Unknown host.", requestUri));
			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());	
		} catch (ConnectException e) {
			this.logger.warn(String.format("Error crawling %s: Unable to connect to host.", requestUri));
			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
		} catch (ConnectTimeoutException e) {
			this.logger.warn(String.format("Error crawling %s: %s.", requestUri, e.getMessage()));
			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
		} catch (SocketTimeoutException e) {
			this.logger.warn(String.format("Error crawling %s: Connection timeout.", requestUri));
			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
		} catch (CircularRedirectException e) {
			this.logger.warn(String.format("Error crawling %s: %s", requestUri, e.getMessage()));
			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
		} catch (NoHttpResponseException e) {
			this.logger.warn(String.format("Error crawling %s: %s", requestUri, e.getMessage()));
			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
		} catch (ContentLengthLimitExceededException e) {
			this.logger.warn(String.format("Error crawling %s: %s", requestUri, e.getMessage()));
			doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, e.getMessage());
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
			
			this.logger.error(String.format("Error crawling %s: %s", requestUri, errorMsg));
			doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, errorMsg);
			e.printStackTrace();
		} finally {
			if (method != null) method.releaseConnection();
		}
		
		return doc;
	}
	
	private static void extractHttpHeaders(final HttpMethod method, final ICrawlerDocument doc) throws IOException {
		// getting the document languages
		Header contentLanguageHeader = method.getResponseHeader(HTTPHEADER_CONTENT_LANGUAGE);
		if (contentLanguageHeader != null) {
			String contentLanguage = contentLanguageHeader.getValue();
			String[] languages = contentLanguage.split(",");
			doc.setLanguages(languages);
		}
		
		// crawling Date
		Date crawlingDate = null;
		Header crawlingDateHeader = method.getResponseHeader(HTTPHEADER_DATE);			
		if (crawlingDateHeader == null) {
			crawlingDate = new Date();
		} else try {
			String dateStr = crawlingDateHeader.getValue();
			crawlingDate = DateUtil.parseDate(dateStr);
		} catch (DateParseException e) {
			crawlingDate = new Date();
		}
		doc.setCrawlerDate(crawlingDate);
		
		// last mod date
		Date lastModDate = null;
		Header lastModDateHeader = method.getResponseHeader(HTTPHEADER_LAST_MODIFIED);
		if (lastModDateHeader != null) try {
			String dateStr = lastModDateHeader.getValue();
			lastModDate = DateUtil.parseDate(dateStr);
		} catch (DateParseException e) {
			lastModDate = crawlingDate;
		}
		doc.setLastModDate(lastModDate);			
		
		// ETAG
		// XXX: this is protocol specific. How to store this in a generic crawler-document?
//		Header etageHeader = method.getResponseHeader(HTTPHEADER_ETAG);
//		if (etageHeader != null) {
//			String etag = etageHeader.getValue();
//			doc.setEtag(etag);
//		}
	}
	
	private static InputStream handleContentEncoding(final Header contentEncodingHeader, InputStream responseBody) throws IOException {
		if (contentEncodingHeader == null) return responseBody;
		
		final String contentEncoding = contentEncodingHeader.getValue();
		final StringTokenizer st = new StringTokenizer(contentEncoding, ",");
		
		// apply decompression methods in the order given, see RFC 2616, section 14.11		
		while (st.hasMoreTokens()) {
			String encoding = st.nextToken().trim();
			// the "identity"-encoding does not need any transformation
			
			if (encoding.equals("deflate")) {
				responseBody = new ZipInputStream(responseBody);
			} else {
				// support for the recommendation of the W3C, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.5
				if (encoding.startsWith("x-"))
					encoding = encoding.substring("x-".length());
				if (encoding.equals("gzip") || encoding.equals("compress")) {
					responseBody = new GZIPInputStream(responseBody);
				}
			}
		}
		return responseBody;
	}
}
