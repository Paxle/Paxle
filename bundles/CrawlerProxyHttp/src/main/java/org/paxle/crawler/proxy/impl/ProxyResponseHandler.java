package org.paxle.crawler.proxy.impl;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.crawler.CrawlerContext;
import org.paxle.crawler.proxy.impl.io.ExptenedPipedInputStream;
import org.paxle.crawler.proxy.impl.io.ExtendedPipedOutputStream;
import org.xsocket.Execution;
import org.xsocket.connection.IConnection.FlushMode;
import org.xsocket.connection.http.BodyDataSink;
import org.xsocket.connection.http.HttpRequest;
import org.xsocket.connection.http.HttpRequestHeader;
import org.xsocket.connection.http.HttpResponse;
import org.xsocket.connection.http.HttpResponseHeader;
import org.xsocket.connection.http.client.IHttpResponseHandler;
import org.xsocket.connection.http.client.IHttpResponseTimeoutHandler;
import org.xsocket.connection.http.server.IHttpResponseContext;

public class ProxyResponseHandler implements IHttpResponseHandler, IHttpResponseTimeoutHandler {
	public static final String HTTPHEADER_CONTENT_LANGUAGE = "Content-Language";
	public static final String HTTPHEADER_DATE = "Date";
	public static final String HTTPHEADER_LAST_MODIFIED = "Last-Modified";
	public static final String HTTPHEADER_ETAG = "ETag";
	public static final String HTTPHEADER_CONNECTION = "Connection";
	public static final String HTTPHEADER_KEEP_ALIVE = "Keep-Alive";
	public static final String HTTPHEADER_PROXY_AUTHENTICATE = "Proxy-Authenticate";
	public static final String HTTPHEADER_TRAILER = "Trailer";
	public static final String HTTPHEADER_UPGRADE = "Upgrade";
	public static final String HTTPHEADER_PRAGMA = "Pragma";
	public static final String HTTPHEADER_CACHE_CONTROL = "Cache-Control";
	public static final String HTTPHEADER_AUTHORIZATION = "Authorization";
	public static final String HTTPHEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
	public static final String HTTPHEADER_CONTENT_RANGE = "Content-Range";
	public static final String HTTPHEADER_RANGE = "Range";
	public static final String HTTPHEADER_COOKIE = "Cookie";
	public static final String HTTPHEADER_SET_COOKIE = "Set-Cookie";
	public static final String HTTPHEADER_SET_COOKIE2 = "Set-Cookie2";

	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Original http-request
	 */
	private final HttpRequest req;
	
	/**
	 * Reponse-message context
	 */
	private final IHttpResponseContext responseCtx;

	public ProxyResponseHandler(HttpRequest req, IHttpResponseContext responseCtx) {
		this.req = req;
		this.responseCtx = responseCtx;
	}

	/**
	 * @see IHttpResponseHandler#onResponse(HttpResponse)
	 */
	@Execution(Execution.MULTITHREADED)
	public void onResponse(HttpResponse response) throws IOException {
		final HttpRequestHeader reqHdr = this.req.getRequestHeader();
		final HttpResponseHeader resHdr = response.getResponseHeader();		
		
		try {	

			PipedInputStream is = null; 
			PipedOutputStream ps = null;

			// removing hop-by-hop headers
			response.removeHopByHopHeaders("CONNECTION", "KEEP-ALIVE", 
                    "PROXY-AUTHENTICATE", "TRAILER", "UPGRADE");

			// check if crawling of this resource is allowed
			boolean shouldCrawl = this.shouldCrawl(reqHdr, resHdr);
			if (shouldCrawl) {
				is = new ExptenedPipedInputStream();
				ps = new ExtendedPipedOutputStream(is);
			}

			BodyDataSink dataSink = null;
			if (resHdr.getContentLength() < 0) { 
				dataSink = responseCtx.send(resHdr);
			} else {
				dataSink = responseCtx.send(resHdr,resHdr.getContentLength());
			}
			dataSink.setFlushmode(FlushMode.ASYNC);

			boolean hasBody = response.hasBody();
			if (hasBody) response.getNonBlockingBody().setDataHandler(new ProxyForwarder(ps,dataSink));

			if (hasBody && shouldCrawl) {
				new ProxyDataProvider(reqHdr.getTargetURI(),resHdr, is).start();
			}
		} catch (Throwable e) {
			String msg = String.format("Unexpected '%s' while requesting '%s': %s",e.getClass().getName(),reqHdr.getTargetURI() ,e.getMessage());
			this.logger.error(msg,e);
			throw new IOException(msg);
		}
	}

	/**
	 * @see IHttpResponseTimeoutHandler#onResponseTimeout()
	 */
	public void onResponseTimeout() throws IOException {
		responseCtx.sendError(504);
	}
	
	private boolean shouldCrawl(final HttpRequestHeader reqHdr, final HttpResponseHeader resHdr) {
		URI uri = reqHdr.getTargetURI();
		
		/*
		 * Only accept repsonse with 200/203
		 */
		if (resHdr.getStatus() != 200 && resHdr.getStatus() != 203) {
			this.logger.info(String.format("Crawling of '%s' disallowed: Invalid statuscode %d.",uri,resHdr.getStatus()));
			return false;
		}
		
		/*
		 * Ignore URLs with query-parameters
		 */
		String queryString = reqHdr.getQueryString();
		if (queryString != null) {
			this.logger.info(String.format("Crawling of '%s' disallowed: Query parameters found '%s'.",uri,queryString));
			return false;
		}
		
		/*
		 * check if we support the mimetype
		 */
		final CrawlerContext context = CrawlerContext.getCurrentContext();
		if (context == null) throw new RuntimeException("Unexpected error. The crawler-context was null.");
		
		String contentType = resHdr.getContentType();
		int idx = contentType.indexOf(";");
		if (idx != -1) contentType = contentType.substring(0,idx).trim();
		
		if (contentType != null && !context.getSupportedMimeTypes().contains(contentType)) {
			this.logger.info(String.format("Crawling of '%s' disallowed: Unsupported mimetype '%s'",uri,contentType));
			return false;
		}
		
		/*
		 * Block websites with authentication
		 */
		if (reqHdr.containsHeader(HTTPHEADER_AUTHORIZATION) ||
			resHdr.containsHeader(HTTPHEADER_WWW_AUTHENTICATE)) {
			this.logger.info(String.format("Crawling of '%s' disallowed: Authorization required.",uri));
			return false;
		}
		
		/*
		 * Bock content-range request- and response-messages
		 */
		if (reqHdr.containsHeader(HTTPHEADER_RANGE) ||
			resHdr.containsHeader(HTTPHEADER_CONTENT_RANGE)) {
			this.logger.info(String.format("Crawling of '%s' disallowed: Content-range request.",uri));
			return false;
		}
		
		/*
		 * Block sites with pragma/cache-control private/no-cache/no-store
		 */
		String pragma = resHdr.getHeader(HTTPHEADER_PRAGMA);
		if (pragma != null) {
			if (pragma.equalsIgnoreCase("no-cache")) {
				this.logger.info(String.format("Crawling of '%s' disallowed: pragma '%s'",uri,pragma));
				return false;
			}
		}
		
		String cacheControl = resHdr.getHeader(HTTPHEADER_CACHE_CONTROL);
		if (cacheControl != null) {
			cacheControl = cacheControl.toLowerCase().trim();
			if (cacheControl.startsWith("private") || 
				cacheControl.startsWith("no-cache") || 
				cacheControl.startsWith("no-store") || 
				cacheControl.startsWith("max-age=0")) {
				this.logger.info(String.format("Crawling of '%s' disallowed: Cache-Control '%s'",uri,cacheControl));
				return false;
			}
		}
		
		/*
		 * No sites with cookies
		 */
		if (reqHdr.containsHeader(HTTPHEADER_COOKIE) ||
			resHdr.containsHeader(HTTPHEADER_SET_COOKIE) ||
			resHdr.containsHeader(HTTPHEADER_SET_COOKIE2)) {
			this.logger.info(String.format("Crawling of '%s' disallowed: Cookies found.",uri));
			return false;
		}
		
		return true;
	}
}
