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
import java.nio.BufferUnderflowException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.data.db.ICommandDB;
import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.INonBlockingConnection;

/**
 * http://wiki.squid-cache.org/Features/Redirectors
 */
public class UrlHandler implements IDataHandler {

	final ICommandDB commandDB;
	
	final IReferenceNormalizer refNormalizer;
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Connection manager used for http connection pooling
	 */
	private MultiThreadedHttpConnectionManager connectionManager = null;	
	
	/**
	 * http client class
	 */
	HttpClient httpClient = null;		
	
	/**
	 * Thread pool
	 */
	private ThreadPoolExecutor execService;	
	
	public UrlHandler(ICommandDB commandDB, IReferenceNormalizer refNormalizer) {
		this.commandDB = commandDB;
		this.refNormalizer = refNormalizer;
		
		// init http-client
		this.connectionManager = new MultiThreadedHttpConnectionManager();
		this.httpClient = new HttpClient(this.connectionManager);	
		
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
		URI url = null;
		
		if (inputParts[0].startsWith("http")) {
			// the first element is the URL
			url = this.refNormalizer.normalizeReference(inputParts[0]);
		} else {
			// in concurrency-modus squid send's us a request-ID
			reqID = inputParts[0];
			
			// the URL
			url = this.refNormalizer.normalizeReference(inputParts[1]);
		}
		
		// sending a message back to the redirector script
		if (reqID != null) nbc.write(reqID + " ");
		nbc.write(url + "\r\n");		
		
		// skipping too long URI
		if (url.toString().length() > 512) {
			this.logger.debug("Skipping too long URL: " + url);
			return true;
		}				

		// async. processing of the new URL
		this.execService.execute(new UrlHandlerJob(this, url));
		
		return true;
	}
}