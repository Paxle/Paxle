package org.paxle.crawler.proxy.impl;

import java.io.IOException;
import java.net.ConnectException;

import org.xsocket.Execution;
import org.xsocket.ILifeCycle;
import org.xsocket.connection.http.HttpRequest;
import org.xsocket.connection.http.client.HttpClient;
import org.xsocket.connection.http.server.IHttpRequestHandler;
import org.xsocket.connection.http.server.IHttpResponseContext;

public class ProxyRequestHandler implements IHttpRequestHandler, ILifeCycle {

	private HttpClient httpClient = null;	
	
	/**
	 * @see IHttpRequestHandler#onRequest(HttpRequest, IHttpResponseContext)
	 */
	@Execution(Execution.MULTITHREADED)
	public void onRequest(HttpRequest req, IHttpResponseContext responseCtx) throws IOException {
	      // remove the named hop-by-hop headers
	      req.removeHopByHopHeaders("CONNECTION", "PROXY-AUTHORIZATION",
	                                "TRAILER", "UPGRADE");

	      // remove accept encoding for now
	      req.removeHeader("Accept-Encoding");	      
	      
	      // reset address (Host header will be update automatically)
//	      req.updateTargetURI(host, port);

	      try {
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
