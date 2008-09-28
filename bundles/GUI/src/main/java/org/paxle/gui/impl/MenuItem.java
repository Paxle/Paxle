package org.paxle.gui.impl;

public class MenuItem {
	private String url = null;
	private String name = null;
	private ServletManager sManager;
	
	public MenuItem(ServletManager sManager, String url, String name) {
		this.sManager = sManager;
		this.url = url;
		this.name = name;
	}
	
	public String getUrl() {
		return this.sManager.getFullAlias(this.url);
	}
	
	public String getName() {
		return this.name;
	}
	
	public static MenuItem newInstance(ServletManager sManager, String url, String name) {
		return new MenuItem(sManager, url,name);
	}
	
	@Override
	public int hashCode() {
		return this.url.hashCode();
	}
}
