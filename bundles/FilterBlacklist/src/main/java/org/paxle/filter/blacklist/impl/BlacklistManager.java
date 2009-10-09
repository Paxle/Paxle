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
import java.util.ArrayList;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.paxle.filter.blacklist.IBlacklist;
import org.paxle.filter.blacklist.IBlacklistStore;
import org.paxle.filter.blacklist.IBlacklistManager;
import org.paxle.filter.blacklist.IFilterResult;
import org.paxle.filter.blacklist.InvalidBlacklistnameException;

@Component(immediate=true)
@Service(IBlacklistManager.class)
public class BlacklistManager implements IBlacklistManager {

	/**
	 * The store of the blacklists.
	 */
	private IBlacklistStore blacklistStore;

	/**
	 * The list of enabled blacklists.
	 */
	private String[] enabledBlacklistNames;

	/**
	 * This function is called by the OSGi framework if this component is activated
	 */
	protected void activate(Map<String, Object> props) throws InvalidBlacklistnameException {
		this.blacklistStore = new BlacklistFileStore();
		if (props != null)
			this.enabledBlacklistNames = (String[]) props.get("enabledBlacklistNames");
		else
			this.enabledBlacklistNames = null;
	}

	protected void deactivate() throws Exception {
		// clear blacklist map
		this.blacklistStore = null;
		this.enabledBlacklistNames = null;
	}

	/* (non-Javadoc)
	 * @see org.paxle.filter.blacklist.impl.IBlacklistManager#isListed(java.lang.String)
	 */
	public IFilterResult isListed(String url) {
		return this.isListed(url, this.enabledBlacklistNames);
	}

	/**
	 * @see org.paxle.filter.blacklist.IBlacklistManager#isListed(String, String[])
	 */
	public IFilterResult isListed(String url, String[] enabledBlacklistNames) {
		for (IBlacklist blacklist : this.getEnabledLists(enabledBlacklistNames)) {
			IFilterResult result = blacklist.isListed(url);
			if (result.getStatus() == IFilterResult.LOCATION_REJECTED)
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

	/**
	 * @see org.paxle.filter.blacklist.IBlacklistManager#getEnabledLists()
	 */
	public Collection<IBlacklist> getEnabledLists() {
		if (this.enabledBlacklistNames == null) {
			return this.getLists();
		} else {
			return this.getEnabledLists(this.enabledBlacklistNames);
		}
	}

	/**
	 * @see org.paxle.filter.blacklist.IBlacklistManager#getEnabledLists(String[])
	 */
	public Collection<IBlacklist> getEnabledLists(String[] enabledBlacklistNames) {
		if (enabledBlacklistNames == null) {
			return this.getEnabledLists();
		} else {
			ArrayList<IBlacklist> enabledLists = new ArrayList<IBlacklist>(enabledBlacklistNames.length);
			for (String name : enabledBlacklistNames) {
				IBlacklist list = this.getList(name);
				if (list != null)
					enabledLists.add(list);
			}
			return enabledLists;
		}
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
	public IBlacklist getList(String name) {
		return this.blacklistStore.getBlacklist(name);
	}
}
