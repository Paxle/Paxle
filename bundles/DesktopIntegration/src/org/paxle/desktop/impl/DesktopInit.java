package org.paxle.desktop.impl;

import java.lang.reflect.Field;

import javax.swing.ImageIcon;

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
	
	public void shutdown() {
		tray.removeTrayIcon(trayIcon);
		trayIcon = null;
		try {
			Class trayService = this.getClass().getClassLoader().loadClass("org.jdesktop.jdic.tray.internal.impl.GnomeSystemTrayService");
			Field thread = trayService.getDeclaredField("display_thread");
			thread.setAccessible(true);			
			Thread threadObj = (Thread) thread.get(null);
			threadObj.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		tray = null;
		context = null;
	}
}
