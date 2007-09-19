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
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cache.Cache;
import org.apache.commons.cache.EvictionPolicy;
import org.apache.commons.cache.GroupMap;
import org.apache.commons.cache.MemoryStash;
import org.apache.commons.cache.SimpleCache;
import org.apache.commons.cache.StashPolicy;

public class RobotsTxtManager {
	private static final Pattern HOSTPORT_PATTERN = Pattern.compile("([^:]+)://([^/\\?#]+)(?=/|$|\\?|#)");
	private static final Pattern PATH_PATTERN = Pattern.compile("((?<!:/)/(?!/).*$)");

	private Cache cache = null;
	private File path = null;

	public RobotsTxtManager(File path) {
		this.cache = new SimpleCache(new MemoryStash(1000), (EvictionPolicy)null, (StashPolicy)null, (GroupMap)null, (File)null);
		this.path = path;
		if (!this.path.exists()) this.path.mkdirs();
	}

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

	private String getPath(String location) {
		Matcher matcher = PATH_PATTERN.matcher(location);
		return (!matcher.find())? "" : matcher.group(1);
	}

	private RobotsTxt parseRobotsTxt(String robotsUrlStr) throws IOException {
		String hostPort = this.getHostPort(robotsUrlStr);
		URL robotsURL = new URL(robotsUrlStr);

		String statusLine = null;
		URLConnection connection = robotsURL.openConnection();
		if ( connection instanceof HttpURLConnection) {
			HttpURLConnection httpConnection = (HttpURLConnection) connection;

			int code = httpConnection.getResponseCode();
			statusLine = httpConnection.getHeaderField(0);
			
			if (code == 401 || code == 403) {
				// access to the whole website is restricted
				return new RobotsTxt(hostPort,new Date(), statusLine, true);
			} else if (code == 404) {
				// no robots.txt provided
				return new RobotsTxt(hostPort,new Date(), statusLine);
			} else if (code != 200) {
				// the robots.txt seems not to be deliverable
				return new RobotsTxt(hostPort,new Date(), statusLine);
			}
			
			String mimeType = httpConnection.getContentType();
			if (mimeType != null && !mimeType.startsWith("text/plain")) {
				// the robots.txt seems not to be available
				return new RobotsTxt(hostPort, new Date(), "Wrong mimeType " + mimeType);
			}
		}

		InputStream inputStream = null;
		try {
			RobotsTxt robotsTxt = new RobotsTxt(hostPort, new Date(), statusLine);
			return this.parseRobotsTxt(robotsTxt, connection.getInputStream());
		} finally {
			if (inputStream != null) try { inputStream.close(); } catch (Exception e) {/* ignore this */}
		}

	}

	private RobotsTxt parseRobotsTxt(RobotsTxt robotsTxt, InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));		
		RuleBlock currentBlock = null;

		boolean inRuleBlock = false;
		String line = null;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			line = this.cutOfComments(line);
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
						this.cache.store(hostPort, robotsTxt, null, null);
					}
				}

				// trying to download the robots.txt
				if (robotsTxt == null) {				
					robotsTxt = this.parseRobotsTxt("http://" + hostPort + "/robots.txt");
					this.cache.store(hostPort, robotsTxt, null, null);
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
		try {
			// getting the host:port string
			hostPort = hostPort.replace(':', '_');			

			// getting the file
			File robotsTxtFile = new File(this.path,hostPort);
			if (!robotsTxtFile.exists()) return null;

			// loading object from file
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(robotsTxtFile)));
			RobotsTxt robotsTxt = (RobotsTxt) ois.readObject();
			return robotsTxt;
// TODO:			
//		} catch (InvalidClassException e) {
//			// just ignore this, class format has changed
//			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (ois != null) try { ois.close(); } catch (Exception e) {/* ingore this */}
		}
	}
}