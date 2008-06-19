package org.paxle.data.db.impl;

import java.net.URI;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.doc.LinkInfo.Status;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandProfile;
import org.paxle.core.queue.ICommandProfileManager;

/**
 * XXX: This is just a fist step. later we'll split this filter into 
 * separate filters for crawler/parser/indexer-core-bundles
 */
public class CommandProfileFilter implements IFilter<ICommand> {
	private static final int DEFAULT_DEPTH = 0;

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

			int maxDepth;
			int currentDepth = command.getDepth();

			if (profileID == -1) {
				// no profile was configured use default settings
				maxDepth = DEFAULT_DEPTH;
			} else {
				// loading profile data
				ICommandProfile profile = this.profileDB.getProfileByID(profileID);
				if (profile == null) {
					this.logger.error(String.format("Unable to fild profile '%d' for command '%s'.",
							Integer.valueOf(profileID),
							command.getLocation().toASCIIString()
					));
					maxDepth = DEFAULT_DEPTH;
				} else {	
					maxDepth = profile.getMaxDepth();
				}
			}

			/* 
			 * TODO: move this into separate filters
			 */
			if (context.getTargetID().equals("org.paxle.crawler.in")) {			
				if (currentDepth > maxDepth) {
					command.setResult(ICommand.Result.Rejected, "Max-depth exceeded.");
					logger.info(command.getLocation() + " rejected. Max depth exceeded.");
					return;
				}
			} else if (context.getTargetID().equals("org.paxle.parser.out")) {
				final Counter c = new Counter();
				IParserDocument parserDoc = command.getParserDocument();
				this.checkLinks(maxDepth, currentDepth, parserDoc, c);
				logger.info(String.format("Removed %d URLs from reference map(s) of '%s'. Depth limit exceeded.", Integer.valueOf(c.c), command.getLocation())); 
			}
		} catch (Exception e) {
			this.logger.error(String.format(
					"Unexpected %s while filtering command with location '%s'.",
					e.getClass().getName(),
					command.getLocation().toASCIIString()
			),e);
		}
	}

	void checkLinks(final int maxDepth, final int currentDepth, IParserDocument parserDoc, final Counter c) {
		if (parserDoc == null) return;

		// getting the link map
		Map<URI, LinkInfo> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			if (currentDepth + 1 > maxDepth) {
				c.c += linkMap.size();

				// reject all links
				for (LinkInfo meta : linkMap.values()) {
					if (!meta.hasStatus(Status.OK)) continue;
					meta.setStatus(Status.FILTERED,"Max. crawl-depth exceeded.");
				}
			}
		}

		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				this.checkLinks(maxDepth, currentDepth, subDoc, c);
			}
		}
	}

}
