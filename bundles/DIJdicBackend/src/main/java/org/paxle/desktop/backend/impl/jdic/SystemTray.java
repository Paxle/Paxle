
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
