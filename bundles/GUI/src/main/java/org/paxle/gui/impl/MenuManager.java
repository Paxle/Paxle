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

package org.paxle.gui.impl;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.paxle.gui.IMenuManager;

public class MenuManager implements IMenuManager {
	/**
	 * A map of all currently registeres {@link MenuItem menu-items}. 
	 */
	private LinkedHashMap<String,MenuItem> items = new LinkedHashMap<String,MenuItem>();	
	
	private final ServletManager sManager;
	
	public MenuManager(ServletManager sManager) {
		this.sManager = sManager;
	}
	
	public void addItem(String url, String name) {
		items.put(url,MenuItem.newInstance(this.sManager, url, name));
	}
	
	public Collection<MenuItem> getMenuItemList() {
		return items.values();
	}
	
	public void removeItem(String url) {
		this.items.remove(url);
	}
}
