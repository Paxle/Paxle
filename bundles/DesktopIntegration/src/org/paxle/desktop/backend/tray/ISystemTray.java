
package org.paxle.desktop.backend.tray;

public interface ISystemTray {
	
	public boolean isSupported();
	public void add(ITrayIcon trayIcon);
	public void remove(ITrayIcon trayIcon);
	public ITrayIcon[] getTrayIcons();
}
