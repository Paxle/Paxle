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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.filter.blacklist.IBlacklist;
import org.paxle.filter.blacklist.IBlacklistStore;
import org.paxle.filter.blacklist.IFilterResult;
import org.paxle.filter.blacklist.InvalidBlacklistnameException;

/**
 * This is a RegExp-based Blacklist
 * @author Matthias Soehnholz
 * @author Michael Hamann
 *
 */
public class Blacklist implements IBlacklist {
	private IBlacklistStore blacklistStore;
	private ConcurrentHashMap<String, Pattern> blacklist;
	private final String name;
	private Log logger = LogFactory.getLog(this.getClass());

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * New blacklist object.
	 * @param name the name of the blacklist
	 * @param patterns the patterns the list consists of
	 * @param blacklistStore the store for the blacklists
	 * @throws InvalidBlacklistnameException
	 */
	public Blacklist(String name, Collection<String> patterns, IBlacklistStore blacklistStore) throws InvalidBlacklistnameException {
		this.name = name;
		this.blacklistStore = blacklistStore;
		// check for uniqueness of this object
		if (this.blacklistStore.getBlacklist(this.name) != null) {
		    throw new IllegalArgumentException("Blacklist-object does already exist!");
		}
		Iterator<String> patternIterator = patterns.iterator();
		blacklist = new ConcurrentHashMap<String, Pattern>();
		while (patternIterator.hasNext()) {
			String pattern = patternIterator.next();
			try {
				blacklist.put(pattern, Pattern.compile(pattern));
			} catch (PatternSyntaxException e) {
				logger.warn("Invalid blacklistpattern " + pattern + " in file " + name + ", it will be ignored and a version without this pattern will be saved");
				e.printStackTrace();
			}
		}
		this.blacklistStore.updateBlacklist(this);
	}


	/**
	 * @see org.paxle.filter.blacklist.IBlacklist#delete()
	 */
	public boolean delete() {
		lock.writeLock().lock();
		try {
			if (this.blacklistStore.deleteBlacklist(this.name)) {
				this.blacklist.clear();
				return true;
			} else {
				return false;
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * store this blacklist so that it can be derived using getList.
	 * @return If the update was successful and no exception was thrown.
	 */
	private boolean store() {
		try {
			return blacklistStore.updateBlacklist(this);
		} catch (InvalidBlacklistnameException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @see org.paxle.filter.blacklist.IBlacklist#getName()
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @see org.paxle.filter.blacklist.IBlacklist#isListed(String)
	 */
	public IFilterResult isListed(String url) {
		long time = System.currentTimeMillis();
		Enumeration<Pattern> eter = blacklist.elements();
		while (eter.hasMoreElements()) {
			Pattern temp = eter.nextElement();
			Matcher m = temp.matcher(url);
			if (m.matches()) {
				if (logger.isDebugEnabled()) {
					time = System.currentTimeMillis() - time;
					logger.debug("Duration in 'isListed()' for blacklistcheck: " + time + " ms . URL: " + url);
				}
				return new FilterResult(IFilterResult.LOCATION_REJECTED, temp.pattern());
			}
		}
		if (logger.isDebugEnabled()) {
			time = System.currentTimeMillis() - time;
			logger.debug("Duration in 'isListed()' for blacklistcheck: "+ time + " ms . URL: " + url);
		}
		return FilterResult.LOCATION_OKAY_RESULT;
	}

	/**
	 * @see org.paxle.filter.blacklist.IBlacklist#getPatternList()
	 */
	public List<String> getPatternList() {
		return new ArrayList<String>(blacklist.keySet());
	}

	/**
	 * @see org.paxle.filter.blacklist.IBlacklist#addPattern(String)
	 */
	public boolean addPattern(String pattern) {
		lock.writeLock().lock();
		try {
			Pattern p = Pattern.compile(pattern);
			blacklist.put(pattern, p);
			//System.out.println("Pattern from "+listFileName+" added to blacklist: "+pattern);
			return this.store(); // Update the blacklist store
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
			return false;
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * @see org.paxle.filter.blacklist.IBlacklist#removePattern(String)
	 */
	public boolean removePattern(String pattern) {
		lock.writeLock().lock();
		try {
			blacklist.remove(pattern);
			return this.store();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * @see org.paxle.filter.blacklist.IBlacklist#editPattern(String, String)
	 */
	public boolean editPattern(String fromPattern, String toPattern) {
		try {
			Pattern.compile(toPattern);
			return (this.removePattern(fromPattern) && this.addPattern(toPattern));
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
			return false;
		}

	}
	
	/**
	 * @see org.paxle.filter.blacklist.IBlacklist#size()
	 */
	public int size() {
		return this.blacklist == null ? 0 : this.blacklist.size();
	}
}
