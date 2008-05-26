
package org.paxle.desktop.backend.impl.jre6;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.MenuItem;

import javax.swing.event.EventListenerList;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.paxle.desktop.backend.tray.IMenuItem;
import org.paxle.desktop.backend.tray.IPopupMenu;

public class PopupMenu extends java.awt.PopupMenu implements IPopupMenu {
	
	private static final long serialVersionUID = 1L;
	
	private EventListenerList listeners = null;
	
	public PopupMenu() throws HeadlessException {
	}
	
	private final EventListenerList getEventListenerList() {
		if (listeners == null)
			listeners = new EventListenerList();
		return listeners;
	}
	
	@Override
	public void show(Component origin, int x, int y) {
		if (listeners == null) {
			super.show(origin, x, y);
		} else {
			final PopupMenuEvent event = new PopupMenuEvent(this);
			for (PopupMenuListener l : listeners.getListeners(PopupMenuListener.class))
				l.popupMenuWillBecomeVisible(event);
			
			super.show(origin, x, y);
			
			for (PopupMenuListener l : listeners.getListeners(PopupMenuListener.class))
				l.popupMenuWillBecomeInvisible(event);
		}
	}
	
	public void addPopupMenuListener(final PopupMenuListener l) {
		getEventListenerList().add(PopupMenuListener.class, l);
	}
	
	public PopupMenu(String label) throws HeadlessException {
		super(label);
	}
	
	public void add(IMenuItem menuItem) {
		super.add((MenuItem)menuItem);
	}
	
	@Override
	public void addSeparator() {
		super.addSeparator();
	}
	
	public void insert(IMenuItem menuItem, int index) {
		super.insert((MenuItem)menuItem, index);
	}
	
	@Override
	public void insertSeparator(int index) {
		super.insertSeparator(index);
	}
	
	public void init(String label) {
		super.setLabel(label);
	}
	
	public void remove(IMenuItem menuItem) {
		super.remove((MenuItem)menuItem);
	}
	
	@Override
	public void remove(int index) {
		super.remove(index);
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
