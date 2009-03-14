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

import java.util.Collection;

public interface IBlacklistStore {
	/**
	 * Get a list of all available blacklists.
	 * @return The blacklists.
	 */
	Collection<IBlacklist> getBlacklists();

	/**
	 * Create a new blacklist with the given name.
	 * @param name The name of the blacklist.
	 * @return The new blacklist or <code>null</code> if there was a failure.
	 * @throws InvalidBlacklistnameException
	 */
	IBlacklist createBlacklist(String name) throws InvalidBlacklistnameException;

	/**
	 * Get the blacklist with the given name.
	 * @param name The name of the blacklist.
	 * @return The blacklist or <code>null</code> if it doesn't exist.
	 */
	IBlacklist getBlacklist(String name);

	/**
	 * Delete the blacklist with the given name.
	 * @param name The name of the blacklist.
	 * @return If the deletion was successful.
	 */
	boolean deleteBlacklist(String name);

	/**
	 * Update the stored version of the given blacklist.
	 * @param blacklist The blacklist to update.
	 * @return If the update was successful.
	 * @throws InvalidBlacklistnameException
	 */
	boolean updateBlacklist(IBlacklist blacklist) throws InvalidBlacklistnameException;
}
