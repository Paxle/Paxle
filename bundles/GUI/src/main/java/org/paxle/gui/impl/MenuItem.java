package org.paxle.gui.impl;

public class MenuItem {
	private String url = null;
	private String name = null;
	
	public MenuItem(String url, String name) {
		this.url = url;
		this.name = name;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public String getName() {
		return this.name;
	}
	
	public static MenuItem newInstance(String url, String name) {
		return new MenuItem(url,name);
	}
	
	@Override
	public int hashCode() {
		return this.url.hashCode();
	}
}
