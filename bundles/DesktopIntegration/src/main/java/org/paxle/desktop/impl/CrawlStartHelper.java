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

package org.paxle.desktop.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.doc.ICommandProfile;
import org.paxle.core.doc.ICommandProfileManager;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.data.db.ICommandDB;
import org.paxle.desktop.ICrawlStartHelper;
import org.paxle.filter.robots.IRobotsTxtManager;

@Component(immediate=true)
@Service(ICrawlStartHelper.class)
@References({
	@Reference(
		name="robots",
		referenceInterface=IRobotsTxtManager.class,
		bind="setRobots",
		unbind="unsetRobots",
		cardinality=ReferenceCardinality.OPTIONAL_UNARY,
		policy=ReferencePolicy.DYNAMIC
	),
	@Reference(
		name="commandDB",
		referenceInterface=ICommandDB.class
	)
})
public class CrawlStartHelper implements ICrawlStartHelper {
	
	private static final Log logger = LogFactory.getLog(CrawlStartHelper.class);
	
	/** Default depth for crawls initiated using {@link #startDefaultCrawl(String)} */
	private static final int DEFAULT_PROFILE_MAX_DEPTH = 3;
	/** Default name of CrawlProfiles for crawls initiated by the DI-bundle */
	private static final String DEFAULT_NAME = "desktop-crawl";
	
	private final HashMap<Integer,Integer> profileDepthMap = new HashMap<Integer,Integer>();
	
	protected void activate(ComponentContext ctx) {
		commandDB = ctx.locateService("commandDB");
	}
	
	protected void deactivate(@SuppressWarnings("unused") ComponentContext ctx) {
		profileDepthMap.clear();
	}
	
	@Reference
	private IReferenceNormalizer refNormalizer;
	
	@Reference
	private ICommandProfileManager profileDB;
	
	@Reference(target="(docType=org.paxle.core.doc.ICommandProfile)")
	private IDocumentFactory profileFactory;
	
	// synchronization via "this"-object; ideally use RWLock, but so many crawls are not started concurrently
	private Object robots;
	
	private Object commandDB;
	
	public synchronized void setRobots(Object robots) {
		this.robots = robots;
	}
	
	public synchronized void unsetRobots(@SuppressWarnings("unused") Object robots) {
		this.robots = null;
	}
	
	public void startDefaultCrawl(final String location) {
		startCrawl(location, DEFAULT_PROFILE_MAX_DEPTH);
	}
	
	public void startCrawl(final String location, final int depth) {
		try {
			startCrawlImpl(location, depth);
		} catch (Exception ee) {
			Utilities.instance.showURLErrorMessage("Starting crawl failed: " + ee.getMessage(), location);
			logger.error("Starting crawl of URL '" + location + "' failed: " + ee.getMessage(), ee);
		}
	}
	
	private void startCrawlImpl(final String location, final int depth) throws ServiceException, IOException {
		final URI uri = refNormalizer.normalizeReference(location);
		
		// check uri against robots.txt
		synchronized (this) {
			if (robots != null) try {
				final Method isDisallowed = robots.getClass().getMethod("isDisallowed", URI.class);
				final Object result = isDisallowed.invoke(robots, uri);
				if (((Boolean)result).booleanValue()) {
					logger.info("Domain does not allow crawling of '" + uri + "' due to robots.txt blockage");
					Utilities.instance.showURLErrorMessage(
							"This URI is blocked by the domain's robots.txt, see",
							uri.resolve(URI.create("/robots.txt")).toString());
					return;
				}
			} catch (Exception e) {
				logger.warn(String.format("Error retrieving robots.txt from host '%s': [%s] %s - continuing crawl",
						uri.getHost(), e.getClass().getName(), e.getMessage()));
			}
		}
		
		// get or create the crawl profile to use for URI
		ICommandProfile cp = null;
		
		final Integer depthInt = Integer.valueOf(depth);
		final Integer id = profileDepthMap.get(depthInt);
		if (id != null)
			cp = profileDB.getProfileByID(id.intValue());
		if (cp == null) {
			// create a new profile
			cp = this.profileFactory.createDocument(ICommandProfile.class);
			cp.setMaxDepth(depth);
			cp.setName(DEFAULT_NAME);
			profileDB.storeProfile(cp);
		}
		if (id == null || cp.getOID() != id.intValue())
			profileDepthMap.put(depthInt, Integer.valueOf(cp.getOID()));
		
		try {
			final Method enqueueCommand = commandDB.getClass().getMethod("enqueue", URI.class, int.class, int.class);
			
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
