
package org.paxle.desktop.impl;

import javax.swing.ImageIcon;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	public static ServiceManager manager = null;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bc = context;
		manager = new ServiceManager(bc);
		
		final TrayIcon trayIcon = new TrayIcon(new ImageIcon("resources/trayIcon.png"), "Paxle Tray");
		final SystemTray tray = SystemTray.getDefaultSystemTray();
		tray.addTrayIcon(trayIcon);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		manager = null;
		bc = null;
	}
}
