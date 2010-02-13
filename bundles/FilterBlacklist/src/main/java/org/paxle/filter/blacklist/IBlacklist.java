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

package org.paxle.filter.blacklist;

import java.util.List;


public interface IBlacklist {

	/**
	 * Deletes the blacklist
	 * @return boolean if delete was successful
	 */
	public boolean delete();

	/**
	 * The name of the blacklist
	 * @return
	 */
	public String getName();

	/**
	 * 
	 * @param url URL to be checked against blacklist
	 * @return returns a String containing the pattern which blacklists the url, returns null otherwise
	 */
	public IFilterResult isListed(String url);

	/**
	 * Returns all entries in the list as Strings
	 * @return
	 */
	public List<String> getPatternList();

	/**
	 * Adds a new blacklist-pattern to the selected blacklistfile
	 * @param pattern blacklistpattern to be added 
	 * @return if the blacklistpattern was successfully added
	 */
	public boolean addPattern(String pattern);

	/**
	 * Removes the pattern from the blacklistfile
	 * @param pattern blacklistpattern to be removed
	 * @return if the pattern was successfully removed, note that it is even true when the pattern wasn't included in the list
	 */
	public boolean removePattern(String pattern);

	/**
	 * Edits a pattern of the blacklistfile
	 * @param fromPattern the blacklistpattern that shall be edited
	 * @param toPattern the new value of the blacklistpattern
	 * @return if the blacklistpattern was successfully edited
	 */
	public boolean editPattern(String fromPattern, String toPattern);
	
	/**
	 * @return the amount of patterns stored in this blacklist
	 */
	public int size();
}