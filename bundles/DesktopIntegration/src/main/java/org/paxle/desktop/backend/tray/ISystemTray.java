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
package org.paxle.desktop.backend.tray;

public interface ISystemTray {
	
	/**
	 * Not every system Java runs on supports system-trays or even has a graphical user interface.
	 * This method checks for all pre-requisites needed to determine system support for the tray.
	 * @return whether the system-tray is supported by the system or not
	 */
	public boolean isSupported();
	
	/**
	 * Adds a {@link ITrayIcon tray-icon} to the system-tray.
	 * @see org.paxle.desktop.backend.IDIBackend#createTrayIcon(javax.swing.ImageIcon, String, IPopupMenu)
	 * @param trayIcon the tray-icon to add to the system-tray
	 */
	public void add(ITrayIcon trayIcon);
	
	/**
	 * Remove a {@link ITrayIcon tray-icon} from the system-tray.
	 * @param trayIcon the tray-icon to remove from the system-tray
	 */
	public void remove(ITrayIcon trayIcon);
	
	/**
	 * @return all {@link ITrayIcon}s in this VM's system-tray that are currently registered using the backend
	 *         implementing this interface
	 */
	public ITrayIcon[] getTrayIcons();
}
