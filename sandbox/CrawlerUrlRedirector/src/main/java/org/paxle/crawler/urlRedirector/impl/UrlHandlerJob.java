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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.crawler.urlRedirector.IUriTester;

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
				
		// test URI against all testers
		final List<IUriTester> uriTesters = this.urlRedirectorServer.getUriTesters();
		for (IUriTester tester : uriTesters) {
			boolean rejected = tester.reject(normalizedURL);
			if (rejected) {
				logger.info(String.format(
						"Rejecting URL '%s'. URL rejected by tester '%s'.",
						this.requestUri,
						tester.getClass().getName()
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