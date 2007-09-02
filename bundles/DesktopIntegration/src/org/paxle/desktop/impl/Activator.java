
package org.paxle.desktop.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;

import org.jdesktop.jdic.init.JdicManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.io.IOTools;

/**
 * To get this to work you need to set the variable
 * org.osgi.framework.system.packages=sun.awt,javax.swing,javax.swing.event,sun.awt.motif,sun.awt.X11,javax.swing.plaf.metal,javax.swing.plaf.basic
 *
 */
public class Activator implements BundleActivator {
	
	public static final float NATIVE_JRE_SUPPORT = 1.6f;
	
	private enum Implementations {
		jdic("org.paxle.desktop.backend.impl.jdic"),
		jre6("org.paxle.desktop.backend.impl.jre6")
		
		;
		
		public final String packageName;
		
		private Implementations(String packageName) {
			this.packageName = packageName;
		}
	}
	
	public static Implementations mode = null;
	public static BundleContext bc = null;
	public static ServiceManager manager = null;
	public static String libPath = null;
	
	public Method initMethod = null;
	public Method shutdownMethod = null;
	public Object initObject = null;
	
	public Object dibackend = null;
	public Object trayIcon = null;
	public static HelperClassLoader helperClassloader = null;

	public void start(final BundleContext context) throws Exception {
		bc = context;
		
		// check which java version we have
		String version = "1.5"; //System.getProperty("java.version");
		int dot1 = version.indexOf('.');
		if (dot1 > -1) {
			int dot2 = version.indexOf('.', dot1 + 1);
			if (dot2 > -1)
				version = version.substring(0, dot2);
		}
		mode = (Float.parseFloat(version) >= NATIVE_JRE_SUPPORT) ? Implementations.jre6 : Implementations.jdic;
		
		// copy natives into bundle data folder
		this.copyNatives(context);
		
		final JdicManager manager = JdicManager.getManager();
		manager.initShareNative();
		
		if (helperClassloader == null) {
			final URL[] helperClassloaderURLs = { manager.jdicStubJarFile.toURI().toURL() };
			helperClassloader = new HelperClassLoader(helperClassloaderURLs, Activator.class.getClassLoader());
		}
		
		// display icon
		final boolean thelisMethod = false;
		
		if (thelisMethod) {
			// use DesktopInit object
			Class<?> init = helperClassloader.loadClass("org.paxle.desktop.impl.DesktopInit");
			Constructor initC = init.getConstructor(BundleContext.class, String.class);
			initObject = initC.newInstance(context, mode.toString());
			initMethod = init.getMethod("init");
			shutdownMethod = init.getMethod("shutdown");
			initMethod.invoke(initObject);
			
		} else {
			// use org.paxle.desktop.backend.*-tree and dialogues.Menu
			this.dibackend = helperClassloader.loadClass(mode.packageName + ".DIBackend").newInstance();
			final Class<?> smC = helperClassloader.loadClass("org.paxle.desktop.impl.ServiceManager");
			final Object sm = smC.getConstructor(helperClassloader.loadClass("org.osgi.framework.BundleContext")).newInstance(context);
			
			final Class<?> menuC = helperClassloader.loadClass("org.paxle.desktop.impl.SystrayMenu2");
			this.shutdownMethod = menuC.getMethod("shutdown");
			this.initObject = menuC.getConstructor(smC,
					helperClassloader.loadClass("org.paxle.desktop.backend.IDIBackend"),
					helperClassloader.loadClass("java.net.URL")
				).newInstance(sm, this.dibackend, context.getBundle().getResource("resources/trayIcon.png"));
		}

	}
	
	public void stop(BundleContext context) throws Exception {
		if (shutdownMethod != null) {
			shutdownMethod.invoke(initObject);
			shutdownMethod = null;
		}
		initMethod = null;
		initObject = null;
		manager = null;
		bc = null;
	}

	@SuppressWarnings("unchecked")
	private void copyNatives(BundleContext context) throws IOException {
		Activator.libPath = context.getDataFile("/").getCanonicalPath();

		File libFile = null;
		Enumeration<URL> libs = context.getBundle().findEntries("/resources/libs/","*",true);
		while (libs.hasMoreElements()) {

			// open the URL
			URL lib = libs.nextElement();
			InputStream libIn = lib.openStream();

			// open a file
			String fileName = lib.getFile();
			int idx = fileName.lastIndexOf("/resources/libs/");
			fileName = fileName.substring(idx+"/resources/libs/".length());			
			libFile = context.getDataFile(fileName);			

			// copy data
			if (!libFile.exists()) {
				File parent = libFile.getParentFile();
				if (!parent.exists()) parent.mkdirs();

				FileOutputStream out = new FileOutputStream(libFile);
				IOTools.copy(libIn,out);
				out.close();
			}			
		}		
	}
}
