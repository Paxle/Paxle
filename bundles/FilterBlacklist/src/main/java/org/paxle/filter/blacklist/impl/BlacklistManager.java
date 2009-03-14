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

import java.util.Collection;
import java.util.Iterator;

import org.osgi.service.component.ComponentContext;
import org.paxle.filter.blacklist.IBlacklist;
import org.paxle.filter.blacklist.IBlacklistStore;
import org.paxle.filter.blacklist.IBlacklistManager;
import org.paxle.filter.blacklist.IFilterResult;
import org.paxle.filter.blacklist.InvalidBlacklistnameException;

/**
 * @scr.component immediate="true"
 * @scr.service interface="org.paxle.filter.blacklist.IBlacklistManager"
 */
public class BlacklistManager implements IBlacklistManager {

	/**
	 * The store of the blacklists.
	 */
	private IBlacklistStore blacklistStore;

	/**
	 * This function is called by the OSGi framework if this component is activated
	 */
	protected void activate(ComponentContext context) throws InvalidBlacklistnameException {
		this.blacklistStore = new BlacklistFileStore();
	}

	protected void deactivate(ComponentContext context) throws Exception {
		// clear blacklist map
		this.blacklistStore = null;
	}

	/* (non-Javadoc)
	 * @see org.paxle.filter.blacklist.impl.IBlacklistManager#isListed(java.lang.String)
	 */
	public IFilterResult isListed(String url) {
		Iterator<IBlacklist> allLists = this.blacklistStore.getBlacklists().iterator();
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
	public Collection<IBlacklist> getLists() {
		return this.blacklistStore.getBlacklists();
	}

	/* (non-Javadoc)
	 * @see org.paxle.filter.blacklist.impl.IBlacklistManager#createList(java.lang.String)
	 */
	public IBlacklist createList(String name) throws InvalidBlacklistnameException {
		return this.blacklistStore.createBlacklist(name);
	}

	/* (non-Javadoc)
	 * @see org.paxle.filter.blacklist.impl.IBlacklistManager#getList(java.lang.String)
	 */
	public IBlacklist getList(String name) throws InvalidBlacklistnameException {
		return this.blacklistStore.getBlacklist(name);
	}
}
