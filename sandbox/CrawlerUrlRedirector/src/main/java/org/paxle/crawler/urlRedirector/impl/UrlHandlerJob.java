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

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class UrlHandlerJob implements Runnable {
	/**
	 * 
	 */
	private final UrlRedirectorServer urlRedirectorServer;

	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * The HTTP URI that should be processed
	 */
	private final String requestUri;
	
	public UrlHandlerJob(UrlRedirectorServer urlRedirectorServer, String url) {
		this.urlRedirectorServer = urlRedirectorServer;
		this.requestUri = url;
	}
	
	public void run() {
		// normalize URL
		final URI normalizedURL = this.urlRedirectorServer.referenceNormalizer.normalizeReference(this.requestUri);
				
		// blacklist testing
		if (this.urlRedirectorServer.blacklistTester != null) {
			boolean rejected = this.urlRedirectorServer.blacklistTester.reject(normalizedURL);
			if (rejected) {
				logger.info(String.format(
						"Rejecting URL '%s'. URL rejected by blacklist.",
						this.requestUri
				));	
				return;
			}
		}
		
		// testing http-status and mime-type of URI
		if (this.urlRedirectorServer.httpTester != null) {
			boolean rejected = this.urlRedirectorServer.httpTester.reject(normalizedURL);
			if (rejected) {
				logger.info(String.format(
						"Rejecting URL '%s'. URL rejected by http-tester.",
						this.requestUri
				));	
				return;
			}
		}
		
		// enqueue URL to command-DB
		boolean enqueued = this.urlRedirectorServer.commandDB.enqueue(normalizedURL, -1, 0);
		if (!enqueued) {
			logger.info(String.format(
					"Rejecting URL '%s'. URL already known to DB.",
					this.requestUri
			));		
		}
	}

}