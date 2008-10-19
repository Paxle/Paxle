/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

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
