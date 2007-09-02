package org.paxle.desktop.backend.impl.jre6;

import java.awt.HeadlessException;
import java.awt.MenuItem;

import org.paxle.desktop.backend.tray.IMenuItem;
import org.paxle.desktop.backend.tray.IPopupMenu;

public class PopupMenu extends java.awt.PopupMenu implements IPopupMenu {
	
	private static final long serialVersionUID = 1L;
	
	public PopupMenu() throws HeadlessException {
	}
	
	public PopupMenu(String label) throws HeadlessException {
		super(label);
	}
	
	public void add(IMenuItem menuItem) {
		super.add((MenuItem)menuItem);
	}
	
	public void init(String label) {
		super.setLabel(label);
	}
	
	public void remove(IMenuItem menuItem) {
		super.remove((MenuItem)menuItem);
	}
	
	public void init(IMenuItem... items) {
		for (final IMenuItem item : items)
			if (item == null) {
				addSeparator();
			} else {
				add(item);
			}
	}
}
