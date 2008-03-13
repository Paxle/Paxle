
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
