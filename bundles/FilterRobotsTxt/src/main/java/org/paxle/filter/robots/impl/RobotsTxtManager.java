package org.paxle.filter.robots.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.httpclient.CircularRedirectException;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;
import org.paxle.filter.robots.IRobotsTxtManager;
import org.paxle.filter.robots.impl.rules.AllowRule;
import org.paxle.filter.robots.impl.rules.DisallowRule;
import org.paxle.filter.robots.impl.rules.RobotsTxt;
import org.paxle.filter.robots.impl.rules.RuleBlock;

public class RobotsTxtManager implements IRobotsTxtManager, ManagedService {
	/* =========================================================
	 * Config Properties
	 * ========================================================= */
	private static final String PROP_CONNECTION_TIMEOUT = "connectionTimeout";
	private static final String PROP_SOCKET_TIMEOUT = "socketTimeout";
	private static final String PROP_MAXCONNECTIONS_TOTAL = "maxConnectionsTotal";
	private static final String PROP_MAX_CACHE_SIZE = "maxCacheSize";
	
	private static final String PROP_PROXY_USE = "useProxy";
	private static final String PROP_PROXY_HOST = "proxyHost";
	private static final String PROP_PROXY_PORT = "proxyPort";
	private static final String PROP_PROXY_USER = "proxyUser";
	private static final String PROP_PROXY_PASSWORD = "proxyPassword";	
		
	private static final String ROBOTS_AGENT_PAXLE = "paxle";

	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());

	/**
	 * The cachemanager to use
	 */
	private CacheManager manager = null;

	/**
	 * A cach to hold {@link RobotsTxt} objects in memory
	 */
	private Cache cache = null;

	/**
	 * Path where {@link RobotsTxt} objects should be stored
	 */
	private File path = null;
	
	/**
	 * Connection manager used for http connection pooling
	 */
	private MultiThreadedHttpConnectionManager connectionManager = null;

	/**
	 * http client class
	 */
	private HttpClient httpClient = null;	
	
	private boolean useAsyncExecution = true;
	
	private ExecutorService execService;
	
	/**
	 * @param path the path where the {@link RobotsTxt} objects should be stored
	 */
	public RobotsTxtManager(File path) {
		// configure path where serialized robots-txt objects should be stored
		this.path = path;
		if (!this.path.exists()) this.path.mkdirs();

		// configure caching manager
		this.manager = new CacheManager();
		
		// configure with default configuration
		this.updated(this.getDefaults());
				
		// init threadpool
		// XXX should we set the thread-pool size? 
		this.execService = Executors.newCachedThreadPool();					
	}
	
	public void terminate() {
		// clear cache
		this.manager.clearAll();

		// unregiser cache
		this.manager.removalAll();

		// shutdown cache manager
		this.manager.shutdown();
		
		// cleanup http-client
		if (this.connectionManager != null) {
			this.connectionManager.shutdown();
			this.connectionManager = null;
			this.httpClient = null;
		}		
		
		// shutdown exec-service
		// XXX maybe we should use shutdownNow here?
		this.execService.shutdown();
	}

	/**
	 * This method is synchronized with {@link #updated(Dictionary)} to avoid
	 * problems during configuration update.
	 * 
	 * @return the {@link Cache} to use
	 */
	synchronized Cache getCache() {
		return this.cache;
	}
	

	/**
	 * This method is synchronized with {@link #updated(Dictionary)} to avoid
	 * problems during configuration update.
	 * 
	 * @return the {@link HttpClient} to use
	 */
	private synchronized HttpClient getHttpClient() {
		return this.httpClient;
	}	
	
	/**
	 * @return the default configuration of this service
	 */
	public Hashtable<String,Object> getDefaults() {
		Hashtable<String,Object> defaults = new Hashtable<String,Object>();
		
		// default connection manager properties
		defaults.put(PROP_MAXCONNECTIONS_TOTAL, Integer.valueOf(MultiThreadedHttpConnectionManager.DEFAULT_MAX_TOTAL_CONNECTIONS));
		defaults.put(PROP_CONNECTION_TIMEOUT, Integer.valueOf(15000));
		defaults.put(PROP_SOCKET_TIMEOUT, Integer.valueOf(15000));
		
		// default cache props
		defaults.put(PROP_MAX_CACHE_SIZE, Integer.valueOf(1000));
		
		// default proxy settings
		defaults.put(PROP_PROXY_USE, Boolean.FALSE);
		defaults.put(PROP_PROXY_HOST, "");
		defaults.put(PROP_PROXY_PORT, Integer.valueOf(3128));		// default squid port
		defaults.put(PROP_PROXY_USER, "");
		defaults.put(PROP_PROXY_PASSWORD, "");
		
		defaults.put(Constants.SERVICE_PID, IRobotsTxtManager.class.getName());
		
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
				logger.warn("updated configuration is null");
				/*
				 * Generate default configuration
				 */
				configuration = this.getDefaults();
			}
			
			// remove old cache
			this.manager.removeCache("robotsTxtCache");
			this.cache = null;
			
			// init a new cache 
			this.cache = new Cache("robotsTxtCache", ((Integer) configuration.get(PROP_MAX_CACHE_SIZE)).intValue(), false, false, 60*60, 30*60);
			this.manager.addCache(this.cache);
			
			// shutdown old connection-manager
			this.connectionManager.shutdown();
			this.connectionManager = null;
			
			// init http-client
			this.connectionManager = new MultiThreadedHttpConnectionManager();
			this.connectionManager.getParams().setConnectionTimeout(((Integer) configuration.get(PROP_CONNECTION_TIMEOUT)).intValue());
			this.connectionManager.getParams().setSoTimeout(((Integer) configuration.get(PROP_SOCKET_TIMEOUT)).intValue());
			this.connectionManager.getParams().setMaxTotalConnections(((Integer) configuration.get(PROP_MAXCONNECTIONS_TOTAL)).intValue());
			this.httpClient = new HttpClient(this.connectionManager);
			
			// proxy configuration
			final boolean useProxy = ((Boolean)configuration.get(PROP_PROXY_USE)).booleanValue();
			final String host = (String)configuration.get(PROP_PROXY_HOST);
			final int port = ((Integer)configuration.get(PROP_PROXY_PORT)).intValue();
			
			if (useProxy && host.length() > 0) {
				this.logger.info(String.format("Proxy is enabled: %s:%d",host,port));
				final ProxyHost proxyHost = new ProxyHost(host, port);
				this.httpClient.getHostConfiguration().setProxyHost(proxyHost);
				
				final String user = (String)configuration.get(PROP_PROXY_HOST);
				final String pwd = (String)configuration.get(PROP_PROXY_PASSWORD);
				
				if (user.length() > 0 && pwd.length() > 0)
					this.httpClient.getState().setProxyCredentials(
							new AuthScope(host, port),
							new UsernamePasswordCredentials(user, pwd)
					);
			} else {
				this.logger.info("Proxy is disabled");
				this.httpClient.getHostConfiguration().setProxyHost(null);
				this.httpClient.getState().clearCredentials();
			}			
			
		} catch (Throwable e) {
			logger.error("Internal exception during configuring", e);
		}			
	}

	/**
	 * @param location the URL
	 * @return the hostname:port part of the location
	 */
	private String getHostPort(URI location) {
		String prot = location.getScheme();
		String host = location.getHost();
		int port = location.getPort();

		if (prot.equals("http") && port == -1) port = 80;
		if (prot.equals("https") && port == -1) port = 443;

		return String.format("%s:%d",host,port);
	}
	
	/**
	 * Downloads a <i>robots.txt</i> file from the given url and parses it
	 * @param robotsUrlStr the URL to the robots.txt. This must be a http(s) resource
	 * @return the parsed robots.txt file as a {@link RobotsTxt}-object
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	RobotsTxt parseRobotsTxt(URI robotsURL) throws IOException, URISyntaxException {
		String hostPort = this.getHostPort(robotsURL);

		String statusLine = null;
		if (!robotsURL.getScheme().startsWith("http")) {
			throw new IOException(String.format("Unsupported protocol: %s", robotsURL.getScheme()));
		}

		InputStream inputStream = null;
		HttpMethod getMethod = null;
		try {
			getMethod = new GetMethod(robotsURL.toASCIIString());

			int code = this.getHttpClient().executeMethod(getMethod);
			statusLine = getMethod.getStatusLine().toString();

			if (code == HttpStatus.SC_UNAUTHORIZED || code == HttpStatus.SC_FORBIDDEN) {
				// access to the whole website is restricted
				return new RobotsTxt(hostPort,RobotsTxt.RELOAD_INTERVAL_DEFAULT, statusLine, true);
			} else if (code == HttpStatus.SC_NOT_FOUND) {
				// no robots.txt provided
				return new RobotsTxt(hostPort,RobotsTxt.RELOAD_INTERVAL_DEFAULT, statusLine);
			} else if (code != HttpStatus.SC_OK) {
				// the robots.txt seems not to be deliverable
				return new RobotsTxt(hostPort,RobotsTxt.RELOAD_INTERVAL_DEFAULT, statusLine);
			}

			Header contentTypeHeader = getMethod.getResponseHeader("Content-Type");
			if (contentTypeHeader != null && !contentTypeHeader.getValue().startsWith("text/plain")) {
				// the robots.txt seems not to be available
				return new RobotsTxt(hostPort, RobotsTxt.RELOAD_INTERVAL_ERROR, "Wrong mimeType " + contentTypeHeader.getValue());
			}
			
			inputStream = getMethod.getResponseBodyAsStream();
			RobotsTxt robotsTxt = new RobotsTxt(hostPort, RobotsTxt.RELOAD_INTERVAL_DEFAULT, statusLine);
			return this.parseRobotsTxt(robotsTxt, inputStream);
		} catch (IOException e) {
			long reloadInterval = RobotsTxt.RELOAD_INTERVAL_TEMP_ERROR;
			String status = e.getMessage();
			if (e instanceof UnknownHostException) {
				reloadInterval = RobotsTxt.RELOAD_INTERVAL_ERROR;
				status = "Unknown host";
			} else if (e instanceof CircularRedirectException) {
				reloadInterval = RobotsTxt.RELOAD_INTERVAL_ERROR;
				logger.debug(String.format("Invalid redirection on host '%s'.",hostPort));
			} else if (e instanceof SocketTimeoutException || e instanceof ConnectTimeoutException) {
				logger.debug(String.format("TimeOut while loading robots.txt from host '%s'.",hostPort));
			} else if (!(
					e instanceof ConnectException ||
					e instanceof SocketException
			)) {
				logger.error("Exception while loading robots.txt from " + hostPort, e);
			}

			return new RobotsTxt(hostPort, reloadInterval, status);
		} finally {
			if (inputStream != null) try { inputStream.close(); } catch (Exception e) {/* ignore this */}
			if (getMethod != null) getMethod.releaseConnection();			
		}

	}

	/**
	 * Reads a <i>robots.txt</i> from stream and stores all parsed properties into a {@link RobotsTxt} object.
	 * @param robotsTxt the {@link RobotsTxt} object that should be filled with the parsed properties
	 * @param inputStream the {@link InputStream} to read the data 
	 * @return the {@link RobotsTxt} object. 
	 * @throws IOException
	 */
	RobotsTxt parseRobotsTxt(RobotsTxt robotsTxt, InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));		
		RuleBlock currentBlock = null;

		boolean isInRuleBlock = false;
		String line = null;
		while ((line = reader.readLine()) != null) {
			// trim and cut of comments
			line = line.trim();
			line = this.cutOfComments(line);

			// ignore empty lines
			if (line.length() == 0) continue;

			if (line.toLowerCase().startsWith("User-agent:".toLowerCase())) {

				// getting the user-agent
				line = line.substring("User-agent:".length()).trim();
				if (line.length() > 0) {
					if (isInRuleBlock || currentBlock == null) {
						// the start of a new block was detected
						isInRuleBlock = false;
						currentBlock = new RuleBlock();
						robotsTxt.addRuleBlock(currentBlock);
					}
					currentBlock.addAgent(line);
				}
			} else if (line.toLowerCase().startsWith("Disallow:".toLowerCase())) {
				if (currentBlock == null) continue;
				isInRuleBlock = true;

				// getting the user-agent
				line = line.substring("Disallow:".length()).trim();

				if (line.length() > 0) {
					try {
						line = URLDecoder.decode(line,"UTF-8");
					} catch (Exception e) {/* ignore this */}

					currentBlock.addRule(new DisallowRule(line));
				}
			} else if (line.toLowerCase().startsWith("Allow:".toLowerCase())) {
				if (currentBlock == null) continue;
				isInRuleBlock = true;

				// getting the user-agent
				line = line.substring("Allow:".length()).trim();

				if (line.length() > 0) {
					try {
						line = URLDecoder.decode(line,"UTF-8");
					} catch (Exception e) {/* ignore this */}

					currentBlock.addRule(new AllowRule(line));
				}
			}

		}

		return robotsTxt;
	}

	private String cutOfComments(String line) {
		int pos = line.indexOf("#");
		if (pos != -1) line = line.substring(0,pos).trim();
		return line;
	}
	
	/**
	 * Check weather the specified {@link URI} is blocked by the robots.txt of the server hosting the {@link URI}
	 * @return <code>true</code> if crawling of the {@link URI} is disallowed
	 */
	public boolean isDisallowed(String location) {
		return isDisallowed(URI.create(location));		// XXX please re-check whether non-escaped hosts (i.e. "umlaut-domains") work
	}

	/**
	 * Check weather the specified {@link URI} is blocked by the robots.txt of the server hosting the {@link URI}
	 * @return <code>true</code> if crawling of the {@link URI} is disallowed
	 */
	public boolean isDisallowed(URI location) {
		String protocol = location.getScheme();
		if (!protocol.startsWith("http")) {
			this.logger.debug(String.format("Protocol of location '%s' not supported", location));
			return false;
		}

		// check if the URI is blocked
		return this.isDisallowed(new ArrayList<URI>(Arrays.asList(new URI[]{location}))).size() == 1;
	}
	
	/**
	 * Check a list of {@link URI URI} against the robots.txt file of the servers hosting the {@link URI}.
	 * @param hostPort the web-server hosting the {@link URI URIs}
	 * @param urlList a list of {@link URI}
	 * 
	 * @return all {@link URI} that are blocked by the servers
	 */
	public List<URI> isDisallowed(Collection<URI> urlList) {
		if (urlList == null) throw new NullPointerException("The URI-list is null.");
		
		// group the URL list based on hostname:port
		HashMap<URI, List<URI>> uriBlocks = this.groupURI(urlList);
		ArrayList<URI> disallowedURI = new ArrayList<URI>();

		if (this.useAsyncExecution) {
			/*
			 * Asynchronous execution and parallel check of all blocks 
			 */
			final CompletionService<Collection<URI>> execCompletionService = new ExecutorCompletionService<Collection<URI>>(this.execService);
			
			// loop through the blocks and start a worker for each block
			for (Entry<URI, List<URI>> uriBlock : uriBlocks.entrySet()) {
				URI baseUri = uriBlock.getKey();
				List<URI> uriList = uriBlock.getValue();			
				execCompletionService.submit(new RobotsTxtManagerCallable(baseUri,uriList));
			}
			
			// wait for the worker-threads to finish execution
			for (int i = 0; i < uriBlocks.size(); ++i) {
				try {
					Collection<URI> disallowedInGroup = execCompletionService.take().get();
					if (disallowedInGroup != null) {
						disallowedURI.addAll(disallowedInGroup);
					}
				} catch (InterruptedException e) {
					this.logger.info(String.format("Interruption detected while waiting for robots.txt-check result."));
					// XXX should we break here?
				} catch (ExecutionException e) {
					this.logger.error(String.format("Unexpected '%s' while performing robots.txt check.",
							e.getClass().getName()
					),e);
				}
			}
			
		} else {
			/*
			 * Synchronous execution and sequential check of all blocks 
			 */
			for (Entry<URI, List<URI>> uriBlock : uriBlocks.entrySet()) {
				URI baseUri = uriBlock.getKey();
				List<URI> uriList = uriBlock.getValue();

				// check the block
				Collection<URI> disallowedInGroup = this.isDisallowed(baseUri, uriList);
				if (disallowedInGroup != null) {
					disallowedURI.addAll(disallowedInGroup);
				}
			}
		}
		
		return disallowedURI;
	}
	
	/**
	 * Check a list of {@link URI URI} against the robots.txt file of the specified server.
	 * @param baseUri the base-URI of the server hosting the {@link URI URIs}
	 * @param uriList a list of {@link URI}. Each {@link URI} must belong to the specified <code>hostname:port</code>
	 * 
	 * @return all {@link URI} that are blocked by the specified server
	 */
	private Collection<URI> isDisallowed(URI baseUri , Collection<URI> uriList) {
		if (baseUri == null) throw new NullPointerException("The base-URI is null.");
		if (uriList == null) throw new NullPointerException("The URI-list is null.");
		if (!baseUri.getScheme().startsWith("http")) throw new IllegalArgumentException("Only http(s) resources are allowed.");
		if (baseUri.getPath().length() > 1) throw new IllegalArgumentException("Invalid base-URI.");
		
		ArrayList<URI> disallowedList = new ArrayList<URI>();
		
		// getting the RobotsTxt-info for this host[:port]
		RobotsTxt robotsTxt = null;
		try {
			Cache theCache = this.getCache();
			
			String hostPort = this.getHostPort(baseUri);			
			synchronized (hostPort.intern()) {
				// trying to get the robots.txt from cache
				Element element = theCache.get(hostPort);
				if (element != null) robotsTxt = (RobotsTxt) element.getValue();

				// trying to get the robots.txt from file
				if (robotsTxt == null) {
					robotsTxt = this.loadRobotsTxt(hostPort);
					if (robotsTxt != null) {
						element = new Element(hostPort, robotsTxt);
						theCache.put(element);
					}
				}

				// trying to download the robots.txt
				if (robotsTxt == null || (System.currentTimeMillis() - robotsTxt.getLoadedDate().getTime() > robotsTxt.getReloadInterval())) {				
					robotsTxt = this.parseRobotsTxt(URI.create(baseUri.toASCIIString() + "/robots.txt"));
					element = new Element(hostPort, robotsTxt);
					theCache.put(element);
					this.storeRobotsTxt(robotsTxt);
				}
			}
			
			// loop through each URI and check against the robots.txt
			for (URI location : uriList) {
				assert(this.getHostPort(location).equals(hostPort));
				String path = location.getPath();
				
				if (robotsTxt.isDisallowed(ROBOTS_AGENT_PAXLE, path)) {
					disallowedList.add(location);
				}
			}
		} catch (Exception e) {
			this.logger.error(String.format(
					"Unexpected '%s' while checking URIs against the robots.txt hosted on '%s'.",
					e.getClass().getName(),
					baseUri.toASCIIString()
			),e);
		} 
		
		return disallowedList;
	}

	void storeRobotsTxt(RobotsTxt robotsTxt) {
		ObjectOutputStream oos = null;
		try {
			// getting the host:port string
			String hostPort = robotsTxt.getHostPort();
			hostPort = hostPort.replace(':', '_');

			// creating a file
			File robotsTxtFile = new File(this.path,hostPort);
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(robotsTxtFile)));
			oos.writeObject(robotsTxt);
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oos != null) try { oos.close(); } catch (Exception e) {/* ingore this */}
		}
	}

	/**
	 * Loads a robots.txt from HDD
	 * @param hostPort
	 * @return
	 */
	RobotsTxt loadRobotsTxt(String hostPort) {
		ObjectInputStream ois = null;
		File robotsTxtFile = null;
		try {
			// getting the host:port string
			hostPort = hostPort.replace(':', '_');			

			// getting the file
			robotsTxtFile = new File(this.path,hostPort);
			if (!robotsTxtFile.exists()) return null;

			// loading object from file
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(robotsTxtFile)));
			RobotsTxt robotsTxt = (RobotsTxt) ois.readObject();
			return robotsTxt;

		} catch (InvalidClassException e) {
			// just ignore this, class format has changed
			if (robotsTxtFile != null && robotsTxtFile.exists()) robotsTxtFile.delete();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (ois != null) try { ois.close(); } catch (Exception e) {/* ingore this */}
		}
	}
	
	/**
	 * Groups a list of {@link URI} into blocks hosted by a single server. 
	 * {@link URI} which are not accessible via <code>http(s)</code> are ignored.
	 * 
	 * @param urlList the {@link URI} list
	 * @return a map containing the base-URI <code>http(s)://hostname:port</code> as key and the list of {@link URI}
	 *         hosted by the host as values.
	 */
	private HashMap<URI, List<URI>> groupURI(Collection<URI> urlList) {
		HashMap<URI, List<URI>> group = new HashMap<URI, List<URI>>();
		
		for (URI uri : urlList) {
			if (!uri.getScheme().startsWith("http")) continue;
			
			// getting hostname:port
			String hostPort = this.getHostPort(uri);
			URI baseURI = URI.create(uri.getScheme() + "://" + hostPort);
			
			List<URI> pathList = null;
			if (!group.containsKey(baseURI)) {
				pathList = new ArrayList<URI>();
				group.put(baseURI, pathList);
			} else {
				pathList = group.get(baseURI);
			}
			pathList.add(uri);			
		}
		
		return group;
	}
	
	/**
	 * Callable class for async. execution of {@link RobotsTxtManager#isDisallowed(URI, Collection)} check.
	 */
	class RobotsTxtManagerCallable implements Callable<Collection<URI>> {		
		private URI baseUri = null;
		private List<URI> uriList = null;
		
		public RobotsTxtManagerCallable(URI baseUri, List<URI> uriList) {
			if (baseUri == null) throw new NullPointerException("The base-URI is null.");
			if (uriList == null) throw new NullPointerException("The URI-list is null.");
			this.baseUri = baseUri;
			this.uriList = uriList;
		}

		@SuppressWarnings("unchecked")
		public Collection<URI> call() throws Exception {
			try {
				long start = System.currentTimeMillis(); 
				Collection<URI> disallowedList = isDisallowed(this.baseUri, this.uriList);
				long end = System.currentTimeMillis();

				logger.debug(String.format(
						"Robots.txt check of %d URI hosted on '%s' took %d ms. Access to %d URI disallowed.",
						this.uriList.size(),
						this.baseUri.toASCIIString(),
						end-start,
						(disallowedList==null)?0:disallowedList.size()
				));

				return disallowedList;
			} catch (Exception e) {
				logger.error(String.format("Unexpected '%s' while performing robots.txt check agains '%s'.",
						e.getClass().getName(),
						this.baseUri.toASCIIString()
				),e);
				return Collections.EMPTY_LIST;
			}
		}

	}	
}