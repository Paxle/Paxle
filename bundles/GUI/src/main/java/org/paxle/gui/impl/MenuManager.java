package org.paxle.gui.impl;

import java.util.ArrayList;
import java.util.List;

import org.paxle.gui.IMenuManager;

public class MenuManager implements IMenuManager {
	private List<MenuItem> items = new ArrayList<MenuItem>();	
	
	public void addItem(MenuItem item) {
		items.add(item);
	}
	
	public void addItem(String url, String name) {
		items.add(MenuItem.newInstance(url, name));
	}
	
	public List<MenuItem> getMenuItemList() {
		return items;
	}
}
