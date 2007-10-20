package org.paxle.gui.impl;

import java.util.ArrayList;
import java.util.List;

import org.paxle.gui.IMenuManager;

public class MenuManager implements IMenuManager {
	private List<MenuItem> items = new ArrayList<MenuItem>();	
	
	public void addItem(String alias, String name) {
		items.add(MenuItem.newInstance(alias, name));
	}
	
	public List<MenuItem> getMenuItemList() {
		return items;
	}
}
