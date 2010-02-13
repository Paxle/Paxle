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

package org.paxle.filter.blacklist.impl;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICommandProfile;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.doc.LinkInfo.Status;
import org.paxle.core.filter.FilterQueuePosition;
import org.paxle.core.filter.FilterTarget;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.filter.blacklist.IBlacklistManager;
import org.paxle.filter.blacklist.IFilterResult;
import org.paxle.filter.blacklist.IRegexpBlacklistFilter;

/**
 * This is a RegExp-based Blacklistfilter
 * @author Matthias Soehnholz
 * @author Michael Hamann
 */
@Component(metatype=false)
@Service(IFilter.class)
@Properties({
	@Property(name="org.paxle.metadata", boolValue=true),
	@Property(name="org.paxle.metadata.localization", value="/OSGI-INF/l10n/BlacklistFilter")
})
@FilterTarget({
	@FilterQueuePosition(queueId=FilterQueuePosition.CRAWLER_IN,position=0-1),
	@FilterQueuePosition(queueId=FilterQueuePosition.PARSER_OUT,position=66)
})
public class BlacklistFilter implements IRegexpBlacklistFilter {
	private static final String PROP_ADDITIONAL_BLACKLISTS = BlacklistFilter.class.getName() + ".additionalBlacklistNames";	
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	@Reference
	IBlacklistManager manager;
	
	/**
	 * Class to count rejected URI
	 */
	static class Counter {
		/**
		 * Number of rejected {@link URI}.
		 */
		public int c = 0;
		
		/**
		 * Total number of checked {@link URI}.
		 */
		public int t = 0;
	}	
	
	public void filter(ICommand command, IFilterContext filterContext) {
		// ignoring not-ok commands
		if (command.getResult() != ICommand.Result.Passed) {
			logger.debug("Command didn't pass, aborting blacklist filtering.");
			return;
		}		

		// get additional blacklists to check
		final String[] enabledBlacklistNames = this.getAdditionalBlacklistNames(command, filterContext);
		
		// checking command location
		IFilterResult result = this.manager.isListed(command.getLocation().toString(), enabledBlacklistNames);		// XXX should this be .toASCIIString()?
		if(result.hasStatus(IFilterResult.LOCATION_REJECTED)) {
			command.setResult(ICommand.Result.Rejected, "rejected by blacklistentry: " + result.getRejectPattern());
			logger.info(command.getLocation() + " rejected by blacklistentry: " + result.getRejectPattern());
			return;
		}
		
		// check the extracted links
		final long start = System.currentTimeMillis();
		
		// getting the parsed document
		final Counter c = new Counter();
		final IParserDocument parserDoc = command.getParserDocument();
		
		// check URIs against blacklists
		final int rejected = this.checkBlacklist(parserDoc, c, enabledBlacklistNames);
		
		if (rejected > 0 || this.logger.isDebugEnabled()) {
			this.logger.info(String.format(
					"Rejecting %d out of %d URIs from reference map(s) of '%s' in %d ms.", 
					Integer.valueOf(rejected), 
					Integer.valueOf(c.t),
					command.getLocation(),
					Long.valueOf(System.currentTimeMillis() - start)
			));
		}
	}

	private String[] getAdditionalBlacklistNames(ICommand command, IFilterContext filterContext) {
		if (filterContext == null) return null;
		String[] enabledBlacklistNames = null;
		
		final ICommandProfile profile = filterContext.getCommandProfile(command.getProfileOID());
		Object propObj = profile.getProperty(PROP_ADDITIONAL_BLACKLISTS);
		if (propObj instanceof String[]) {
			enabledBlacklistNames = (String[]) propObj;
		} else if (propObj instanceof String) {
			enabledBlacklistNames = ((String)propObj).split(",");
		}
		
		return enabledBlacklistNames;
	}
	
	/**
	 * @return the number of blocked {@link URI}
	 */
	private int checkBlacklist(IParserDocument parserDoc, Counter c, String[] enabledBlacklistNames) {
		if (parserDoc == null) return 0;
		
		int cnt = 0;
		
		// getting the link map
		Map<URI, LinkInfo> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			cnt += this.checkBlacklist(linkMap,c, enabledBlacklistNames);
		}

		// loop through sub-parser-docs
		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				cnt += this.checkBlacklist(subDoc,c, enabledBlacklistNames);
			}
		}
		
		return cnt;
	}   

	int checkBlacklist(Map<URI, LinkInfo> linkMap, Counter c, String[] enabledBlacklistNames) {
		if (linkMap == null || linkMap.size() == 0) return 0;
		
		int cnt = 0;
		
		// loop through all known URI
		Iterator<Entry<URI, LinkInfo>> refs = linkMap.entrySet().iterator();
		while (refs.hasNext()) {						
			Entry<URI,LinkInfo> next = refs.next();
			URI location = next.getKey();
			LinkInfo meta = next.getValue();

			// skip URI that are already marked as not OK
			if (!meta.hasStatus(Status.OK)) continue;			

			// check if URI is backlisted
			c.t++;
			IFilterResult result = this.manager.isListed(location.toString(), enabledBlacklistNames);		// XXX should this be .toASCIIString()?
			
			// mark URI as rejected
			if (result.hasStatus(IFilterResult.LOCATION_REJECTED)) {
				meta.setStatus(Status.FILTERED, "Rejected by blacklistentry: " + result.getRejectPattern());
				this.logger.debug(String.format("%s rejected by blacklistentry: %s", location, result.getRejectPattern()));
				cnt++;
				c.c++;
			}
		}
		
		return cnt;
	}
}
