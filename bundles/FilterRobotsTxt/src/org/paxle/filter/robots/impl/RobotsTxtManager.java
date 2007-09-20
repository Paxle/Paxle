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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cache.Cache;
import org.apache.commons.cache.EvictionPolicy;
import org.apache.commons.cache.GroupMap;
import org.apache.commons.cache.LRUEvictionPolicy;
import org.apache.commons.cache.MemoryStash;
import org.apache.commons.cache.SimpleCache;
import org.apache.commons.cache.StashPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RobotsTxtManager {
	private static final int CACHE_SIZE = 1000;
	
	/**
	 * Regular expression pattern to extract the hostname:port portion of an URL
	 */
	private static final Pattern HOSTPORT_PATTERN = Pattern.compile("([^:]+)://([^/\\?#]+)(?=/|$|\\?|#)");
	
	/**
	 * Regular expression to extract the path of an URL
	 */
	private static final Pattern PATH_PATTERN = Pattern.compile("((?<!:/)/(?!/).*$)");

	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A cach to hold {@link RobotsTxt} objects in memory
	 */
	private Cache cache = null;
	
	/**
	 * Path where {@link RobotsTxt} objects should be stored
	 */
	private File path = null;

	/**
	 * @param path the path where the {@link RobotsTxt} objects should be stored
	 */
	public RobotsTxtManager(File path) {
		LRUEvictionPolicy ep = new LRUEvictionPolicy();
		this.cache = new SimpleCache(new MemoryStash(CACHE_SIZE), (EvictionPolicy)ep, (StashPolicy)null, (GroupMap)null, (File)null);
		this.path = path;
		if (!this.path.exists()) this.path.mkdirs();
	}

	/**
	 * @param location the URL
	 * @return the hostname:port part of the location
	 */
	private String getHostPort(String location) {
		// getting the host:port string
		Matcher matcher = HOSTPORT_PATTERN.matcher(location);
		if (!matcher.find()) return null;

		String hostPort = matcher.group(2);
		if (hostPort.endsWith(":-1")) hostPort = hostPort.substring(0,hostPort.length()-":-1".length());

		String prot = matcher.group(1);
		if (prot.equals("http") && hostPort.endsWith(":80")) hostPort = hostPort.substring(0,hostPort.length()-":80".length());
		if (prot.equals("https") && hostPort.endsWith(":443")) hostPort = hostPort.substring(0,hostPort.length()-":443".length());

		return hostPort.intern();
	}

	/**
	 * @param location the URL
	 * @return the path of the location
	 */
	private String getPath(String location) {
		Matcher matcher = PATH_PATTERN.matcher(location);
		return (!matcher.find())? "" : matcher.group(1);
	}

	/**
	 * Downloads a <i>robots.txt</i> file from the given url and parses it
	 * @param robotsUrlStr the URL to the robots.txt
	 * @return the parsed robots.txt file as a {@link RobotsTxt}-object
	 * @throws IOException
	 */
	private RobotsTxt parseRobotsTxt(String robotsUrlStr) throws IOException {
		String hostPort = this.getHostPort(robotsUrlStr);
		URL robotsURL = new URL(robotsUrlStr);

		String statusLine = null;
		URLConnection connection = robotsURL.openConnection();
		if ( connection instanceof HttpURLConnection) {
			HttpURLConnection httpConnection = (HttpURLConnection) connection;

			try {
				int code = httpConnection.getResponseCode();
				statusLine = httpConnection.getHeaderField(0);

				if (code == 401 || code == 403) {
					// access to the whole website is restricted
					return new RobotsTxt(hostPort,RobotsTxt.RELOAD_INTERVAL_DEFAULT, statusLine, true);
				} else if (code == 404) {
					// no robots.txt provided
					return new RobotsTxt(hostPort,RobotsTxt.RELOAD_INTERVAL_DEFAULT, statusLine);
				} else if (code != 200) {
					// the robots.txt seems not to be deliverable
					return new RobotsTxt(hostPort,RobotsTxt.RELOAD_INTERVAL_DEFAULT, statusLine);
				}

				String mimeType = httpConnection.getContentType();
				if (mimeType != null && !mimeType.startsWith("text/plain")) {
					// the robots.txt seems not to be available
					return new RobotsTxt(hostPort, RobotsTxt.RELOAD_INTERVAL_ERROR, "Wrong mimeType " + mimeType);
				}
			} catch (UnknownHostException e) {
				// host is unknown, create a dummy robots.txt
				return new RobotsTxt(hostPort, RobotsTxt.RELOAD_INTERVAL_ERROR, "Unknown host");
			}
		}

		InputStream inputStream = null;
		try {
			RobotsTxt robotsTxt = new RobotsTxt(hostPort, RobotsTxt.RELOAD_INTERVAL_DEFAULT, statusLine);
			return this.parseRobotsTxt(robotsTxt, connection.getInputStream());
		} finally {
			if (inputStream != null) try { inputStream.close(); } catch (Exception e) {/* ignore this */}
		}

	}

	/**
	 * Reads a <i>robots.txt</i> from stream and stores all parsed properties into a {@link RobotsTxt} object.
	 * @param robotsTxt the {@link RobotsTxt} object that should be filled with the parsed properties
	 * @param inputStream the {@link InputStream} to read the data 
	 * @return the {@link RobotsTxt} object. 
	 * @throws IOException
	 */
	private RobotsTxt parseRobotsTxt(RobotsTxt robotsTxt, InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));		
		RuleBlock currentBlock = null;

		boolean inRuleBlock = false;
		String line = null;
		while ((line = reader.readLine()) != null) {
			// trim and cutof comments
			line = line.trim();
			line = this.cutOfComments(line);
			
			// ignore empty lines
			if (line.length() == 0) continue;

			if (line.toLowerCase().startsWith("User-agent:".toLowerCase())) {
				if (inRuleBlock || currentBlock == null) {
					// the start of a new block ws detected
					inRuleBlock = false;
					currentBlock = new RuleBlock();
					robotsTxt.addRuleBlock(currentBlock);
				}

				// getting the user-agnet
				line = line.substring("User-agent:".length()).trim();
				currentBlock.addAgent(line);				
			} else if (line.toLowerCase().startsWith("Disallow:".toLowerCase())) {
				if (currentBlock == null) continue;
				inRuleBlock = true;

				// getting the user-agnet
				line = line.substring("Disallow:".length()).trim();

				if (line.length() > 0) {
					try {
						line = URLDecoder.decode(line,"UTF-8");
					} catch (Exception e) {/* ignore this */}

					currentBlock.addRule(new DisallowRule(line));
				}
			} else if (line.toLowerCase().startsWith("Allow:".toLowerCase())) {
				if (currentBlock == null) continue;
				inRuleBlock = true;

				// getting the user-agnet
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

	public boolean isDisallowed(String location) {	
		if (!location.startsWith("http://") && !location.startsWith("https")) {
			this.logger.info(String.format("Protocol of location '%s' not supported", location));
			return Boolean.FALSE;
		}
		
		// getting the host[:port] string 
		String hostPort = this.getHostPort(location);

		// getting the RobotsTxt-info for this host[:port]
		RobotsTxt robotsTxt = null;
		try {
			synchronized (hostPort) {
				// trying to geht the robots.txt from cache
				robotsTxt = (RobotsTxt) this.cache.retrieve(hostPort);

				// trying to get the robots.txt from file
				if (robotsTxt == null) {
					robotsTxt = this.loadRobotsTxt(hostPort);
					if (robotsTxt != null) {
						this.cache.store(hostPort, robotsTxt, robotsTxt.getExpirationDate().getTime(), null);
					}
				}

				// trying to download the robots.txt
				if (robotsTxt == null || (System.currentTimeMillis() - robotsTxt.getLoadedDate().getTime() > robotsTxt.getReloadInterval())) {				
					robotsTxt = this.parseRobotsTxt("http://" + hostPort + "/robots.txt");
					this.cache.store(hostPort, robotsTxt, robotsTxt.getExpirationDate().getTime(), null);
					this.storeRobotsTxt(robotsTxt);
				}
			}

			return robotsTxt.isDisallowed("paxle", this.getPath(location));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void storeRobotsTxt(RobotsTxt robotsTxt) {
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

	private RobotsTxt loadRobotsTxt(String hostPort) {
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
}