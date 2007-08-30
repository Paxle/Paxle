
package org.paxle.desktop.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	public static ServiceManager manager = null;
	private static TrayIcon trayIcon = null;
	private static SystemTray tray = null;
	public static String libPath = null;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		bc = context;
		
		// copy natives into bundle data folder
		this.copyNatives(context);
		
		// setting classloaders
		final ClassLoader cl = this.getClass().getClassLoader();
		Thread.currentThread().setContextClassLoader(cl);
		UIManager.getLookAndFeelDefaults().put("ClassLoader", cl);
		
		// display icon
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				ImageIcon img = new ImageIcon(context.getBundle().getResource("resources/trayIcon.png"));
				trayIcon = new TrayIcon(img, "Paxle Tray");
				ServiceManager sm = new ServiceManager(context);
				SystrayMenu sym = new SystrayMenu(sm);
				trayIcon.setPopupMenu(sym);
				tray = SystemTray.getDefaultSystemTray();
				tray.addTrayIcon(trayIcon);
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		tray.removeTrayIcon(trayIcon);
		trayIcon = null;
		tray = null;
		manager = null;
		bc = null;
	}
	
	private void copyNatives(BundleContext context) throws IOException {
		File libFile = null;
		Enumeration<URL> libs = context.getBundle().findEntries("/resources/libs/","*",false);
		while (libs.hasMoreElements()) {
			
			// open the URL
			URL lib = libs.nextElement();
			InputStream libIn = lib.openStream();
			
			// open a file
			String fileName = lib.getFile();
			int idx = fileName.lastIndexOf("/");
			fileName = fileName.substring(idx+1);			
			libFile = context.getDataFile(fileName);
			
			// copy data
			if (!libFile.exists()) {
				FileOutputStream out = new FileOutputStream(libFile);
				copy(libIn,out);
				out.flush();
				out.close();
			}			
		}
		libPath = libFile.getParentFile().getCanonicalPath();			
	}
	
	static void copy( InputStream in, OutputStream out ) throws IOException { 
		byte[] buffer = new byte[ 0xFFFF ]; 

		for ( int len; (len = in.read(buffer)) != -1; ) 
			out.write( buffer, 0, len ); 
	} 
}
