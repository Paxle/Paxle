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
package org.paxle.filter.blacklist;

import java.util.List;

import org.paxle.filter.blacklist.impl.Blacklist;

public interface IBlacklistManager {

	/**
	 * Checks if an URL is listed in any blacklist
	 * @param url URL to be checked against blacklists
	 * @return returns a String containing the pattern which blacklists the url, returns null otherwise
	 */
	public IFilterResult isListed(String url);

	/**
	 * Gets all blacklistnames
	 * @return all blacklistnames
	 */
	public List<String> getLists();

	/**
	 * creates a blacklist
	 * @param name the name of the blacklist
	 * @return the blacklist that was created, can be null when there is a failure
	 * @throws InvalidFilenameException 
	 */
	public IBlacklist createList(String name) throws InvalidFilenameException;

	/**
	 * gets the blacklist
	 * @param name the name of the list
	 * @return the blacklist
	 * @throws InvalidFilenameException 
	 */
	public IBlacklist getList(String name) throws InvalidFilenameException;

	/**
	 * store the blacklist so that it can be derived using getList
	 * @param blacklist the list to be stored
	 */
	public void storeList(IBlacklist blacklist);

	/**
	 * remove the blacklist from the store so that it can be longer accessed
	 * please note that this does not delete the blacklist
	 * @param blacklist the list to be unstored
	 */
	public void unstoreList(Blacklist blacklist);

}