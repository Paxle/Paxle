/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
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
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.crawler.urlRedirector.IUriTester;
import org.paxle.data.db.ICommandDB;
import org.xsocket.connection.IServer;
import org.xsocket.connection.Server;

@Component(metatype=true, immediate=true)
@Reference(
	name="uriTesters",
	referenceInterface=IUriTester.class,
	cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
	policy=ReferencePolicy.DYNAMIC,
	bind="addUriTester",
	unbind="removeUriTester",
	target="(IUrlTester.type=*)"
)
public class UrlRedirectorServer {
	/**
	 * The port to bind the server to
	 */
	@Property(name="Port", intValue=8090)
	static final String PORT = "org.paxle.crawler.urlRedirector.UrlRedirectorServer.port";
	
	/**
	 * For logging
	 */
	protected Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * The xSocket server
	 */
	protected IServer srv = null;
	
	/**
	 * Thread pool
	 */
	private ThreadPoolExecutor execService;	
	
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
	
	/**
	 * A list of all currently registered {@link IUriTester URI-testers}
	 */
	private final TreeSet<ServiceReference> uriTestersRefs = new TreeSet<ServiceReference>();	
	
	private ComponentContext ctx;
	
	protected void activate(ComponentContext context) throws UnknownHostException, IOException {
		this.ctx = context;
		
		// getting the service properties
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> props = context.getProperties();
		
		// init this component
		this.activate(props);
	}
	
	protected void activate(Dictionary<String, Object> props) throws UnknownHostException, IOException {
		Integer port = (Integer) props.get(PORT);
		if (port == null) port = Integer.valueOf(8090);
		
		// init thread-pool
		this.execService = new ThreadPoolExecutor(
				5,20,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()
        );		
		
		// create server
		this.srv = new Server(port, new UrlRedirectorHandler(this));
		
		// start it
		this.srv.start();
	}
	
	protected void deactivate(ComponentContext context) throws IOException {
		// shutdown the server
		this.srv.close();
		this.srv = null;
		
		// shutdown thread pool
		this.execService.shutdown();
	}
	
	protected void addUriTester(ServiceReference testerRef) {		
		this.uriTestersRefs.add(testerRef);
		
		final String testerType = (String) testerRef.getProperty(IUriTester.TYPE);
		this.logger.info(String.format(
				"URI-Tester with Type '%s' from bundle '%s' registered.",
				testerType,
				testerRef.getBundle().getSymbolicName()
		));
	}

	protected void removeUriTester(ServiceReference testerRef) {
		this.uriTestersRefs.remove(testerRef);
		
		final String testerType = (String) testerRef.getProperty(IUriTester.TYPE);
		this.logger.info(String.format(
				"URI-Tester with Type '%s' from bundle '%s' unregistered.",
				testerType,
				testerRef.getBundle().getSymbolicName()
		));
	}
	
	public List<IUriTester> getUriTesters() {
		final ArrayList<IUriTester> testers = new ArrayList<IUriTester>();
		
		for (ServiceReference testerRef : this.uriTestersRefs) {
			final String testerType = (String) testerRef.getProperty(IUriTester.TYPE);
			// TODO: check if tester was disabled			
			
			// getting a reference to the service
			final IUriTester tester = (IUriTester) this.ctx.locateService("uriTesters", testerRef);
			if (tester != null) testers.add(tester);
		}		
		
		return testers;
	}
	
	void process(String originalURL) {
		this.execService.execute(new UrlHandlerJob(this, originalURL));
	}
}
