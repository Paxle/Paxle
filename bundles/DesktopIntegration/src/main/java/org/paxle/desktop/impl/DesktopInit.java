
package org.paxle.desktop.impl;

import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

import org.osgi.framework.BundleContext;

public class DesktopInit {
	private static String mode = "jdic";
	private static final String MODE_JDIC = "jdic";
	private static final String MODE_JDK6 = "jre6";
	
	private static final String SYSTRAY = "SystemTray";
	private static final String DESKTOP = "Desktop";
	private static final String TRAYICON = "TrayIcon";
	private static final String GETSYSTRAY = "getSysTray";
	private static final String ADDTRAYICON = "addTrayIcon";
	private static final String RMTRAYICON = "removeTrayIcon";
	private static final String BROWSE = "browse";
	
	private static final HashMap<String, String> CONSTANTS = new HashMap<String, String>();
	static {
		// SysTray Classes
		CONSTANTS.put(MODE_JDIC + "." + SYSTRAY, "org.jdesktop.jdic.tray.SystemTray");
		CONSTANTS.put(MODE_JDK6 + "." + SYSTRAY, "java.awt.SystemTray");
		
		// Desktop Classes
		CONSTANTS.put(MODE_JDIC + "." + DESKTOP, "org.jdesktop.jdic.desktop.Desktop");
		CONSTANTS.put(MODE_JDK6 + "." + DESKTOP, "java.awt.Desktop");
		
		// Systray Icon Classes
		CONSTANTS.put(MODE_JDIC + "." + TRAYICON, "org.jdesktop.jdic.tray.TrayIcon");
		CONSTANTS.put(MODE_JDK6 + "." + TRAYICON, "java.awt.TrayIcon");		
		
		// Systray method definitions
		CONSTANTS.put(MODE_JDIC + "." + SYSTRAY + "." + GETSYSTRAY, "getDefaultSystemTray");
		CONSTANTS.put(MODE_JDK6 + "." + SYSTRAY + "." + GETSYSTRAY, "getSystemTray");		
		CONSTANTS.put(MODE_JDIC + "." + SYSTRAY + "." + ADDTRAYICON, "addTrayIcon");
		CONSTANTS.put(MODE_JDK6 + "." + SYSTRAY + "." + ADDTRAYICON, "add");	
		CONSTANTS.put(MODE_JDIC + "." + SYSTRAY + "." + RMTRAYICON, "removeTrayIcon");
		CONSTANTS.put(MODE_JDK6 + "." + SYSTRAY + "." + RMTRAYICON, "remove");		
		
		// desktop methods
		CONSTANTS.put(MODE_JDIC + "." + DESKTOP + "." + BROWSE, "browse");
		CONSTANTS.put(MODE_JDK6 + "." + DESKTOP + "." + BROWSE, "browse");		
	}
	
	private Object trayIcon = null;
	private Object tray = null;
	private static ClassLoader cl = DesktopInit.class.getClassLoader();
	public static BundleContext context;
	
	public DesktopInit(BundleContext context, String displayMode) {
		mode = displayMode;
		DesktopInit.context = context;
		System.out.println(this.getClass().getClassLoader().getClass().getName());
	}
	
	public void init() {			
		try {
			// init servicemanager
			ServiceManager sm = new ServiceManager(context);

			// init the menu
			Object sym = this.newSysTrayMenu(sm);		

			// load the image to use
			Object img = this.newImage(context.getBundle().getResource("resources/trayIcon.png"));

			// create the tray icon
			trayIcon = this.newTrayIcon(img, "Paxle Tray", sym);

			// get the systray
			tray = this.getSysTray();
			this.addTrayIcon(trayIcon);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	private static Class<?> getClass(String mode, String clazzName) throws Exception {
		String fullClazzName = CONSTANTS.get(mode + "." + clazzName);
		return cl.loadClass(fullClazzName);
	}
	
	private static Method getMethod(String mode, String className, String methodName, Class<?> ... clazzes) throws Exception {
		String realMethodName = CONSTANTS.get(mode + "." + className + "." + methodName);
		return getClass(mode,className).getMethod(realMethodName, clazzes);
	}
	
	private Object newImage(URL imageURL) {
		return (mode.equals(MODE_JDIC))
			? new ImageIcon(imageURL)
			: Toolkit.getDefaultToolkit().getImage(imageURL);		
	}
	
	private Object newTrayIcon(Object icon, String caption, Object popup) throws Exception {
		return getClass(mode,TRAYICON)
			.getConstructor(new Class[]{
					(mode.equals(MODE_JDIC))?Icon.class:Image.class,
					String.class,
					(mode.equals(MODE_JDIC))?JPopupMenu.class:PopupMenu.class,
			}).newInstance(new Object[]{icon,caption,popup});
	}
	
	private Object newSysTrayMenu(ServiceManager sm) {
		if (mode.equals(MODE_JDIC)) {
			return new SystrayMenu(sm);
		} else {
			// TODO
			return new PopupMenu("Test");
		}		
	}
	
	private Object getSysTray() throws Exception {
		return getMethod(mode, SYSTRAY, GETSYSTRAY)
			.invoke(null, (Object[])null);
	}
	
	private void addTrayIcon(Object trayIcon) throws Exception {
		getMethod(mode, SYSTRAY, ADDTRAYICON, getClass(mode, TRAYICON))
			.invoke(this.tray, trayIcon);
	}
	
	private void removeTrayIcon(Object trayIcon) throws Exception {
		getMethod(mode, SYSTRAY, RMTRAYICON, getClass(mode, TRAYICON))
			.invoke(this.tray, trayIcon);			
	}
	
	public static boolean openBrowser(String url) throws MalformedURLException {
		try {
			getMethod(mode, DESKTOP, BROWSE, URL.class).invoke(null, new URL(url));
		} catch (Exception e1) {
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
	
	public void shutdown() throws Exception {
		this.removeTrayIcon(trayIcon);
		trayIcon = null;
		tray = null;
		context = null;
	}
}
