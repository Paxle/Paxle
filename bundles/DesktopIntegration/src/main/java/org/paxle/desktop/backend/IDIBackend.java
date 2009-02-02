/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.desktop.backend;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import org.paxle.desktop.backend.desktop.IDesktop;
import org.paxle.desktop.backend.tray.IMenuItem;
import org.paxle.desktop.backend.tray.IPopupMenu;
import org.paxle.desktop.backend.tray.ISystemTray;
import org.paxle.desktop.backend.tray.ITrayIcon;

public interface IDIBackend {
	
	public ISystemTray getSystemTray();
	public IDesktop getDesktop();
	public ITrayIcon createTrayIcon(ImageIcon icon, String tooltip, IPopupMenu popupMenu);
	public IPopupMenu createPopupMenu(IMenuItem... items);
	public IMenuItem createMenuItem(String text, String actionCommand, ActionListener actionListener);
}
