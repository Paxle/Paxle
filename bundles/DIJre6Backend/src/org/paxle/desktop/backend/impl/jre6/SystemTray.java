
package org.paxle.desktop.backend.impl.jre6;

import java.awt.AWTException;
import java.awt.TrayIcon;

import org.paxle.desktop.backend.tray.ISystemTray;
import org.paxle.desktop.backend.tray.ITrayIcon;

public class SystemTray implements ISystemTray {
	
	private static java.awt.SystemTray systemTray = null;
	
	public SystemTray() {
		SystemTray.systemTray = java.awt.SystemTray.getSystemTray();
	}
	
	public void add(ITrayIcon trayIcon) {
		try {
			systemTray.add((TrayIcon)trayIcon);
		} catch (AWTException e) { e.printStackTrace(); }
	}
	
	public ITrayIcon[] getTrayIcons() {
		return (ITrayIcon[])systemTray.getTrayIcons();
	}
	
	public boolean isSupported() {
		return java.awt.SystemTray.isSupported();
	}
	
	public void remove(ITrayIcon trayIcon) {
		systemTray.remove((TrayIcon)trayIcon);
	}
}
