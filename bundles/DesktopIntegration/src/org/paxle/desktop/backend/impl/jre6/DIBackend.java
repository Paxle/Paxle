
package org.paxle.desktop.backend.impl.jre6;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import org.paxle.desktop.backend.IDIBackend;
import org.paxle.desktop.backend.desktop.IDesktop;
import org.paxle.desktop.backend.impl.jdic.SystemTray;
import org.paxle.desktop.backend.tray.IMenuItem;
import org.paxle.desktop.backend.tray.IPopupMenu;
import org.paxle.desktop.backend.tray.ISystemTray;
import org.paxle.desktop.backend.tray.ITrayIcon;

public class DIBackend implements IDIBackend {
	
	private static IDesktop desktop;
	private static ISystemTray tray;
	
	public IDesktop getDesktop() {
		if (desktop == null)
			desktop = new Desktop();
		return desktop;
	}
	
	public ISystemTray getSystemTray() {
		if (tray == null)
			tray = new SystemTray();
		return tray;
	}
	
	public IMenuItem createMenuItem(String text, String actionCommand, ActionListener actionListener) {
		final IMenuItem mi = new MenuItem();
		mi.init(text, actionCommand, actionListener);
		return mi;
	}
	
	public IPopupMenu createPopupMenu(IMenuItem... items) {
		final IPopupMenu pm = new PopupMenu();
		pm.init(items);
		return pm;
	}
	
	public ITrayIcon createTrayIcon(ImageIcon icon, String tooltip, IPopupMenu popupMenu) {
		return new TrayIcon(icon, tooltip, popupMenu);
	}
}
