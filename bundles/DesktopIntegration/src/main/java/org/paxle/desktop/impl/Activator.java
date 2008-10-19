/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

package org.paxle.desktop.impl;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.paxle.core.io.IOTools;

/**
 * To get this to work you need to set the variable
 * org.osgi.framework.system.packages=sun.awt,javax.swing,javax.swing.event,sun.awt.motif,sun.awt.X11,javax.swing.plaf.metal,javax.swing.plaf.basic
 *
 */
public class Activator implements BundleActivator {
	
	private static final Log logger = LogFactory.getLog(Activator.class);
	
	static {
		logger.debug("Loading Activator with class-loader: " + Thread.currentThread().getContextClassLoader());
	}
	
	public static final float NATIVE_JRE_SUPPORT = 1.6f;
	public static final String BACKEND_IMPL_ROOT_PACKAGE = "org.paxle.desktop.backend.impl";
	
	private static final String IMPL_JDIC = "jdic";
	private static final String IMPL_JRE6 = "jre6";
	
	/**
	 * Contains all currently known implementations of the desktop integration backend descendingly
	 * sorted by the first supported Java version number
	 */
	// XXX: maybe this can work with the execution environments the respective bundle manifest specifies,
	// all I currently know is that J2SE-1.5 or something like that seems not to be a legal value here
	private static final TreeMap<Float,String> KNOWN_IMPLS = new TreeMap<Float,String>(
			// negative comparator
			new Comparator<Float>() {
				public int compare(Float o1, Float o2) {
					return -o1.compareTo(o2);
				}
			});
	
	static {
		KNOWN_IMPLS.put(Float.valueOf(1.6f), IMPL_JRE6);
		KNOWN_IMPLS.put(Float.valueOf(1.4f), IMPL_JDIC);
	}
	
	private static final String CLASS_IDI_BACKEND 			= "org.paxle.desktop.backend.IDIBackend";
	private static final String CLASS_DESKTOP_SERVICES 		= "org.paxle.desktop.impl.DesktopServices";
	private static final String CLASS_IDESKTOP_SERVICES		= "org.paxle.desktop.IDesktopServices";
	private static final String CLASS_SERVICE_MANAGER 		= "org.paxle.desktop.impl.ServiceManager";
	private static final String CLASS_BUNDLE_CONTEXT 		= "org.osgi.framework.BundleContext";
	private static final String CLASS_JDIC_MANAGER 			= "org.jdesktop.jdic.init.JdicManager";
	
	private static final String CLASSSIMPLE_DI_BACKEND 		= "DIBackend";
	
	private static final String METHOD_SHUTDOWN 			= "shutdown";
	private static final String METHOD_GET_MANAGER 			= "getManager";
	private static final String METHOD_INIT_SHARE_NATIVE 	= "initShareNative";
	
	private static final String FIELD_JDIC_STUB_JAR_FILE 	= "jdicStubJarFile";
	
	private static final String PATH_JDIC_LIBS 				= "/binaries/libs/";
	
	private static float getJavaVersion() {
		String version = System.getProperty("java.version");
		int dot = version.indexOf('.');
		if (dot != -1) {
			dot = version.indexOf('.', dot + 1);
			if (dot != -1)
				version = version.substring(0, dot);
		}
		return Float.parseFloat(version);
	}
	
	public static ClassLoader uiClassLoader = Activator.class.getClassLoader();
	public static BundleContext bc = null;
	public static String libPath = null;
	
	public Method shutdownMethod = null;
	public Object initObject = null;
	
	public void start(final BundleContext context) throws Exception {
		bc = context;
		
		// first we check whether the DI-bundle will able to work on the system
		if (GraphicsEnvironment.isHeadless()) {
			logger.fatal("Java runs in a headless environment, cannot initialize any graphical user interfaces, aborting");
			return;
		}
		
		/* 
		 * Configuring the classloader to use by the UIManager.
		 * ATTENTION: do not remove this, otherwise we get ClassNotFoundExceptions 
		 */
		UIManager.put("ClassLoader", this.getClass().getClassLoader());
		
		final float javaVersion = getJavaVersion();
		logger.debug(String.format("Detected java version: %f", Float.valueOf(javaVersion)));
		
		final Iterator<Map.Entry<Float,String>> implIt = KNOWN_IMPLS.entrySet().iterator();
		boolean started = false;
		while (implIt.hasNext()) {
			final Map.Entry<Float,String> impl = implIt.next();
			
			// discard if not supported by JRE
			if (impl.getKey().floatValue() > javaVersion) {
				logger.info(String.format(
						"Implementation %s skipped because of missing java version %f.",
						impl.getValue(),
						impl.getKey()
				));
				continue;
			}
			
			try {
				// display icon				
				initUI(context, impl.getValue());
				started = true;
				logger.info(String.format("Successfully started bundle using backend '%s'", impl));
				break;
			} catch (Exception e) {
				final Throwable ex = (e instanceof InvocationTargetException) ? e.getCause() : e;
				final String cause =
					(ex instanceof UnsupportedOperationException) ? "Java claims your system to not support the system-tray." :
					ex.toString();
				final String err = String.format("Error starting bundle using backend '%s': %s Skipping implementation...", impl, cause);
				if (logger.isDebugEnabled()) {
					logger.error(err, ex);
				} else {
					logger.error(err);
				}
			}
		}
		if (!started)
			logger.fatal("No backends left, could not start bundle");
	}
	
	public void stop(BundleContext context) throws Exception {
		if (shutdownMethod != null) {
			shutdownMethod.invoke(initObject);
			shutdownMethod = null;
		}
		initObject = null;
		bc = null;
	}
	
	private void initUI(BundleContext context, String impl) throws Exception {
		String bundleName = BACKEND_IMPL_ROOT_PACKAGE + '.' + impl;
		Bundle bundle = findBundle(context, bundleName);
		if (bundle != null) {
			logger.info(String.format("Using implementation %s ...", impl));
		} else {
			throw new ClassNotFoundException(String.format(
					"Unable to find bundle '%s' for implementation %s, is the bundle resolved?", bundleName, impl));
		}
		
		if (impl == IMPL_JDIC) {
			// copy natives into bundle data folder
			copyNatives(context);
			
			// JdicManager.getManager().initShareNative();
			final Class<?> jdicManagerC = uiClassLoader.loadClass(CLASS_JDIC_MANAGER);
			final Object jdicManager = jdicManagerC.getMethod(METHOD_GET_MANAGER).invoke(null);
			jdicManagerC.getMethod(METHOD_INIT_SHARE_NATIVE).invoke(jdicManager);
			
			if (!(uiClassLoader instanceof HelperClassLoader)) {
				logger.debug("Wrapping the classloader into a HelperClassLoader for JDIC classes");
				final File jdicStubJarFile = (File)jdicManagerC.getField(FIELD_JDIC_STUB_JAR_FILE).get(jdicManager);
				final URL[] helperClassloaderURLs = { jdicStubJarFile.toURI().toURL() };
				uiClassLoader = new HelperClassLoader(helperClassloaderURLs, uiClassLoader);
			}
		}
		
		// use org.paxle.desktop.backend.*-tree and dialogues.Menu
		final String diBackendCName = String.format("%s.%s.%s", BACKEND_IMPL_ROOT_PACKAGE, impl, CLASSSIMPLE_DI_BACKEND);
		logger.debug("Loading " + diBackendCName + " using class-loader " + uiClassLoader);
		
		final Object dibackend = uiClassLoader.loadClass(diBackendCName).newInstance();
		final Class<?> smC = uiClassLoader.loadClass(CLASS_SERVICE_MANAGER);
		final Object sm = smC.getConstructor(uiClassLoader.loadClass(CLASS_BUNDLE_CONTEXT)).newInstance(context);
		
		final Class<?> idibackendC = uiClassLoader.loadClass(CLASS_IDI_BACKEND);
		final Class<?> desktopServicesC = uiClassLoader.loadClass(CLASS_DESKTOP_SERVICES);
		initObject = desktopServicesC.getConstructor(smC, idibackendC).newInstance(sm, dibackend);
		context.registerService(CLASS_IDESKTOP_SERVICES, initObject, null);
		shutdownMethod = desktopServicesC.getMethod(METHOD_SHUTDOWN);
	}
	
	private static Bundle findBundle(BundleContext context, String symbolicName) {
		for (Bundle b : context.getBundles()) {
			final Object bundleSymbolicName = b.getHeaders().get(Constants.BUNDLE_SYMBOLICNAME);
			final boolean eq = bundleSymbolicName.equals(symbolicName);
			if (eq)
				return b;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static void copyNatives(BundleContext context) throws IOException {
		logger.debug("copyNatives(): BundleContext: " + context);
		Activator.libPath = context.getDataFile("").getCanonicalPath();

		File libFile = null;
		
		Enumeration<URL> libs = findBundle(context, BACKEND_IMPL_ROOT_PACKAGE + '.' + IMPL_JDIC).findEntries(PATH_JDIC_LIBS, "*", true);
		while (libs.hasMoreElements()) {
			
			// open the URL
			URL lib = libs.nextElement();
			
			InputStream libIn = lib.openStream();
			
			// open a file
			String fileName = lib.getFile();
			int idx = fileName.lastIndexOf(PATH_JDIC_LIBS);
			fileName = fileName.substring(idx + PATH_JDIC_LIBS.length());			
			libFile = context.getDataFile(fileName);			
			
			if (!libFile.exists() && !fileName.endsWith("/")) {
				File parent = libFile.getParentFile();
				if (!parent.exists()) parent.mkdirs();
				
				FileOutputStream out = new FileOutputStream(libFile);
				IOTools.copy(libIn,out);
				out.close();
			}
		}
	}
}
