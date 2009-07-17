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

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.CircularRedirectException;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.crawler.ContentLengthLimitExceededException;

class UrlHandlerJob implements Runnable {
	/**
	 * 
	 */
	private final UrlHandler urlHandler;

	private URI requestUri;
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	public UrlHandlerJob(UrlHandler urlHandler, URI url) {
		this.urlHandler = urlHandler;
		this.requestUri = url;
	}
	
	public void run() {
		// TODO: doing a head request to guess the mime-type
		HeadMethod head = null;
		try {
			// trying to do a head request
			head = new HeadMethod(this.requestUri.toString());
			final int status = this.urlHandler.httpClient.executeMethod(head);
			
			// skipping not OK ressources
			if (status != HttpStatus.SC_OK) {
				logger.info(String.format(
						"Rejecting URL '%s'. Status-Code was: %s",
						this.requestUri,
						head.getStatusLine()
				));					
				return;
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
						this.requestUri,
						mimeType
				));
				return;
			}
			
			// TODO: skipping unsupported mime-types
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
					this.requestUri,
					e.getMessage()
			),e);
		} finally {
			if (head != null) head.releaseConnection();
		}				
			
		
		// enqueue URL to command-DB
		boolean enqueued = this.urlHandler.commandDB.enqueue(requestUri, -1, 0);
		if (!enqueued) {
			logger.info(String.format(
					"Rejecting URL '%s'. URL already known to DB.",
					this.requestUri
			));		
		}
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