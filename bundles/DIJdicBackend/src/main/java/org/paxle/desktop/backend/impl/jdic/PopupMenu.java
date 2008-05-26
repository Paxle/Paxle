
package org.paxle.desktop.backend.impl.jdic;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.paxle.desktop.backend.tray.IMenuItem;
import org.paxle.desktop.backend.tray.IPopupMenu;

public class PopupMenu extends JPopupMenu implements IPopupMenu {
	
	private static final long serialVersionUID = 1L;
	
	public PopupMenu() {
	}
	
	public PopupMenu(String label) {
		super(label);
	}
	
	public void add(IMenuItem menuItem) {
		super.add((JMenuItem)menuItem);
	}
	
	public void insert(IMenuItem menuItem, int index) {
		super.insert((JMenuItem)menuItem, index);
	}
	
	public void insertSeparator(int index) {
		super.insert(new Separator(), index);
	}
	
	@Override
	public void remove(int pos) {
		super.remove(pos);
	}
	
	public void remove(IMenuItem menuItem) {
		super.remove((JMenuItem)menuItem);
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
