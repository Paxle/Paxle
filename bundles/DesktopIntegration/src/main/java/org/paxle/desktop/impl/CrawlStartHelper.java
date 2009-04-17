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

package org.paxle.desktop.impl;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.core.queue.CommandProfile;
import org.paxle.core.queue.ICommandProfile;
import org.paxle.core.queue.ICommandProfileManager;
import org.paxle.desktop.Utilities;


public class CrawlStartHelper {
	
	private static final Log logger = LogFactory.getLog(CrawlStartHelper.class);
	
	/** The fully qualified name of the interface under which the CommandDB of the DataLayer-bundle registered to the framework */
	private static final String ICOMMANDDB = "org.paxle.data.db.ICommandDB";
	/** The fully qualified name of the interface under which the RobotsTxtManager of the FilterRobotsTxt-bundle registered to the framework */
	private static final String IROBOTSM = "org.paxle.filter.robots.IRobotsTxtManager";
	
	/** Default depth for crawls initiated using {@link #startDefaultCrawl(String)} */
	private static final int DEFAULT_PROFILE_MAX_DEPTH = 3;
	/** Default name of CrawlProfiles for crawls initiated by the DI-bundle */
	private static final String DEFAULT_NAME = "desktop-crawl";
	
	private final HashMap<Integer,Integer> profileDepthMap = new HashMap<Integer,Integer>();
	private final ServiceManager manager;
	
	public CrawlStartHelper(final ServiceManager manager) {
		this.manager = manager;
	}
	
	public void startDefaultCrawl(final String location) {
		try {
			startCrawl(location, DEFAULT_PROFILE_MAX_DEPTH);
		} catch (ServiceException ee) {
			Utilities.showURLErrorMessage("Starting crawl failed: " + ee.getMessage(), location);
			logger.error("Starting crawl of URL '" + location + "' failed: " + ee.getMessage(), ee);
		}
	}
	
	public void startCrawl(final String location, final int depth) throws ServiceException {
		final IReferenceNormalizer refNormalizer = manager.getService(IReferenceNormalizer.class);
		if (refNormalizer == null)
			throw new ServiceException("Reference normalizer", IReferenceNormalizer.class.getName());
		final URI uri = refNormalizer.normalizeReference(location);
		
		// check uri against robots.txt
		final Object robotsManager = manager.getService(IROBOTSM);
		if (robotsManager != null) try {
			final Method isDisallowed = robotsManager.getClass().getMethod("isDisallowed", URI.class);
			final Object result = isDisallowed.invoke(robotsManager, uri);
			if (((Boolean)result).booleanValue()) {
				logger.info("Domain does not allow crawling of '" + uri + "' due to robots.txt blockage");
				Utilities.showURLErrorMessage(
						"This URI is blocked by the domain's robots.txt, see",
						uri.resolve(URI.create("/robots.txt")).toString());
				return;
			}
		} catch (Exception e) {
			logger.warn(String.format("Error retrieving robots.txt from host '%s': [%s] %s - continuing crawl",
					uri.getHost(), e.getClass().getName(), e.getMessage()));
		}
		
		// get or create the crawl profile to use for URI
		ICommandProfile cp = null;
		final ICommandProfileManager profileDB = manager.getService(ICommandProfileManager.class);
		if (profileDB == null)
			throw new ServiceException("Profile manager", ICommandProfileManager.class.getName());
		
		final Integer depthInt = Integer.valueOf(depth);
		final Integer id = profileDepthMap.get(depthInt);
		if (id != null)
			cp = profileDB.getProfileByID(id.intValue());
		if (cp == null) {
			// create a new profile
			cp = new CommandProfile();
			cp.setMaxDepth(depth);
			cp.setName(DEFAULT_NAME);
			profileDB.storeProfile(cp);
		}
		if (id == null || cp.getOID() != id.intValue())
			profileDepthMap.put(depthInt, Integer.valueOf(cp.getOID()));
		
		// get the command-db object and it's method to enqueue the URI
		final Object commandDB;
		final Method enqueueCommand;
		try {
			commandDB = manager.getService(ICOMMANDDB);
			if (commandDB == null)
				throw new ServiceException("Command-DB", ICOMMANDDB);
			enqueueCommand = commandDB.getClass().getMethod("enqueue", URI.class, int.class, int.class);
			
			final Object result = enqueueCommand.invoke(commandDB, uri, Integer.valueOf(cp.getOID()), Integer.valueOf(0));
			if (((Boolean)result).booleanValue()) {
				logger.info("Initiated crawl of URL '" + uri + "'");
			} else {
				logger.info("Initiating crawl of URL '" + uri + "' failed, URL is already known");
			}
		} catch (Exception e) {
			throw new ServiceException("Crawl start", e.getMessage(), e);
		}
	}
}
