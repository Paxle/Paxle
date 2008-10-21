/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.crawler.proxy.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.paxle.crawler.proxy.IHttpProxy;
import org.xsocket.connection.ConnectionUtils;
import org.xsocket.connection.http.server.HttpServer;

public class Proxy implements ManagedService, IHttpProxy {
	/* =========================================================
	 * Config Properties
	 * ========================================================= */
	private static final String PROP_PROXY_PORT = "proxyPort";
	private static final String PROP_ENABLE_PROXY_AUTH = "enableProxyAuthentication";
	private static final String PROP_TRANSFER_LIMIT = "transferLimit";
	
	/**
	 * Logger class
	 */
	private Log logger = LogFactory.getLog(this.getClass());	

	/**
	 * The proxy server implementation
	 */
	private HttpServer proxy = null;
	
	/**
	 * OSGi Service to tracke the {@link UserAdmin} service.
	 */
	private ServiceTracker userAgentTracker = null;	
	
	public Proxy(ServiceTracker userAgentTracker) {
		if (userAgentTracker == null) throw new NullPointerException("The user-agent-tracker must not be null.");		
		this.userAgentTracker = userAgentTracker;		
		
		// init with default configuration
		this.updated(this.getDefaults());
	}

	/**
	 * @return the default configuration of this service
	 */
	public Hashtable<String,Object> getDefaults() {
		Hashtable<String,Object> defaults = new Hashtable<String,Object>();

		defaults.put(PROP_ENABLE_PROXY_AUTH, Boolean.TRUE);
		defaults.put(PROP_TRANSFER_LIMIT, Integer.valueOf(-1));
		defaults.put(PROP_PROXY_PORT, Integer.valueOf(8081));		
		defaults.put(Constants.SERVICE_PID, IHttpProxy.class.getName());

		return defaults;
	}	

	/**
	 * @see ManagedService#updated(Dictionary)
	 */
	@SuppressWarnings("unchecked")		// we're only implementing an interface
	public synchronized void updated(Dictionary configuration) {
		// our caller catches all runtime-exceptions and silently ignores them, leaving us confused behind,
		// so this try/catch-block exists for debugging purposes
		try {
			if ( configuration == null ) {
				this.logger.warn("updated configuration is null");
				/*
				 * Generate default configuration
				 */
				configuration = this.getDefaults();
			}


			// cleanup old
			this.terminate();

			// init new
			int port = ((Integer)configuration.get(PROP_PROXY_PORT)).intValue();
			if (port <= 0) port = 8081;
			this.proxy = new HttpServer(port, new ProxyRequestHandler(
					this.userAgentTracker,
					(Boolean)configuration.get(PROP_ENABLE_PROXY_AUTH)
			));

			Integer transferLimit = (Integer) configuration.get(PROP_TRANSFER_LIMIT);
			if (transferLimit != null && transferLimit.intValue() > 0) {
				this.proxy.setWriteTransferRate(transferLimit.intValue() * 1024);
			}
			
			// start it
			ConnectionUtils.start(proxy);
		} catch (Throwable e) {
			this.logger.error("Internal exception during configuring", e);
		}
	}

	public void terminate() {
		if (this.proxy != null) {
			this.proxy.close();
			this.proxy = null;
		}
	}
}
