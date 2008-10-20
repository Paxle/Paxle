/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.desktop.backend.impl.jre6;

import java.awt.HeadlessException;
import java.awt.MenuShortcut;
import java.awt.event.ActionListener;

import org.paxle.desktop.backend.tray.IMenuItem;

public class MenuItem extends java.awt.MenuItem implements IMenuItem {
	
	private static final long serialVersionUID = 1L;
	
	public MenuItem() throws HeadlessException {
	}
	
	public MenuItem(String label) throws HeadlessException {
		super(label);
	}
	
	public MenuItem(String label, MenuShortcut s) throws HeadlessException {
		super(label, s);
	}
	
	public String getText() {
		return super.getLabel();
	}
	
	public void init(String text, String actionCommand, ActionListener actionListener) {
		super.setLabel(text);
		super.setActionCommand(actionCommand);
		super.addActionListener(actionListener);
	}
	
	public void setText(String text) {
		super.setLabel(text);
	}
}
