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
