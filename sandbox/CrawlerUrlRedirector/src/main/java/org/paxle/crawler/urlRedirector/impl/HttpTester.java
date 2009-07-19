/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.crawler.urlRedirector.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Dictionary;

import org.apache.commons.httpclient.CircularRedirectException;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.paxle.crawler.ContentLengthLimitExceededException;

@Component(immediate=true, metatype=false)
@Service(IUrlTester.class)
@Property(name = IUrlTester.TYPE, value = "HttpTester")
public class HttpTester extends AUrlTester {
	
	/**
	 * http client class
	 */
	protected HttpClient httpClient;			
	
	/**
	 * Connection manager used for http connection pooling
	 */
	protected MultiThreadedHttpConnectionManager connectionManager;		
	
	protected void activate(ComponentContext context) throws UnknownHostException, IOException {
		// getting the service properties
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> props = context.getProperties();
		
		// init this component
		this.activate(props);
	}
	
	protected void activate(Dictionary<String, Object> props) throws UnknownHostException, IOException {
		// init http-client
		this.connectionManager = new MultiThreadedHttpConnectionManager();
		this.httpClient = new HttpClient(this.connectionManager);	
	}	
	
	public boolean reject(URI requestUri) {
		// doing a head request to determine the mime-type
		HeadMethod head = null;
		try {
			// trying to do a head request
			head = new HeadMethod(requestUri.toString());
			final int status = this.httpClient.executeMethod(head);
			
			// skipping not OK ressources
			if (status != HttpStatus.SC_OK) {
				logger.info(String.format(
						"Rejecting URL '%s'. Status-Code was: %s",
						requestUri,
						head.getStatusLine()
				));					
				return true;
			}
			
			// getting mime-type
			final String mimeType = this.getMimeType(head);
			
			// skipping images / css
			if (mimeType.startsWith("image/") || 
				mimeType.equalsIgnoreCase("text/css") || 
				mimeType.equalsIgnoreCase("text/javascript") ||
				mimeType.equalsIgnoreCase("application/x-javascript")
			) {
				logger.info(String.format(
						"Rejecting URL '%s'. Unsupported mime-type: %s",
						requestUri,
						mimeType
				));
				return true;
			}
			
			// URI seems to be ok
			return false;
		} catch (NoRouteToHostException e) {
			this.logger.warn(String.format("Rejecting URL %s: %s", requestUri, e.getMessage()));
		} catch (UnknownHostException e) {
			this.logger.warn(String.format("Rejecting URL %s: Unknown host.", requestUri));
		} catch (ConnectException e) {
			this.logger.warn(String.format("Rejecting URL %s: Unable to connect to host.", requestUri));
		} catch (ConnectTimeoutException e) {
			this.logger.warn(String.format("Rejecting URL %s: %s.", requestUri, e.getMessage()));
		} catch (SocketTimeoutException e) {
			this.logger.warn(String.format("Rejecting URL %s: Connection timeout.", requestUri));
		} catch (CircularRedirectException e) {
			this.logger.warn(String.format("Rejecting URL %s: %s", requestUri, e.getMessage()));
		} catch (NoHttpResponseException e) {
			this.logger.warn(String.format("Rejecting URL %s: %s", requestUri, e.getMessage()));
		} catch (ContentLengthLimitExceededException e) {
			this.logger.warn(String.format("Rejecting URL %s: %s", requestUri, e.getMessage()));
		} catch (Throwable e) {
			logger.error(String.format(
					"Rejecting URL '%s': ",
					requestUri,
					e.getMessage()
			),e);
		} finally {
			if (head != null) head.releaseConnection();
		}	
		
		return true;
	}

	
	private String getMimeType(HeadMethod head) {
		final Header contentTypeHeader = head.getResponseHeader("Content-Type");
		if (contentTypeHeader == null) return null;
		
		// separate MIME-type and charset from the content-type specification
		String contentMimeType = contentTypeHeader.getValue();
		
		int idx = contentMimeType.indexOf(";");
		if (idx != -1) {
			contentMimeType = contentMimeType.substring(0,idx);
		}
		
		return contentMimeType;
	}
}
