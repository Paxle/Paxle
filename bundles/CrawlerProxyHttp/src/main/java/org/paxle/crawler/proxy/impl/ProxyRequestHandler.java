package org.paxle.crawler.proxy.impl;

import java.io.IOException;
import java.net.ConnectException;

import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.xsocket.Execution;
import org.xsocket.ILifeCycle;
import org.xsocket.connection.http.HttpRequest;
import org.xsocket.connection.http.client.HttpClient;
import org.xsocket.connection.http.server.IHttpRequestHandler;
import org.xsocket.connection.http.server.IHttpResponseContext;

public class ProxyRequestHandler implements IHttpRequestHandler, ILifeCycle {

	/**
	 * HTTP-client component to forward {@link HttpRequest}s to a remote server
	 */
	private HttpClient httpClient = null;	
	
	/**
	 * OSGi Service to tracke the {@link UserAdmin} service.
	 */
	private ServiceTracker userAgentTracker = null;
	
	public ProxyRequestHandler(ServiceTracker userAgentTracker) {
		if (userAgentTracker == null) throw new NullPointerException("The user-agent-tracker must not be null.");		
		this.userAgentTracker = userAgentTracker;
	}
	
	/**
	 * @see IHttpRequestHandler#onRequest(HttpRequest, IHttpResponseContext)
	 */
	@Execution(Execution.MULTITHREADED)
	public void onRequest(HttpRequest req, IHttpResponseContext responseCtx) throws IOException {
		try {

			// remove the named hop-by-hop headers
			req.removeHopByHopHeaders("CONNECTION", "PROXY-AUTHORIZATION", "TRAILER", "UPGRADE");

			// remove accept encoding for now
			req.removeHeader("Accept-Encoding");	      
			
			// TODO: add proxy-auth. here

			// .. and forward the request
			httpClient.send(req, new ProxyResponseHandler(req, responseCtx));
		} catch (ConnectException ce) {
			responseCtx.sendError(502, ce.toString());
		}
	}

	/**
	 * @see ILifeCycle#onDestroy()
	 */
	public void onDestroy() throws IOException {
		this.httpClient.close();
	}

	/**
	 * @see ILifeCycle#onInit()
	 */
	public void onInit() {
		this.httpClient = new HttpClient();		
	}

}
