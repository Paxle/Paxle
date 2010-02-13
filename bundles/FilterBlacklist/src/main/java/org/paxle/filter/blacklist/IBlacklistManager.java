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

import java.util.Collection;

public interface IBlacklistManager {

	/**
	 * Checks if an URL is listed in any enabled blacklist
	 * @param url URL to be checked against blacklists
	 * @return returns a String containing the pattern which blacklists the url, returns null otherwise
	 */
	public IFilterResult isListed(String url);

	/**
	 * Checks if an URL is listed in any specified blacklist
	 * @param url URL to be checked against blacklists
	 * @param enabledLists The list of enabled lists
	 * @return returns a String containing the pattern which blacklists the url, returns null otherwise
	 */
	public IFilterResult isListed(String url, String[] enabledLists);

	/**
	 * Gets all blacklistnames
	 * @return all blacklists
	 */
	public Collection<IBlacklist> getLists();

	/**
	 * Gets all enabled blacklists
	 * @return all enabled blacklists
	 */
	public Collection<IBlacklist> getEnabledLists();

	/**
	 * Gets all enabled blacklists with the specified
	 * @param enabledLists the list of enabled blacklist names
	 * @return all enabled blacklists
	 */
	public Collection<IBlacklist> getEnabledLists(String[] enabledLists);

	/**
	 * creates a blacklist
	 * @param name the name of the blacklist
	 * @return the blacklist that was created, can be null when there is a failure
	 * @throws InvalidBlacklistnameException
	 */
	public IBlacklist createList(String name) throws InvalidBlacklistnameException;

	/**
	 * gets the blacklist
	 * @param name the name of the list
	 * @return the blacklist
	 */
	public IBlacklist getList(String name);
}
