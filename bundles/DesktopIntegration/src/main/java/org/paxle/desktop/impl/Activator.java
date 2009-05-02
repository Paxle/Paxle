/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.desktop.impl;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
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
import org.paxle.desktop.IDesktopServices;
import org.paxle.desktop.IDesktopUtilities;
import org.paxle.desktop.IDialogueServices;
import org.paxle.desktop.backend.IDIBackend;

public class Activator implements BundleActivator {
	
	private static final Log logger = LogFactory.getLog(Activator.class);
	
	public static final String BACKEND_IMPL_ROOT_PACKAGE = "org.paxle.desktop.backend.impl";
	private static final String CLASSSIMPLE_DI_BACKEND 		= "DIBackend";
	
	public static final float NATIVE_JRE_SUPPORT = 1.6f;
	
	private static final String IMPL_JDIC = "jdic";
	private static final String IMPL_JRE6 = "jre6";
	
	/**
	 * Contains all currently known implementations of the desktop integration backend descendingly
	 * sorted by the first supported Java version number
	 */
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
	
	public DesktopServices initObject = null;
	public DialogueServices dialogue = null;
	
	public void start(final BundleContext bc) throws Exception {
		
		// first we check whether the DI-bundle will able to work on the system
		if (GraphicsEnvironment.isHeadless()) {
			logger.fatal("Java runs in a headless environment, cannot initialize any graphical user interfaces, aborting");
			return;
		}
		
		uiClassLoader = this.getClass().getClassLoader();		
		Thread.currentThread().setContextClassLoader(uiClassLoader);
		
		/* 
		 * Configuring the classloader to use by the UIManager.
		 * ATTENTION: do not remove this, otherwise we get ClassNotFoundExceptions 
		 */
		UIManager.put("ClassLoader", this.getClass().getClassLoader());
		
		final float javaVersion = getJavaVersion();
		logger.debug(String.format("Detected java version: %f", Float.valueOf(javaVersion)));
		
		final Iterator<Map.Entry<Float,String>> implIt = KNOWN_IMPLS.entrySet().iterator();
		boolean started = false;
		
		// TODO: instantiate (and register) ServiceManager, DialogServices and the DICommandProvider here
		
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
				initUI(bc, impl.getValue());
				started = true;
				logger.info(String.format("Successfully started bundle using backend '%s'", impl));
				break;
			} catch (Exception e) {
				final Throwable ex = ((e instanceof InvocationTargetException) ? e.getCause() : e);
				final String cause = ((ex instanceof UnsupportedOperationException)
						? "Java claims your system to not support the system-tray."
						: ex.toString());
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
		this.initObject.shutdown();
		this.dialogue.shutdown();
		initObject = null;
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
			/*
			 * Initializing Jdic 
			 */
			// JdicInit.init();
			uiClassLoader.loadClass("org.paxle.desktop.backend.impl.jdic.JdicInit")
						 .getMethod("init", (Class[])null)
						 .invoke(null, (Object[])null);
		}
		
		// use org.paxle.desktop.backend.*-tree and dialogues.Menu
		final String diBackendCName = String.format("%s.%s.%s", BACKEND_IMPL_ROOT_PACKAGE, impl, CLASSSIMPLE_DI_BACKEND);
		
		final IDIBackend dibackend = (IDIBackend) uiClassLoader.loadClass(diBackendCName).newInstance();		
		final ServiceManager sm = new ServiceManager(context);
		
		dialogue = new DialogueServices(sm);
		
		// TODO: when CrawlerCore installed, set crawlHelper
		CrawlStartHelper crawlHelper = null;
		
		dialogue.init();
		this.initObject = new DesktopServices(sm, dibackend, dialogue, crawlHelper);
		
		context.registerService(IDesktopUtilities.class.getName(), Utilities.instance, null);
		context.registerService(IDialogueServices.class.getName(), dialogue, null);
		context.registerService(IDesktopServices.class.getName(), initObject, null);
	}
	
	private static Bundle findBundle(BundleContext context, String symbolicName) {
		for (Bundle b : context.getBundles()) {
			if (symbolicName.equals(b.getHeaders().get(Constants.BUNDLE_SYMBOLICNAME)))
				return b;
		}
		return null;
	}
}
