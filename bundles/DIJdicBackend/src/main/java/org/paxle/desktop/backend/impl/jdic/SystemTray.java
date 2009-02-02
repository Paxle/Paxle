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
package org.paxle.desktop.backend.impl.jdic;

import java.util.HashSet;

import org.jdesktop.jdic.tray.TrayIcon;
import org.paxle.desktop.backend.tray.ISystemTray;
import org.paxle.desktop.backend.tray.ITrayIcon;

public class SystemTray implements ISystemTray {
	
	private static org.jdesktop.jdic.tray.SystemTray tray = null;
	private static HashSet<ITrayIcon> icons = new HashSet<ITrayIcon>();
	
	public SystemTray() {
		SystemTray.tray = org.jdesktop.jdic.tray.SystemTray.getDefaultSystemTray();
	}
	
	public void add(ITrayIcon trayIcon) {
		tray.addTrayIcon((TrayIcon)trayIcon);
		icons.add(trayIcon);
	}
	
	public ITrayIcon[] getTrayIcons() {
		return icons.toArray(new ITrayIcon[icons.size()]);
	}
	
	public boolean isSupported() {
		// XXX where is the method in jdic?
		return true;
	}
	
	public void remove(ITrayIcon trayIcon) {
		tray.removeTrayIcon((TrayIcon)trayIcon);
		icons.remove(trayIcon);
	}
}
