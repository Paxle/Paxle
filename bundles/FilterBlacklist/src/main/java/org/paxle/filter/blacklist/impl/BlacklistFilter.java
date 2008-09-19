package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.doc.LinkInfo.Status;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandProfile;
import org.paxle.core.queue.ICommandProfileManager;
import org.paxle.filter.blacklist.IRegexpBlacklistFilter;

/**
 * This is a RegExp-based Blacklistfilter
 * @author Matthias Soehnholz
 * @author Michael Hamann
 *
 */
public class BlacklistFilter implements IRegexpBlacklistFilter {


	private Log logger = LogFactory.getLog(this.getClass());

	public BlacklistFilter(File dir) {
		if (dir == null) throw new NullPointerException("The file-object must not be null.");
		Blacklist.init(dir);
	}

	public void filter(ICommand command, IFilterContext filterContext) {
		FilterResult result = isListed(command.getLocation().toString());		// XXX should this be .toASCIIString()?
		if(result.hasStatus(FilterResult.LOCATION_REJECTED)) {
			command.setResult(ICommand.Result.Rejected, "rejected by blacklistentry: " + result.getRejectPattern());
			logger.info(command.getLocation() + " rejected by blacklistentry: " + result.getRejectPattern());
			return;
		}
		
		// checking if the command-profile has additional restrictions
		int profileID = command.getProfileOID();
		if (profileID >= 0) {
			ICommandProfileManager profileManager = filterContext.getCommandProfileManager();
			if (profileManager != null) {
				ICommandProfile profile = profileManager.getProfileByID(profileID);
				if (profile != null) {
					// TODO: read blacklist-filter-specific properties
					String enabledBlacklistNames = (String) profile.getProperty(this.getClass().getSimpleName() + ".additionalBlacklistNames");
					if (enabledBlacklistNames != null) {
						// TODO: enabledBlacklistNames.split("[;]");
					}
				}
			}
		}	
		
		// check the extracted links
		IParserDocument parserDoc = command.getParserDocument();
		this.checkBlacklist(parserDoc);
	}

	private void checkBlacklist(IParserDocument parserDoc) {
		if (parserDoc == null) return;

		// getting the link map
		Map<URI, LinkInfo> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			this.checkBlacklist(linkMap);
		}

		// loop through sub-parser-docs
		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				this.checkBlacklist(subDoc);
			}
		}
	}   

	void checkBlacklist(Map<URI, LinkInfo> linkMap) {
		if (linkMap == null || linkMap.size() == 0) return;

		Iterator<Entry<URI, LinkInfo>> refs = linkMap.entrySet().iterator();
		while (refs.hasNext()) {
			Entry<URI,LinkInfo> next = refs.next();
			URI location = next.getKey();
			LinkInfo meta = next.getValue();
			
			// skip URI that are already marked as not OK
			if (!meta.hasStatus(Status.OK)) continue;
			
			// check if URI is backlisted
			FilterResult result = this.isListed(location.toString());		// XXX should this be .toASCIIString()?
			if (result.hasStatus(FilterResult.LOCATION_REJECTED)) {
				meta.setStatus(Status.FILTERED, "Rejected by blacklistentry: " + result.getRejectPattern());
				this.logger.info(String.format("%s rejected by blacklistentry: %s", location, result.getRejectPattern()));
			}
		}       
	}

	/**
	 * 
	 * @param url URL to be checked against blacklist
	 * @return returns a String containing the pattern which blacklists the url, returns null otherwise
	 */
	private FilterResult isListed(String url) {
		return Blacklist.isListedInAnyList(url);
	}

	/**
	 * gets a list of all blacklistnames
	 * @return a list of strings
	 */
	public List<String> getLists() {
		return Blacklist.getLists();
	}

	/**
	 * creates a blacklist
	 * @param name the name of the blacklist
	 * @return the blacklist that was created, can be null when there is a failure
	 * @throws InvalidFilenameException 
	 */
	public Blacklist createList(String name) throws InvalidFilenameException {
		return Blacklist.create(name);
	}

	/**
	 * gets a blacklist
	 * @param name the name of the blacklist
	 * @return the blacklist, may be null when a blacklist with the given name does not exist
	 * @throws InvalidFilenameException 
	 */
	public Blacklist getList(String name) throws InvalidFilenameException {
		
		return Blacklist.getList(name);
	}
}
