package org.paxle.crawler.proxy.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;
import org.paxle.core.prefs.Properties;
import org.paxle.crawler.proxy.IHttpProxy;
import org.xsocket.connection.ConnectionUtils;
import org.xsocket.connection.http.server.HttpServer;

public class Proxy implements ManagedService, IHttpProxy {
	/* =========================================================
	 * Config Properties
	 * ========================================================= */
	public static final String PROP_PROX_PORT = "proxyPort";

	/* =========================================================
	 * Preferences
	 * ========================================================= */
	public static final String PREF_PROFILE_ID = "profileID";
	
	/**
	 * Logger class
	 */
	private Log logger = LogFactory.getLog(this.getClass());	

	/**
	 * The proxy server implementation
	 */
	private HttpServer proxy = null;

	/**
	 * The properties of this component
	 */
	private Properties props = null;	
	
	private long commandProfileID = -1;
	
	public Proxy(Properties props) {
		// init with default configuration
		this.updated(this.getDefaults());
		
		if (props != null) {
			this.commandProfileID = Long.valueOf(props.getProperty(PREF_PROFILE_ID,"-1"));
		}
		
		if (commandProfileID == -1) {
			// TODO: create a new command-profile
		}
	}

	/**
	 * @return the default configuration of this service
	 */
	public Hashtable<String,Object> getDefaults() {
		Hashtable<String,Object> defaults = new Hashtable<String,Object>();

		defaults.put(PROP_PROX_PORT, Integer.valueOf(8081));		
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
			int port = ((Integer)configuration.get(PROP_PROX_PORT)).intValue();
			if (port <= 0) port = 8081;
			this.proxy = new HttpServer(port, new ProxyRequestHandler());

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
