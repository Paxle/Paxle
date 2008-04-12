package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.paxle.filter.blacklist.IRegexpBlacklistFilter;

/**
 * This is a RegExp-based Blacklistfilter
 * @author Matthias Soehnholz
 * @author Michael Hamann
 *
 */
public class BlacklistFilter implements IRegexpBlacklistFilter {


	private Log logger = LogFactory.getLog(this.getClass());

	public BlacklistFilter(File list) {
		Blacklist.init(list);
	}

	public void filter(ICommand command, IFilterContext filterContext) {
		FilterResult result = isListed(command.getLocation().toString());		// XXX should this be .toASCIIString()?
		if(result.getStatus()==FilterResult.LOCATION_REJECTED) {
			command.setResult(ICommand.Result.Rejected, "rejected by blacklistentry: " + result.getRejectPattern());
			//System.out.println(command.getLocation() + " rejected by blacklistentry: " + result.getRejectPattern());
			logger.info(command.getLocation() + " rejected by blacklistentry: " + result.getRejectPattern());
			return;
		}
		// check the extracted links
		IParserDocument parserDoc = command.getParserDocument();
		this.checkBlacklist(parserDoc);
	}

	private void checkBlacklist(IParserDocument parserDoc) {
		if (parserDoc == null) return;

		// getting the link map
		Map<URI, String> linkMap = parserDoc.getLinks();
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

	private void checkBlacklist(Map<URI, String> linkMap) {
		if (linkMap == null || linkMap.size() == 0) return;

		Iterator<URI> refs = linkMap.keySet().iterator();
		while (refs.hasNext()) {
			URI location = refs.next();
			FilterResult result = isListed(location.toString());		// XXX should this be .toASCIIString()?
			if (result.getStatus()==FilterResult.LOCATION_REJECTED) {
				refs.remove();
				//System.out.println(location + " rejected by blacklistentry: " + result.getRejectPattern());
				this.logger.info(location + " rejected by blacklistentry: " + result.getRejectPattern());
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
	 */
	public Blacklist createList(String name) {
		return Blacklist.create(name);
	}

	/**
	 * gets a blacklist
	 * @param name the name of the blacklist
	 * @return the blacklist, may be null when a blacklist with the given name does not exist
	 */
	public Blacklist getList(String name) {
		return Blacklist.getList(name);
	}
}
