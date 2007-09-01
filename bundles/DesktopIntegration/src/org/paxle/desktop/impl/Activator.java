
package org.paxle.desktop.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;

import org.jdesktop.jdic.init.JdicManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * To get this to work you need to set the variable
 * org.osgi.framework.system.packages=sun.awt,javax.swing,javax.swing.event,sun.awt.motif,sun.awt.X11,javax.swing.plaf.metal,javax.swing.plaf.basic
 *
 */
public class Activator implements BundleActivator {

	public static BundleContext bc = null;
	public static ServiceManager manager = null;
	public static String libPath = null;	
	
	public Method initMethod = null;
	public Method shutdownMethod = null;
	public Object initObject = null;
	public HelperClassLoader helperClassloader = null;

	public void start(final BundleContext context) throws Exception {
		bc = context;

		// copy natives into bundle data folder
		this.copyNatives(context);

		JdicManager manager = JdicManager.getManager();
		manager.initShareNative();
		helperClassloader = new HelperClassLoader(new URL[]{manager.jdicStubJarFile.toURL()}, Activator.class.getClassLoader());

		// display icon
		Class init = helperClassloader.loadClass("org.paxle.desktop.impl.DesktopInit");
		Constructor initC = init.getConstructor(new Class[]{BundleContext.class});
		initObject = initC.newInstance(context);
		initMethod = init.getMethod("init", (Class[])null);
		shutdownMethod = init.getMethod("shutdown",(Class[])null);
		initMethod.invoke(initObject, (Object[])null);

	}

	public void stop(BundleContext context) throws Exception {
		shutdownMethod.invoke(initObject, (Object[])null);
		shutdownMethod = null;
		initMethod = null;
		initObject = null;
		helperClassloader = null;
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
				copy(libIn,out);
				out.flush();
				out.close();
			}			
		}		
	}

	static void copy( InputStream in, OutputStream out ) throws IOException { 
		byte[] buffer = new byte[ 0xFFFF ]; 

		for ( int len; (len = in.read(buffer)) != -1; ) 
			out.write( buffer, 0, len ); 
	} 
}
