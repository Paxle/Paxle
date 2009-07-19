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
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.util.Dictionary;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.data.db.ICommandDB;
import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.INonBlockingConnection;

/**
 * http://wiki.squid-cache.org/Features/Redirectors
 */
@Component(immediate=true, metatype=false)
@Service(IDataHandler.class)
@Property(name="type", value="UrlRedirectorHandler")
public class UrlRedirectorHandler implements IDataHandler {

	/**
	 * The paxle command-DB
	 */
	@Reference
	protected ICommandDB commandDB;
	
	/**
	 * The paxle reference normalizer
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY_UNARY)
	protected  IReferenceNormalizer referenceNormalizer;
	
	@Reference(target="(type=HttpTester)", cardinality=ReferenceCardinality.OPTIONAL_UNARY)
	protected IUrlTester httpTester;	
	
	@Reference(target="(type=BlacklistTester)", cardinality=ReferenceCardinality.OPTIONAL_UNARY)
	protected IUrlTester blacklistTester;
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Thread pool
	 */
	private ThreadPoolExecutor execService;	

	protected void activate(ComponentContext context) throws UnknownHostException, IOException {
		// getting the service properties
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> props = context.getProperties();
		
		// init this component
		this.activate(props);
	}
	
	protected void activate(Dictionary<String, Object> props) throws UnknownHostException, IOException {		
		// init thread-pool
		this.execService = new ThreadPoolExecutor(
				5,20,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
	}
	
	/**
	 * <code>URL ip/fqdn ident method key-pairs </code>
	 * or
	 * <code>ID URL ip/fqdn ident method key-pairs</code>
	 * e.g.
	 * http://www.paxle.net/en/start 192.168.10.204/- - GET
	 */
	public boolean onData(INonBlockingConnection nbc) throws IOException, BufferUnderflowException, MaxReadSizeExceededException {
		// reading data
		String inputLine = nbc.readStringByDelimiter("\r\n");
		
		// splitting data into parts
		String[] inputParts = inputLine.split("\\s");
				
		String reqID = null;
		String originalURL = null;		

		
		if (inputParts[0].startsWith("http")) {
			// the first element is the URL
			originalURL = inputParts[0];
		} else {
			// in concurrency-modus squid send's us a request-ID
			reqID = inputParts[0];
			
			// the URL
			originalURL = inputParts[1];
		}
		
		// sending a message back to the redirector script
		if (reqID != null) nbc.write(reqID + " ");
		nbc.write(originalURL + "\r\n");		
		
		// skipping too long URI
		if (originalURL.toString().length() > 512) {
			this.logger.debug("Skipping too long URL: " + originalURL);
			return true;
		}				

		// async. processing of the new URL
		this.execService.execute(new UrlHandlerJob(originalURL));
		
		return true;
	}
	
	class UrlHandlerJob implements Runnable {
		/**
		 * The HTTP URI that should be processed
		 */
		private String requestUri;
		
		public UrlHandlerJob(String url) {
			this.requestUri = url;
		}
		
		public void run() {
			// normalize URL
			final URI normalizedURL = referenceNormalizer.normalizeReference(this.requestUri);
					
			// blacklist testing
			if (blacklistTester != null) {
				boolean rejected = blacklistTester.reject(normalizedURL);
				if (rejected) {
					logger.info(String.format(
							"Rejecting URL '%s'. URL rejected by blacklist.",
							this.requestUri
					));	
					return;
				}
			}
			
			// testing http-status and mime-type of URI
			if (httpTester != null) {
				boolean rejected = httpTester.reject(normalizedURL);
				if (rejected) {
					logger.info(String.format(
							"Rejecting URL '%s'. URL rejected by http-tester.",
							this.requestUri
					));	
					return;
				}
			}
			
			// enqueue URL to command-DB
			boolean enqueued = commandDB.enqueue(normalizedURL, -1, 0);
			if (!enqueued) {
				logger.info(String.format(
						"Rejecting URL '%s'. URL already known to DB.",
						this.requestUri
				));		
			}
		}

	}
}