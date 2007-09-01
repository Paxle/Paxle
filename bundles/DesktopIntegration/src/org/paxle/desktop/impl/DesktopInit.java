package org.paxle.desktop.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import org.osgi.framework.BundleContext;

public class DesktopInit {
	private TrayIcon trayIcon = null;
	private SystemTray tray = null;	
	public static BundleContext context;
	
	public DesktopInit(BundleContext context) {
		DesktopInit.context = context;
		System.out.println(this.getClass().getClassLoader().getClass().getName());
	}
	
	public void init() {
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {				
		try {
				System.out.println("INIT: " + this.getClass().getClassLoader().getClass().getName());
				ImageIcon img = new ImageIcon(context.getBundle().getResource("resources/trayIcon.png"));
				trayIcon = new TrayIcon(img, "Paxle Tray");
				ServiceManager sm = new ServiceManager(context);
				SystrayMenu sym = new SystrayMenu(sm);
				trayIcon.setPopupMenu(sym);
				tray = SystemTray.getDefaultSystemTray();
				tray.addTrayIcon(trayIcon);
		} catch (Exception e) {
			e.printStackTrace();
		}
//			}
//		});		
	}
	
	public static boolean openBrowser(String url) throws MalformedURLException {
		try {
			Desktop.browse(new URL(url));
		} catch (DesktopException e1) {
			final String browserPath = System.getenv("BROWSER");
			if (browserPath == null)
				return false;
			try {
				Runtime.getRuntime().exec(browserPath + " \"" + url + "\"");
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}
	
	public void shutdown() {
		tray.removeTrayIcon(trayIcon);
		trayIcon = null;
		tray = null;
		context = null;
	}
}
