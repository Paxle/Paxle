package org.paxle.data.db.impl;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.doc.LinkInfo.Status;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.CommandProfile;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandProfile;
import org.paxle.core.queue.ICommandProfileManager;
import org.paxle.core.queue.ICommandProfile.LinkFilterMode;

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
	}	

	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());

	/**
	 * Component to load and store crawling profiles
	 */
	private final ICommandProfileManager profileDB;

	public CommandProfileFilter(ICommandProfileManager profileDB) {
		if (profileDB == null) throw new NullPointerException("The profile-db is null.");
		this.profileDB = profileDB;
	}

	public void filter(ICommand command, IFilterContext context) {
		if (command == null) throw new NullPointerException("The command object is null.");

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
							command.getDepth(),
							profile.getMaxDepth()
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
				
				logger.info(String.format(
						"Blocking %d URLs from reference map(s) of '%s' due to command-profile.", 
						Integer.valueOf(c.c), 
						command.getLocation(),
						profile.getMaxDepth()
				)); 
			}
		} catch (Exception e) {
			this.logger.error(String.format(
					"Unexpected %s while filtering command with location '%s'.",
					e.getClass().getName(),
					command.getLocation().toASCIIString()
			),e);
		}
	}

	private ICommandProfile createDummyProfile() {
		// create a dummy profile
		CommandProfile profile = new CommandProfile();
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
				c.c += linkMap.size();

				// reject all links
				for (LinkInfo meta : linkMap.values()) {
					if (!meta.hasStatus(Status.OK)) continue;
					meta.setStatus(Status.FILTERED,"Max. crawl-depth exceeded.");
				}
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
