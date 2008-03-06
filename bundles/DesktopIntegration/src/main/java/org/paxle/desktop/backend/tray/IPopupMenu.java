package org.paxle.desktop.backend.tray;

public interface IPopupMenu {
	
	public void add(IMenuItem menuItem);
	public void addSeparator();
	public void remove(IMenuItem menuItem);
	public void init(IMenuItem... items);
}
