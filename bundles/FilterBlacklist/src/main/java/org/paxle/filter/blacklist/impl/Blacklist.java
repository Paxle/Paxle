/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a RegExp-based Blacklist
 * @author Matthias Soehnholz
 * @author Michael Hamann
 *
 */
public class Blacklist {
	private File listFile;
	private BlacklistFilter blacklistFilter;
	private ConcurrentHashMap<String,Pattern> blacklist;
	public String name;
	private Log logger = LogFactory.getLog(this.getClass());

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * New blacklist object for existing blacklistfile
	 * @param name the name of the blacklist
	 * @param listFile the file to read or store the blacklist
	 * @param blacklistFilter the BlacklistFilter-object
	 */
	public Blacklist(String name, File listFile, BlacklistFilter blacklistFilter) throws InvalidFilenameException {
		this.name = name;
		this.listFile = listFile;
		this.blacklistFilter = blacklistFilter;
		if (!(listFile.canRead() && listFile.canWrite())) throw new IllegalArgumentException("Unknown blacklist.");
		// check for uniqueness of this object
		if (this.blacklistFilter.getList(this.name) != null) throw new IllegalArgumentException("Blacklist-object does already exist!");
		boolean incorrectPattern = false;
		try {
			blacklist = new ConcurrentHashMap<String,Pattern>();
			Iterator<?> patternIterator = FileUtils.readLines(this.listFile).iterator();
			while (patternIterator.hasNext()) {
				String pattern = (String) patternIterator.next();
				try {
					blacklist.put(pattern, Pattern.compile(pattern));
				} catch (PatternSyntaxException e) {
					logger.warn("Invalid blacklistpattern " + pattern + " in file " + name + ", it will be ignored and a version without this pattern will be saved");
					incorrectPattern = true;
					e.printStackTrace();
				}
			}
			this.store();
			if (incorrectPattern) // store a version of the list that does not contain invalid patterns anymore
				FileUtils.writeLines(this.listFile, this.getPatternList());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Deletes the blacklist
	 * @return boolean if delete was successful
	 */
	public boolean destroy() {
		lock.writeLock().lock();
		if (listFile.delete()) {
			this.unstore();
			this.blacklist.clear();
			lock.writeLock().unlock();
			return true;
		} else {
			lock.writeLock().unlock();
			return false;
		}
	}

	/**
	 * store this blacklist so that it can be derived using getList
	 */
	private void store() {
		blacklistFilter.storeList(this);
	}

	/**
	 * remove this blacklist from the store so that it can be longer accessed
	 * please note that this does not delete the blacklist
	 */
	private void unstore() {
		blacklistFilter.unstoreList(this);
	}

	/**
	 * The name of the blacklist
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 
	 * @param url URL to be checked against blacklist
	 * @return returns a String containing the pattern which blacklists the url, returns null otherwise
	 */
	public FilterResult isListed(String url) {
		long time = System.currentTimeMillis();
		Enumeration<Pattern> eter = blacklist.elements();
		while(eter.hasMoreElements()) {
			Pattern temp = eter.nextElement();
			Matcher m = temp.matcher(url);
			if(m.matches()) {
				time = System.currentTimeMillis() - time;
				//System.out.println("Duration in 'isListed()' for blacklistcheck: "+ time + " ms . URL: " + url);
				logger.debug("Duration in 'isListed()' for blacklistcheck: "+ time + " ms . URL: " + url);
				return new FilterResult(FilterResult.LOCATION_REJECTED, temp.pattern());
			}
		}
		time = System.currentTimeMillis() - time;
		//System.out.println("Duration in 'isListed()' for blacklistcheck: "+ time + " ms . URL: " + url);
		logger.debug("Duration in 'isListed()' for blacklistcheck: "+ time + " ms . URL: " + url);
		return FilterResult.LOCATION_OKAY_RESULT;
	}

	/**
	 * Returns all entries in the list as Strings
	 * @return
	 */
	public List<String> getPatternList() {
		return new ArrayList<String>(blacklist.keySet());
	}

	/**
	 * Adds a new blacklist-pattern to the selected blacklistfile
	 * @param pattern blacklistpattern to be added 
	 * @return if the blacklistpattern was successfully added
	 */
	public boolean addPattern(String pattern) {
		lock.writeLock().lock();
		try {
			Pattern p = Pattern.compile(pattern);
			blacklist.put(pattern, p);
			//System.out.println("Pattern from "+listFileName+" added to blacklist: "+pattern);
			FileWriter listWriter = new FileWriter(this.listFile, true);
			listWriter.write(pattern + "\n");
			listWriter.close();
			this.store(); // The blacklist might have been destroyed
			return true;
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes the pattern from the blacklistfile
	 * @param pattern blacklistpattern to be removed
	 * @return if the pattern was successfully removed, note that it is even true when the pattern wasn't included in the list
	 */
	public boolean removePattern(String pattern) {
		lock.writeLock().lock();
		try {
			blacklist.remove(pattern);
			FileUtils.writeLines(this.listFile, this.getPatternList());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Edits a pattern of the blacklistfile
	 * @param fromPattern the blacklistpattern that shall be edited
	 * @param toPattern the new value of the blacklistpattern
	 * @return if the blacklistpattern was successfully edited
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
}
