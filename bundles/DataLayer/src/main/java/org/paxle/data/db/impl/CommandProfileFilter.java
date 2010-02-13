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

package org.paxle.data.db.impl;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICommandProfile;
import org.paxle.core.doc.ICommandProfileManager;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.doc.ICommandProfile.LinkFilterMode;
import org.paxle.core.doc.LinkInfo.Status;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;

/**
 * XXX: This is just a fist step. later we'll split this filter into 
 * separate filters for crawler/parser/indexer-core-bundles
 */
public class CommandProfileFilter implements IFilter<ICommand> {
	private static final String QUEUE_CRAWLER_IN = "org.paxle.crawler.in";
	private static final String QUEUE_PARSER_OUT = "org.paxle.parser.out";
	
	/* 
	 * CONSTANTS for default command-profile
	 */
	private static final int DEFAULT_DEPTH = 0;
	private static final LinkFilterMode DEFAULT_LINKFILTER_MODE = LinkFilterMode.none;
	private static final String DEFAULT_LINKFILTER_EXPR = null;

	/**
	 * Class to count rejected URI
	 */
	static class Counter {		
		public int c = 0;
		public Map<URI, LinkInfo> blocked = new HashMap<URI, LinkInfo>();
		
		public String getBlockedURIList() {
			StringBuilder buf = new StringBuilder();
			
			for (Entry<URI, LinkInfo> entry : blocked.entrySet()) {
				String key = entry.getKey().toASCIIString();
				LinkInfo meta = entry.getValue();
				
				buf.append("\n\t").append(key).append(" | ")
				   .append(meta.getStatus()).append(" : ").append(meta.getStatusText());
			}
			
			return buf.toString();
		}
	}	

	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());

	/**
	 * Component to load and store crawling profiles
	 */
	private final ICommandProfileManager profileDB;
	
	/**
	 * A factory to create {@link ICommandProfile}s
	 */
	private final IDocumentFactory profileFactory;

	public CommandProfileFilter(ICommandProfileManager profileDB, IDocumentFactory profileFactory) {
		if (profileDB == null) throw new NullPointerException("The profile-db is null.");
		this.profileDB = profileDB;
		this.profileFactory = profileFactory;
	}

	public void filter(ICommand command, IFilterContext context) {
		try {
			// getting the command profile id
			int profileID = command.getProfileOID();

			// loading the command-profile
			ICommandProfile profile = null;
			if (profileID != -1) {
				// loading profile data
				profile = this.profileDB.getProfileByID(profileID);
				if (profile == null) {
					this.logger.error(String.format("Unable to fild profile '%d' for command '%s'.",
							Integer.valueOf(profileID),
							command.getLocation().toASCIIString()
					));
				} 
			} 
			
			// create a dummy profile
			if (profile == null) profile = createDummyProfile();

			/* 
			 * TODO: move this into separate filters
			 */
			if (context.getTargetID().equals(QUEUE_CRAWLER_IN)) {
				/* ================================================
				 * Check CRAWL_DEPTH
				 * ================================================ */
				if (command.getDepth() > profile.getMaxDepth()) {
					command.setResult(ICommand.Result.Rejected, "Max-depth exceeded.");
					logger.info(String.format(
							"%s rejected. Max depth exceeded. %d > %d.",
							command.getLocation(),
							Integer.valueOf(command.getDepth()),
							Integer.valueOf(profile.getMaxDepth())
					));
					return;
				}
				
				/* ================================================
				 * Check LINK_FILTER match
				 * ================================================ */
				LinkFilterMode filterMode = profile.getLinkFilterMode();
				String filterExpr = profile.getLinkFilterExpression();
				
				if (filterMode.equals(LinkFilterMode.regexp)) {
					if (!command.getLocation().toASCIIString().matches(filterExpr)) {
						command.setResult(ICommand.Result.Rejected, "Filtered by reg.exp filter.");
						logger.info(String.format(
								"%s rejected. Blocked by regex.filter: %s.",
								command.getLocation(),
								filterExpr
						));
						return;
					}
					
				}
			} else if (context.getTargetID().equals(QUEUE_PARSER_OUT)) {
				final Counter c = new Counter();
				final IParserDocument parserDoc = command.getParserDocument();
				
				// check all contained links
				this.checkLinks(profile, command, parserDoc, c);
				
				String logMessage = String.format(
						"Blocking %d URLs from reference map(s) of '%s' due to command-profile.",
						Integer.valueOf(c.c), 
						command.getLocation(),
						Integer.valueOf(profile.getMaxDepth())
				);
				
				if (this.logger.isDebugEnabled()) {
					this.logger.debug(logMessage + c.getBlockedURIList());
				} else if (this.logger.isInfoEnabled()) {
					this.logger.info(logMessage); 
				}
			}
		} catch (Exception e) {
			this.logger.error(String.format(
					"Unexpected %s while filtering command with location '%s'.",
					e.getClass().getName(),
					command.getLocation().toASCIIString()
			),e);
		}
	}

	private ICommandProfile createDummyProfile() throws IOException {
		// create a dummy profile
		ICommandProfile profile = this.profileFactory.createDocument(ICommandProfile.class);
		profile.setMaxDepth(DEFAULT_DEPTH);
		profile.setLinkFilterMode(DEFAULT_LINKFILTER_MODE);
		profile.setLinkFilterExpression(DEFAULT_LINKFILTER_EXPR);
		return profile;
	}
	
	void checkLinks(final ICommandProfile profile, final ICommand command, IParserDocument parserDoc, final Counter c) {
		if (parserDoc == null) return;

		// getting the link map
		Map<URI, LinkInfo> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			/* ================================================
			 * Check CRAWL_DEPTH
			 * ================================================ */
			if (command.getDepth() + 1 > profile.getMaxDepth()) {

				// reject all links
				for (LinkInfo meta : linkMap.values()) {
					if (!meta.hasStatus(Status.OK)) continue;
					meta.setStatus(Status.FILTERED,"Max. crawl-depth exceeded.");
				}
				
				// collect data
				c.c += linkMap.size();
				c.blocked.putAll(linkMap);
			}
			
			/* ================================================
			 * Check LINK_FILTER match
			 * ================================================ */
			LinkFilterMode filterMode = profile.getLinkFilterMode();
			String filterExpr = profile.getLinkFilterExpression();
			
			if (filterMode.equals(LinkFilterMode.regexp)) {
				Pattern regexpPattern = Pattern.compile(filterExpr);
				
				for (Entry<URI, LinkInfo> entry : linkMap.entrySet()) {
					final URI link = entry.getKey();
					final LinkInfo meta = entry.getValue();
					
					// skip already blocked links
					if (!meta.hasStatus(Status.OK)) continue;
					
					// check against regexp
			        Matcher m = regexpPattern.matcher(link.toASCIIString());
			        if (!m.matches()) {
			        	c.c++;
			        	meta.setStatus(Status.FILTERED,"Blocked by regexp filter");
			        	c.blocked.put(link, meta);
			        }
				}
			}
		}

		/* ================================================
		 * Check SUB-DOCUMENTS
		 * ================================================ */	
		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				this.checkLinks(profile, command, subDoc, c);
			}
		}
	}

}
