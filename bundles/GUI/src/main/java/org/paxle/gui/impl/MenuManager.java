package org.paxle.gui.impl;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.paxle.gui.IMenuManager;

public class MenuManager implements IMenuManager {
	private LinkedHashMap<String,MenuItem> items = new LinkedHashMap<String,MenuItem>();	
	
	public void addItem(MenuItem item) {
		items.put(item.getUrl(),item);
	}
	
	public void addItem(String url, String name) {
		items.put(url,MenuItem.newInstance(url, name));
	}
	
	public Collection<MenuItem> getMenuItemList() {
		return items.values();
	}
	
	public void removeItem(String url) {
		this.items.remove(url);
	}
}
