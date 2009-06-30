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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.ObjectClassDefinition;

import org.paxle.core.io.IResourceBundleTool;
import org.paxle.desktop.IDesktopServices;
import org.paxle.desktop.backend.IDIBackend;

@Component
@Service(IDesktopServices.class)
public class DesktopServices implements ManagedService, MetaTypeProvider, IDesktopServices {
	
	/**
	 * Denotes a relative path the the Paxle-icon used for the tray.
	 * @see #setTrayMenuVisible(boolean)
	 */
	private static final String TRAY_ICON_LOCATION = "/resources/trayIcon.png";
	
	/* ============================================================================ *
	 * Class-names needed for reflective access to bundles this bundle does not
	 * strictly depend on
	 * ============================================================================ */
	
	/** The fully qualified name of the interface under which the ServletManager of the GUI-bundle registered to the framework */
	private static final String ISERVLET_MANAGER = "org.paxle.gui.IServletManager";
	
	/** The fully qualified name of the {@link org.paxle.desktop.impl.DICommandProvider} for this bundle
	 * @see DesktopServices#COMMAND_PROVIDER
	 */
	private static final String DI_COMMAND_PROVIDER = "org.paxle.desktop.impl.DICommandProvider";
	/** The fully qualified name of the interface, the proprietary CommandProvider of the Equinox-framework is accessable under */
	private static final String COMMAND_PROVIDER = "org.eclipse.osgi.framework.console.CommandProvider";
	
	/* ============================================================================ *
	 * ConfigurationManagement-related constants
	 * ============================================================================ */
	private static final String PREF_PID = IDesktopServices.class.getName();
	private static final String PREF_OPEN_BROWSER_STARTUP 	= PREF_PID + "." + "openBrowser";
	private static final String PREF_SHOW_SYSTRAY 			= PREF_PID + "." + "showTrayMenu";
	private static final String PREF_OPEN_BROWSER_SERVLET	= PREF_PID + "." + "openServlet";
	private static final String PREF_OPEN_BROWSER_SERVLET_DEFAULT = "/search";
	
	/* ============================================================================ *
	 * Object variables
	 * ============================================================================ */
	
	private final Log logger = LogFactory.getLog(DesktopServices.class);
	private final ServiceManager manager;
	private final IDIBackend backend;
	private final DialogueServices dialogue;
	private final CrawlStartHelper crawlHelper;
	// private final Map<Dialogues,Frame> dialogues = new EnumMap<Dialogues,Frame>(Dialogues.class);
	private final String[] locales;
	
	private final ServiceRegistration regManagedService;
	private final ServiceRegistration regConsoleCmdProvider;
	
	private SystrayMenu trayMenu = null;
	private boolean browserOpenable = true;
	private String openServlet = PREF_OPEN_BROWSER_SERVLET_DEFAULT;
	
	// TODO: get desktop-services working without a backend, dialogues can still be useful and
	//       may be started in different ways than using the tray-menu
	public DesktopServices(
			final ServiceManager manager,
			final IDIBackend backend,
			final DialogueServices dialogue,
			final CrawlStartHelper crawlHelper) {
		this.manager = manager;
		this.backend = backend;
		this.dialogue = dialogue;
		this.crawlHelper = crawlHelper;
		
		// get available locales
		final IResourceBundleTool rbt = manager.getService(IResourceBundleTool.class);
		locales = (rbt == null) ? null : rbt.getLocaleArray("IDesktopService", Locale.ENGLISH);
		
		// register managed-service for CM
		final Hashtable<String,Object> regProps = new Hashtable<String,Object>();
		regProps.put(Constants.SERVICE_PID, PREF_PID);
		regManagedService = manager.registerService(this, regProps, new String[] {
				ManagedService.class.getName(),
				MetaTypeProvider.class.getName()
		});
		
		// if running in Equinox OSGi-framework, register a CommandProvider, otherwise fail silently
		regConsoleCmdProvider = registerDICommandProvider();
		
		// initialize the local variables from properties-store and initially update the configuration
		initDS();
	}
	
	@SuppressWarnings("unchecked")
	private ServiceRegistration registerDICommandProvider() {
		try {
			final Class cmdProviderC = Class.forName(COMMAND_PROVIDER);
			final Class<?> diCmdProviderC = Class.forName(DI_COMMAND_PROVIDER);
			return manager.registerService(
					diCmdProviderC.getConstructor(DesktopServices.class, DialogueServices.class).newInstance(this, dialogue),
					null,
					cmdProviderC);
		} catch (ClassNotFoundException e) {
			final String msg = "Not running in Equinox, Paxle desktop command provider won't be available.";
			if (logger.isDebugEnabled()) {
				logger.debug(msg, e);
			} else {
				logger.info(msg);
			}
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}
	
	private void initDS() {
		// check whether starting the browser on startup is set in the config and open it if necessary
		final ConfigurationAdmin cadmin = manager.getService(ConfigurationAdmin.class);
		Dictionary<?,?> props = null;
		if (cadmin != null) try {
			final Configuration conf = cadmin.getConfiguration(PREF_PID, manager.getBundle().getLocation());
			if (conf != null)
				props = conf.getProperties();
		} catch (IOException e) { e.printStackTrace(); }
		if (props == null)
			props = getDefaults();
		
		final Boolean openBrowserStartup = (Boolean)props.get(PREF_OPEN_BROWSER_STARTUP);
		if (openBrowserStartup.booleanValue()) {
			final Object openServletObj = props.get(PREF_OPEN_BROWSER_SERVLET);
			if (openServletObj != null)
				openServlet = (String)openServletObj;
			
			// opening the browser
			new Thread() {
				@Override
				public void run() {
					try {
						// a delay to ensure that the GUI bundle was started when we are trying to 
						// open the browser
						// TODO: we need a better solution here
						Thread.sleep(3000);
						browseDefaultServlet(false);
					} catch (InterruptedException e) { /* ignore */ }
				}
			}.run();			
		}
	}
	
	public void shutdown() {
		// remove tray-menu
		setTrayMenuVisible(false);
		
		// unregister services registered during instantiation
		if (regManagedService != null)
			regManagedService.unregister();
		if (regConsoleCmdProvider != null)
			regConsoleCmdProvider.unregister();
	}
	
	/* ========================================================================== *
	 * OSGi Services related methods
	 * ========================================================================== */
	
	private static Hashtable<String,Object> getDefaults() {
		final Hashtable<String,Object> defaults = new Hashtable<String,Object>();
		defaults.put(PREF_OPEN_BROWSER_STARTUP, Boolean.TRUE);
		defaults.put(PREF_SHOW_SYSTRAY, Boolean.TRUE);
		defaults.put(PREF_OPEN_BROWSER_SERVLET, PREF_OPEN_BROWSER_SERVLET_DEFAULT);
		return defaults;
	}
	
	public String[] getLocales() {
		return (this.locales == null) ? null : this.locales.clone();
	}
	
	public ObjectClassDefinition getObjectClassDefinition(String id, String loc) {
		final Locale locale = (loc == null) ? Locale.ENGLISH : new Locale(loc);
		final ResourceBundle rb = ResourceBundle.getBundle("OSGI-INF/l10n/IDesktopService", locale);
		
		abstract class SingleAD implements AttributeDefinition {
			
			private final String pid;
			
			public SingleAD(final String pid) {
				this.pid = pid;
			}
			
			public int getCardinality() {
				return 0;
			}
			
			public String getDescription() {
				return rb.getString("desktopService." + pid.substring(pid.lastIndexOf('.') + 1) + ".desc");
			}
			
			public String getID() {
				return pid;
			}
			
			public String getName() {
				return rb.getString("desktopService." + pid.substring(pid.lastIndexOf('.') + 1) + ".name");
			}
		}
		
		final class BooleanAD extends SingleAD {
			
			public BooleanAD(final String pid) {
				super(pid);
			}
			
			public String[] getDefaultValue() {
				return new String[] { Boolean.TRUE.toString() };
			}
			
			public String[] getOptionLabels() { return null; }
			public String[] getOptionValues() { return null; }
			
			public int getType() {
				return BOOLEAN;
			}
			
			public String validate(String value) {
				return null;
			}
		}
		
		final class OpenBrowserAD extends SingleAD {
			
			private final String defaultValue;
			private final String[] servletNames; 
			
			public OpenBrowserAD(final Object servletManager) {
				super(PREF_OPEN_BROWSER_SERVLET);
				try {
					final Class<?> servletManagerClazz = servletManager.getClass();
					final Method getServlets = servletManagerClazz.getMethod("getServlets");
					
					final Map<?,?> servlets = (Map<?,?>)getServlets.invoke(servletManager);
					servletNames = new String[servlets.size()];
					int idx = 0;
					String defVal = null;
					final Iterator<?> it = servlets.keySet().iterator();
					while (it.hasNext()) {
						final String name = (String)it.next();
						servletNames[idx] = name;
						if (name.equals(PREF_OPEN_BROWSER_SERVLET_DEFAULT))
							defVal = name;
						idx++;
					}
					defaultValue = defVal;
					Arrays.sort(servletNames);
				} catch (Exception e) { throw new RuntimeException(e); }
			}
			
			public String[] getDefaultValue() {	return new String[] { defaultValue }; }
			public String[] getOptionLabels() {	return servletNames; }
			public String[] getOptionValues() { return servletNames; }
			public int getType() { return STRING; }
			public String validate(String value) { return null; }
		}
		
		return new ObjectClassDefinition() {
			public AttributeDefinition[] getAttributeDefinitions(int filter) {
				final Object servletManager = manager.getService(ISERVLET_MANAGER);
				final boolean hasWebUi = (servletManager != null);
				
				int idx = 0;
				final AttributeDefinition[] ads = new AttributeDefinition[(hasWebUi) ? 3 : 2];
				ads[idx++] = new BooleanAD(PREF_OPEN_BROWSER_STARTUP);	// option for opening the browser on start-up
				if (hasWebUi)
					ads[idx++] = new OpenBrowserAD(servletManager);
				ads[idx++] = new BooleanAD(PREF_SHOW_SYSTRAY);			// option to show/hide the system tray icon
				
				return ads;
			}
			public String getDescription() { return rb.getString("desktopService.desc"); }
			public InputStream getIcon(int size) throws IOException { return getClass().getResourceAsStream("/OSGI-INF/images/systemtray.png"); }
			public String getID() { return PREF_PID; }
			public String getName() { return rb.getString("desktopService.name"); }
		};
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void updated(Dictionary properties) throws ConfigurationException {
		try {
			if (properties == null) {
				properties = getDefaults();
			}
			
			final Object showTrayMenu = properties.get(PREF_SHOW_SYSTRAY);
			if (showTrayMenu != null) {
				final boolean showTM = ((Boolean)showTrayMenu).booleanValue();
				setTrayMenuVisible(showTM);
			}
			
			final Object openServletObj = properties.get(PREF_OPEN_BROWSER_SERVLET);
			if (openServletObj != null)
				openServlet = (String)openServletObj;
			
			// PREF_OPEN_BROWSER_STARTUP only matters for the initialization of this class,
			// changing the values does not necessitate runtime-changes.
		} catch (Throwable e) { e.printStackTrace(); }
	}
	
	/* ========================================================================== *
	 * Desktop-related methods
	 * ========================================================================== */
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.desktop.IDesktopServices#isBrowserOpenable()
	 */
	public boolean isBrowserOpenable() {
		return browserOpenable;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.desktop.IDesktopServices#isTrayMenuVisible()
	 */
	public boolean isTrayMenuVisible() {
		return trayMenu != null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.desktop.IDesktopServices#setTrayMenuVisible(boolean)
	 */
	public void setTrayMenuVisible(final boolean yes) {
		if (yes && !isTrayMenuVisible()) {
			trayMenu = new SystrayMenu(manager, this, dialogue, crawlHelper, manager.getBundle().getResource(TRAY_ICON_LOCATION));
		} else if (!yes && isTrayMenuVisible()) {
			trayMenu.close();
			trayMenu = null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.desktop.IDesktopServices#getPaxleUrl(java.lang.String[])
	 */
	public String getPaxleUrl(String... path) {
		final String port = manager.getProperty("org.osgi.service.http.port");
		if (port == null) return null;
		
		final StringBuffer sb = new StringBuffer("http://127.0.0.1:").append(port);
		if (path.length == 0 || path[0].charAt(0) != '/') sb.append('/');
		for (final String s : path) sb.append(s);
		return sb.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.desktop.IDesktopServices#browseUrl(java.lang.String)
	 */
	public boolean browseUrl(final String url) {
		return browseUrl(url, false, true);
	}
	
	public boolean browseUrl(final String url, final boolean force, final boolean displayErrMsg) {
		if (url == null) {
			JOptionPane.showMessageDialog(null, "HTTP service not accessible", "Error", JOptionPane.ERROR_MESSAGE);
		} else if (browserOpenable || force) try {
			browserOpenable = backend.getDesktop().browse(url);
			if (browserOpenable)
				return true;
			
			if (displayErrMsg) {
				Utilities.instance.showURLErrorMessage(
						"Couldn't launch system browser due to an error in Paxle's desktop integration\n" +
						"bundle. Please review the log for details. The requested URL was:", url);
			}
		} catch (MalformedURLException e) {
			Utilities.instance.showExceptionBox("Generated mal-formed URL", e);
			logger.error("Generated mal-formed URL '" + url + "': " + e.getMessage(), e);
		}
		return false;
	}
	
	public void browseDefaultServlet(final boolean displayErrMsg) {
		browseServlet(openServlet, displayErrMsg);
	}
	
	public void browseServlet(final String servlet, final boolean displayErrMsg) {
		final Object servletManager = manager.getService(ISERVLET_MANAGER);
		String msg;
		do {
			if (servletManager == null) {
				msg = "GUI ServletManager not available";
				break;
			}
			
			try {
				final Class<?> servletManagerClazz = servletManager.getClass();
				// first check whether the servlet is available
				final Method hasServlet = servletManagerClazz.getMethod("hasServlet", String.class);
				final Object hasServletRes = hasServlet.invoke(servletManager, servlet);
				if (hasServletRes == null || !((Boolean)hasServletRes).booleanValue()) {
					msg = "servlet has not been registered";
					break;
				}
				// then get the path for which it has been registered to construct the final URI
				final Method getFullAlias = servletManagerClazz.getMethod("getFullAlias", String.class);
				final String servletPath = (String)getFullAlias.invoke(servletManager, servlet);
				final String url = getPaxleUrl(servletPath);
				
				logger.debug("Opening browser: " + url);
				final boolean success = browseUrl(url, true, displayErrMsg);
				logger.info(((success) ? "Succeeded" : "Failed") + " opening browser for " + url);
				
				return;
			} catch (Exception e) { e.printStackTrace(); msg = "[" + e.getClass() + "]: " + e.getMessage(); }
			
		} while (false);
		
		msg = "Cannot open servlet '" + servlet + "', " + msg;
		logger.warn(msg);
		if (displayErrMsg)
			JOptionPane.showMessageDialog(null, msg, "Error opening servlet", JOptionPane.ERROR_MESSAGE);
	}
	
	public IDIBackend getBackend() {
		return backend;
	}
}
