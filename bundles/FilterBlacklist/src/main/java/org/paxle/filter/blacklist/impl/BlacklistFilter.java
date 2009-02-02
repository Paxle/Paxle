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
package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
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
	
	/**
	 * The data directory containing {@link Blacklist}-files.
	 */
	private File blacklistDir;
	
	/**
	 * A map containing the {@link Blacklist#name blacklist-name} as key and the {@link Blacklist blacklist}
	 * as value. 
	 */
	private ConcurrentHashMap<String,Blacklist> blacklists = new ConcurrentHashMap<String,Blacklist>();

	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());

	/**
	 * @param dir the data directory containing {@link Blacklist}-files.
	 * @throws InvalidFilenameException
	 */
	public BlacklistFilter(File dir) throws InvalidFilenameException {
		if (dir == null) throw new NullPointerException("The file-object must not be null.");
		this.blacklistDir = dir;
		
		Iterator<?> eter = FileUtils.iterateFiles(this.blacklistDir, null, false);
		while(eter.hasNext()) {
			File blacklistFile = (File) eter.next();
			Blacklist blacklist = new Blacklist(blacklistFile.getName(), blacklistFile, this);
			blacklists.put(blacklistFile.getName(), blacklist);
		}
	}

	public void filter(ICommand command, IFilterContext filterContext) {
		FilterResult result = this.isListed(command.getLocation().toString());		// XXX should this be .toASCIIString()?
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
		final long start = System.currentTimeMillis();
		
		// getting the parsed document
		final Counter c = new Counter();
		final IParserDocument parserDoc = command.getParserDocument();
		
		// check URIs against blacklists
		final int rejected = this.checkBlacklist(parserDoc, c);
		
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

	/**
	 * @return the number of blocked {@link URI}
	 */
	private int checkBlacklist(IParserDocument parserDoc, Counter c) {
		if (parserDoc == null) return 0;
		
		int cnt = 0;
		
		// getting the link map
		Map<URI, LinkInfo> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			cnt += this.checkBlacklist(linkMap,c);
		}

		// loop through sub-parser-docs
		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				cnt += this.checkBlacklist(subDoc,c);
			}
		}
		
		return cnt;
	}   

	int checkBlacklist(Map<URI, LinkInfo> linkMap, Counter c) {
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
			FilterResult result = this.isListed(location.toString());		// XXX should this be .toASCIIString()?
			
			// mark URI as rejected
			if (result.hasStatus(FilterResult.LOCATION_REJECTED)) {
				meta.setStatus(Status.FILTERED, "Rejected by blacklistentry: " + result.getRejectPattern());
				this.logger.debug(String.format("%s rejected by blacklistentry: %s", location, result.getRejectPattern()));
				cnt++;
				c.c++;
			}
		}
		
		return cnt;
	}

	/**
	 * Checks if an URL is listed in any blacklist
	 * @param url URL to be checked against blacklists
	 * @return returns a String containing the pattern which blacklists the url, returns null otherwise
	 */
	private FilterResult isListed(String url) {
		Iterator<Blacklist> allLists = blacklists.values().iterator();
		while (allLists.hasNext()) {
			FilterResult result = allLists.next().isListed(url);
			if (result.getStatus() == FilterResult.LOCATION_REJECTED)
				return result;
		}
		return FilterResult.LOCATION_OKAY_RESULT;
	}


	/**
	 * Gets all blacklistnames
	 * @return all blacklistnames
	 */
	public List<String> getLists() {
		return new ArrayList<String>(blacklists.keySet());
	}

	/**
	 * creates a blacklist
	 * @param name the name of the blacklist
	 * @return the blacklist that was created, can be null when there is a failure
	 * @throws InvalidFilenameException 
	 */
	public Blacklist createList(String name) throws InvalidFilenameException {
		this.validateBlacklistname(name);

		if (this.getList(name) != null)
			return this.getList(name);
		else {
			try {
				File listFile = new File(blacklistDir, name);
				FileUtils.touch(listFile);
				return new Blacklist(name, listFile, this);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * gets the blacklist
	 * @param name the name of the list
	 * @return the blacklist
	 * @throws InvalidFilenameException 
	 */
	public Blacklist getList(String name) throws InvalidFilenameException {
		this.validateBlacklistname(name);
		return blacklists.get(name);
	}

	/**
	 * store the blacklist so that it can be derived using getList
	 * @param blacklist the list to be stored
	 */
	public void storeList(Blacklist blacklist) {
		blacklists.put(blacklist.name, blacklist);
	}

	/**
	 * remove the blacklist from the store so that it can be longer accessed
	 * please note that this does not delete the blacklist
	 * @param blacklist the list to be unstored
	 */
	public void unstoreList(Blacklist blacklist) {
		blacklists.remove(blacklist.name);
	}

	static final int NAME_OK = -1;
	static final int LENGTH_ZERO = -2;

	/**
	 * Checks whether the given name is a valid blacklist name. First, all whitespace is removed, then the length
	 * of the result is tested. If it is zero, {@link #LENGTH_ZERO} is returned. If the remaining characters are
	 * valid, this method returns {@link #NAME_OK}, otherwise the first invalid character is returned.
	 *   
	 * @param name the blacklist name to check
	 * @return {@link #NAME_OK} if the given name is a valid name for a blacklist, {@link #LENGTH_ZERO} if the
	 *         name only consists of whitespace or is an empty string, the first invalid character otherwise.
	 */
	int offendingChar(final String name) {
		final String others = "+-_.&()=";

		final String nn = name.replace("\\s", "").toLowerCase();
		if (nn.length() == 0)
			return LENGTH_ZERO;

		for (int i=0; i<nn.length(); i++) {
			final char c = nn.charAt(i);
			if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9') || others.indexOf(c) != -1)
				return c;
		}
		return NAME_OK;
	}

	/**
	 * Uses {@link #offendingChar(String)} to test whether the name is a valid identifier for a blacklist.
	 * @param name the name to check
	 * @return whether the given name is a valid name for a blacklist or not
	 */
	boolean isValidBlacklistName(final String name) {
		return this.offendingChar(name) == NAME_OK;
	}

	/**
	 * This method checks a given name for attempts of a directory traversal, an empty name and for invalid characters
	 * @throws InvalidFilenameException 
	 */
	private void validateBlacklistname(final String name) throws InvalidFilenameException {
		final int c = this.offendingChar(name);
		switch (c) {
			case NAME_OK: return;
			case LENGTH_ZERO: throw new InvalidFilenameException("The blacklist name is empty.");
			default:
				throw new InvalidFilenameException(
						"The name '" + name + "' is not a valid name for a blacklist. " +
						"Please remove all '" + (char)c + "' characters.");
		}
	}
}
