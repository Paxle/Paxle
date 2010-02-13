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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.filter.blacklist.IBlacklist;
import org.paxle.filter.blacklist.IBlacklistStore;
import org.paxle.filter.blacklist.InvalidBlacklistnameException;

/**
 * Implements an @see org.paxle.filter.blacklist.IBlacklistStore based on
 * simple files in a directory.
 * @author Michael Hamann
 *
 */
class BlacklistFileStore implements IBlacklistStore {
	/**
	 * The data directory containing {@link Blacklist}-files.
	 */
	private final File blacklistDir;

	/**
	 * A map containing the {@link Blacklist#name blacklist-name} as key and the {@link Blacklist blacklist}
	 * as value. 
	 */
	private final ConcurrentHashMap<String, IBlacklist> blacklists = new ConcurrentHashMap<String, IBlacklist>();

	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());

	/**
	 * Create a new @see org.paxle.filter.blacklist.IBlacklistStore that uses
	 * a directory for storing the blacklists as files.
	 */
	public BlacklistFileStore() {
		// getting the data directory to use
		final String dataPath = System.getProperty("paxle.data") + File.separatorChar + "blacklist";

		this.blacklistDir = new File(dataPath);

		// create the directory if required
		if (!this.blacklistDir.exists()) this.blacklistDir.mkdirs();

		this.readBlacklists();
	}

	/**
	 * Reads all blacklists from the blacklist directory and instantiates the 
	 * blacklist objects.
	 */
	@SuppressWarnings("unchecked")
	private void readBlacklists() {
		// init the blacklist objects
		Iterator<File> eter = FileUtils.iterateFiles(this.blacklistDir, null, false);
		this.logger.info(String.format(
					"Reading blacklists from directory: %s",
					blacklistDir.toString()
					));

		final StringBuilder blacklistNames = new StringBuilder();
		while (eter.hasNext()) {
			final File blacklistFile = eter.next();
			final String blacklistName = blacklistFile.getName();			
			try {
				final IBlacklist blacklist = new Blacklist(blacklistName, FileUtils.readLines(blacklistFile), this);
				this.blacklists.put(blacklistName, blacklist);
				blacklistNames.append(String.format(
							"\n- '%s' : %d pattern(s)",
							blacklistName,
							blacklist.size()
							));			
			} catch (InvalidBlacklistnameException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.logger.info(String.format(
					"%d blacklist(s) found: %s",
					this.blacklists.size(),
					blacklistNames
					));
	}


	/**
	 * @see org.paxle.filter.blacklist.IBlacklistStore#getBlacklists()
	 */
	public Collection<IBlacklist> getBlacklists() {
		return this.blacklists.values();
	}

	/**
	 * @see org.paxle.filter.blacklist.IBlacklistStore#createBlacklist(String)
	 */
	public IBlacklist createBlacklist(String name) throws InvalidBlacklistnameException {
		this.validateBlacklistname(name);
		return new Blacklist(name, new ArrayList<String>(), this);
	}

	/**
	 * @see org.paxle.filter.blacklist.IBlacklistStore#getBlacklist(String)
	 */
	public IBlacklist getBlacklist(String name) {
		return this.blacklists.get(name);
	}

	/**
	 * @see org.paxle.filter.blacklist.IBlacklistStore#deleteBlacklist(String)
	 */
	public boolean deleteBlacklist(String name) {
		if (this.blacklists.containsKey(name)) {
			final File file = new File(this.blacklistDir.getAbsolutePath() + File.separatorChar + name);
			if (file.delete()) {
				this.blacklists.remove(name);
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * @see org.paxle.filter.blacklist.IBlacklistStore#updateBlacklist(IBlacklist)
	 */
	public boolean updateBlacklist(IBlacklist blacklist) throws InvalidBlacklistnameException {
		this.validateBlacklistname(blacklist.getName());
		final File file = new File(this.blacklistDir.getAbsolutePath() + File.separatorChar + blacklist.getName());
		if (!file.exists()) {
			try {
				FileUtils.touch(file);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		if (file.isFile() && file.canWrite()) {
			try {
				FileUtils.writeLines(file, blacklist.getPatternList());
				this.blacklists.put(blacklist.getName(), blacklist);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
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
	private int offendingChar(final String name) {
		final String others = "+-_.&()=";

		final String nn = name.replace("\\s", "").toLowerCase();
		if (nn.length() == 0)
			return LENGTH_ZERO;

		for (int i=0; i < nn.length(); i++) {
			final char c = nn.charAt(i);
			if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9') || others.indexOf(c) != -1)
				return c;
		}
		return NAME_OK;
	}

	/**
	 * This method checks a given name for attempts of a directory traversal, an empty name and for invalid characters
	 * @throws InvalidBlacklistnameException 
	 */
	private void validateBlacklistname(final String name) throws InvalidBlacklistnameException {
		final int c = this.offendingChar(name);
		switch (c) {
			case NAME_OK: return;
			case LENGTH_ZERO: throw new InvalidBlacklistnameException("The blacklist name is empty.");
			default:
				throw new InvalidBlacklistnameException(
					"The name '" + name + "' is not a valid name for a blacklist. " +
					"Please remove all '" + (char)c + "' characters.");
		}
	}	
}
