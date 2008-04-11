
package org.paxle.gui.impl.servlets;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.core.queue.CommandProfile;
import org.paxle.core.queue.ICommandProfile;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.ServiceManager;

public class CrawlerView extends ALayoutServlet {
	
	private static final long serialVersionUID = 1L;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	private class UrlTank {
		/**
		 * @see org.paxle.data.db.ICommandDB
		 */
		private final Object commandDB;
		
		/**
		 * org.paxle.data.db.ICommandProfileDB
		 */
		private final Object profileDB;
		
		/**
		 * @see org.paxle.filter.robots.IRobotsTxtManager
		 */
		private final Object robotsManager;
		
		/**
		 * @see org.paxle.filter.robots.IRobotsTxtManager#isDisallowed(String)
		 */
		private Method isDisallowed = null;
		
		/**
		 * @see org.paxle.data.db.ICommandDB#isKnown(URI)
		 */
		private Method doubleURL = null;
		
		/**
		 * @see org.paxle.data.db.ICommandDB#enqueue(URI, int, int)
		 */
		private Method enqueueCommand = null;
		
		/**
		 * @see org.paxle.core.queue.ICommandProfileManager#storeProfile(ICommandProfile)
		 */
		private Method storeProfile = null;
		
		/**
		 * The crawling profile to use
		 */
		private ICommandProfile profile = null;
		
		private String profileName = null;
		
		private int crawlDepth = 0;		
		
		private HashMap<String,String> errorUrls = null;
		
		@SuppressWarnings("unchecked")
		public UrlTank(final ServiceManager manager, int crawlDepth, String profileName) throws Exception {
			this.commandDB = manager.getService("org.paxle.data.db.ICommandDB");
			if (this.commandDB == null) throw new Exception("Command-DB not available");
			this.enqueueCommand = this.commandDB.getClass().getMethod("enqueue", URI.class, int.class, int.class);
			this.doubleURL = this.commandDB.getClass().getMethod("isKnown", URI.class);
			
			this.profileDB = manager.getService("org.paxle.core.queue.ICommandProfileManager");
			if (this.profileDB == null) throw new Exception("Profile-DB not available");
			this.storeProfile = this.profileDB.getClass().getMethod("storeProfile", ICommandProfile.class);
			
			robotsManager = manager.getService("org.paxle.filter.robots.IRobotsTxtManager");
			if (robotsManager != null) isDisallowed = robotsManager.getClass().getMethod("isDisallowed", String.class);
			
			this.crawlDepth = crawlDepth;
			this.profileName = profileName;
		}
		
		public void putUrl2Crawl(final String location) throws Exception {
			final String url = location.trim();
			if (url.length() == 0) {
				putError(location, "URL '" + location + "' is not valid");
				return;
			}
			
			// check if URL is blocked by robots-txt
			if (robotsManager != null && isDisallowed != null) {
				final Object result = isDisallowed.invoke(robotsManager, url);
				if (result != null && ((Boolean)result).booleanValue()) {
					final String msg = "Not allowed to begin crawling for the URL is blocked by robots.txt";
					logger.info(msg);
					putError(location, msg);
					return;
				}
			}
			
			// check if URL is already known
			final Object result = doubleURL.invoke(this.commandDB, URI.create(url));
			if (result != null && ((Boolean)result).booleanValue()) {
				final String msg = "URL rejected. URL was already crawled.";
				logger.info(msg);
				putError(location, msg);
				return;
			}
			
			logger.info("Initiated crawl of URL '" + url + "'");
			if (this.profile == null) {
				// create a new profile
				this.profile = new CommandProfile();
				this.profile.setMaxDepth(this.crawlDepth);
				this.profile.setName(this.profileName);
				
				// store it into the profile-db
				this.storeProfile.invoke(this.profileDB, this.profile);
			}
			
			// store command into DB
			this.enqueueCommand.invoke(this.commandDB, new URI(url), this.profile.getOID(), 0);
		}
		
		private void putError(final String url, final String err) {
			if (errorUrls == null)
				errorUrls = new HashMap<String,String>();
			errorUrls.put(url, err);
		}
		
		public Map<String,String> getErrorUrls() {
			return errorUrls;
		}
	}
	
	public Template handleRequest( 
			HttpServletRequest request,
			HttpServletResponse response,
			Context context 
	) {
		
		Template template = null;
		
		try {
			String url = null;
			if (request.getParameter("crawlSingle") != null) {
				url = request.getParameter("startURL");
				
				// startURL denotes a single URL to crawl entered in an input-field
				final UrlTank tank = new UrlTank(
						(ServiceManager)context.get(SERVICE_MANAGER),
						this.getDepth(request),
						this.getProfileName(request)
				);
				tank.putUrl2Crawl(url);
				context.put("errorUrls", tank.getErrorUrls());
				
			} else if (request.getParameter("crawlMass") != null) {
				url = request.getParameter("startURL2");
				
				// startURL2 contains a whole bunch of URLs to crawl entered in a textarea
				final UrlTank tank = new UrlTank(
						(ServiceManager)context.get(SERVICE_MANAGER),
						this.getDepth(request),
						this.getProfileName(request)
				);
				final BufferedReader startURLs = new BufferedReader(new StringReader(url));
				String line;
				while ((line = startURLs.readLine()) != null)
					tank.putUrl2Crawl(line);
				context.put("errorUrls", tank.getErrorUrls());
			} else {
				// default values for input fields
				context.put("defaultDepth", "3");
				context.put("defaultName", "Crawl " + this.formatter.format(new Date()));
			}
			
			/*
			 * Setting template parameters
			 */
			template = this.getTemplate("/resources/templates/CrawlerView.vm");
		} catch( Exception e ) {
			logger.error("Error processing request: " + e.getMessage(), e);
		}
		
		return template;
	}
	
	private int getDepth(HttpServletRequest request) {
		String depthStr = request.getParameter("crawlDepth");
		if (depthStr == null) return 0;
		
		try {
			return Integer.valueOf(depthStr);
		} catch (NumberFormatException e){
			return 0;
		}
	}
	
	private String getProfileName(HttpServletRequest request) {
		String name = request.getParameter("profileName");
		return (name == null) ? "Crawl " + this.formatter.format(new Date()) : name;
	}
}
