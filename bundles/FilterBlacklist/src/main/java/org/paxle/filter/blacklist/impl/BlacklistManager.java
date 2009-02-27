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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.paxle.filter.blacklist.IBlacklist;
import org.paxle.filter.blacklist.IBlacklistManager;
import org.paxle.filter.blacklist.IFilterResult;
import org.paxle.filter.blacklist.InvalidFilenameException;

/**
 * @scr.component immediate="true"
 * @scr.service interface="org.paxle.filter.blacklist.IBlacklistManager"
 */
public class BlacklistManager implements IBlacklistManager {
	
	/**
	 * The data directory containing {@link Blacklist}-files.
	 */
	private File blacklistDir;
	
	/**
	 * A map containing the {@link Blacklist#name blacklist-name} as key and the {@link Blacklist blacklist}
	 * as value. 
	 */
	private ConcurrentHashMap<String,IBlacklist> blacklists = new ConcurrentHashMap<String,IBlacklist>();

	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * This function is called by the OSGi framework if this component is activated
	 */
	protected void activate(ComponentContext context) throws InvalidFilenameException {
		// getting the data directory to use
		final String dataPath = System.getProperty("paxle.data") + File.separatorChar + "blacklist";
		this.initBlacklistManager(new File(dataPath));
	}	
		
	void initBlacklistManager(File blacklistDir) throws InvalidFilenameException {
		this.blacklistDir = blacklistDir;
	
		// create the directory if required
		if (!this.blacklistDir.exists()) this.blacklistDir.mkdirs();
		// new File(this.blacklistDir, "default.list").createNewFile();
		
		// init the blacklist objects
		Iterator<?> eter = FileUtils.iterateFiles(this.blacklistDir, null, false);
		this.logger.info(String.format(
				"Reading blacklists from directory: %s",
				blacklistDir.toString()
		));
		
		final StringBuilder blacklistNames = new StringBuilder();
		while(eter.hasNext()) {
			final File blacklistFile = (File) eter.next();
			final String blacklistName = blacklistFile.getName();			
			final Blacklist blacklist = new Blacklist(blacklistName, blacklistFile, this);
			this.blacklists.put(blacklistName, blacklist);
			
			blacklistNames.append(String.format(
					"\n- '%s' : %d pattern(s)",
					blacklistName,
					blacklist.size()
			));			
		}
		this.logger.info(String.format(
				"%d blacklist(s) found: %s",
				this.blacklists.size(),
				blacklistNames
		));
	}

	protected void deactivate(ComponentContext context) throws Exception {
		// clear blacklist map
		this.blacklists.clear();
	}	
	

	/* (non-Javadoc)
	 * @see org.paxle.filter.blacklist.impl.IBlacklistManager#isListed(java.lang.String)
	 */
	public IFilterResult isListed(String url) {
		Iterator<IBlacklist> allLists = blacklists.values().iterator();
		while (allLists.hasNext()) {
			IFilterResult result = allLists.next().isListed(url);
			if (result.getStatus() == FilterResult.LOCATION_REJECTED)
				return result;
		}
		return FilterResult.LOCATION_OKAY_RESULT;
	}


	/* (non-Javadoc)
	 * @see org.paxle.filter.blacklist.impl.IBlacklistManager#getLists()
	 */
	public List<String> getLists() {
		return new ArrayList<String>(blacklists.keySet());
	}

	/* (non-Javadoc)
	 * @see org.paxle.filter.blacklist.impl.IBlacklistManager#createList(java.lang.String)
	 */
	public IBlacklist createList(String name) throws InvalidFilenameException {
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

	/* (non-Javadoc)
	 * @see org.paxle.filter.blacklist.impl.IBlacklistManager#getList(java.lang.String)
	 */
	public IBlacklist getList(String name) throws InvalidFilenameException {
		this.validateBlacklistname(name);
		return blacklists.get(name);
	}

	/* (non-Javadoc)
	 * @see org.paxle.filter.blacklist.impl.IBlacklistManager#storeList(org.paxle.filter.blacklist.impl.Blacklist)
	 */
	public void storeList(IBlacklist blacklist) {
		blacklists.put(blacklist.getName(), blacklist);
	}

	/* (non-Javadoc)
	 * @see org.paxle.filter.blacklist.impl.IBlacklistManager#unstoreList(org.paxle.filter.blacklist.impl.Blacklist)
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
