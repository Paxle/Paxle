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
import java.nio.BufferUnderflowException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
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
	private final UrlRedirectorServer server;
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	public UrlRedirectorHandler(UrlRedirectorServer server) {
		this.server = server;
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
		this.server.process(originalURL);
		
		return true;
	}
}