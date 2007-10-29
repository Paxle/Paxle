/*
 * Created on Mon Oct 29 06:53:43 GMT+01:00 2007
 */
package org.paxle.dbus.impl;


import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.gnome.ScreenSaver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	public static BundleContext bc;	
	
	/**
	 * The connection to the dbus
	 */
	public static DBusConnection conn = null; 
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception { 
		try {  
			// connect to dbus
			conn = DBusConnection.getConnection(DBusConnection.SESSION);  

			// get a reference tot the screensaver object 
			ScreenSaver obj = (ScreenSaver) conn.getRemoteObject("org.gnome.ScreenSaver", "/org/gnome/ScreenSaver");

			// register signal-handlers
			conn.addSigHandler(ScreenSaver.SessionIdleChanged.class, new ScreenSaverMonitor());
			conn.addSigHandler(ScreenSaver.ActiveChanged.class, new ScreenSaverMonitor());  
			
//			System.out.println(obj.GetSessionIdle());
//			obj.Lock();
//			System.err.println("fertig");

		} catch (DBusException De) {
			De.printStackTrace();
			if (conn != null) conn.disconnect();
			throw De;
		}
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext arg0) throws Exception {
		conn.disconnect();
		conn = null;
	}
}