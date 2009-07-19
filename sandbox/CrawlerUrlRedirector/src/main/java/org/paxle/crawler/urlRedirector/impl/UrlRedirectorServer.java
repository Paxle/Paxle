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
import java.net.UnknownHostException;
import java.util.Dictionary;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.osgi.service.component.ComponentContext;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.IServer;
import org.xsocket.connection.Server;

@Component(metatype=true, immediate=true)
public class UrlRedirectorServer {
	/**
	 * The port to bind the server to
	 */
	@Property(name="Port", intValue=8090)
	static final String PORT = "org.paxle.crawler.urlRedirector.UrlRedirectorServer.port";
	
	/**
	 * The xSocket server
	 */
	protected IServer srv = null;
	
	@Reference(target="(type=UrlRedirectorHandler)", cardinality=ReferenceCardinality.MANDATORY_UNARY)
	protected IDataHandler urlDataHandler;
	
	protected void activate(ComponentContext context) throws UnknownHostException, IOException {
		// getting the service properties
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> props = context.getProperties();
		
		// init this component
		this.activate(props);
	}
	
	protected void activate(Dictionary<String, Object> props) throws UnknownHostException, IOException {
		Integer port = (Integer) props.get(PORT);
		if (port == null) port = Integer.valueOf(8090);
		
		// create server
		this.srv = new Server(port, this.urlDataHandler);
		
		// start it
		this.srv.start();
	}
	
	protected void deactivate(ComponentContext context) throws IOException {
		// shutdown the server
		this.srv.close();
		this.srv = null;
	}
}
