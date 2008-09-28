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
