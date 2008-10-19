/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

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
