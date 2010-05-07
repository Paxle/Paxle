/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.gui.impl;

import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.apache.velocity.tools.view.VelocityView;
import org.apache.velocity.tools.view.VelocityViewServlet;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.paxle.gui.IMenuManager;
import org.paxle.gui.IServletManager;

@Component(immediate=true, metatype=false, name="org.paxle.gui.IServletManager")
@Services({
	@Service(IServletManager.class)
})
@Property(name="org.paxle.gui.IServletManager.pathPrefix", value="")
@Reference(
	name="servlets",
	referenceInterface=Servlet.class,
	cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
	policy=ReferencePolicy.DYNAMIC,
	bind="addServlet",
	unbind="removeServlet",
	target="(org.paxle.servlet.path=*)"
)
public class ServletManager implements IServletManager, BundleListener {
	
	private static final String SERVLET_DO_USER_AUTH = "org.paxle.servlet.doUserAuth";
	private static final String SERVLET_MENU_ICON = "org.paxle.servlet.menu.icon";
	private static final String SERVLET_MENU_LOCALIZATION = "org.paxle.servlet.menu.localization";
	private static final String SERVLET_MENU_POS = "org.paxle.servlet.menu.pos";
	private static final String SERVLET_MENU_NAME = "org.paxle.servlet.menu";
	private static final String SERVLET_PATH = "org.paxle.servlet.path";

	/**
	 * All registeres {@link Servlet servlets}
	 */
	private HashMap<String,ServiceReference> servlets = new HashMap<String,ServiceReference>();
	
	/**
	 * All registered resources
	 */
	private HashMap<String, ResourceReference> resources = new HashMap<String, ResourceReference>();

	/**
	 * All VelocityViewFactories
	 */
	private HashMap<Long, VelocityViewFactory> factories = new HashMap<Long, VelocityViewFactory>();
	
	/**
	 * The OSGI {@link HttpService Http-Service}
	 */
	@Reference
	protected HttpService http;
	
	@Reference
	protected IMenuManager menuManager;
	
	/**
	 * This service is required for authentication
	 * @see #createHttpContext(ServiceReference)
	 */
	@Reference
	protected IHttpAuthManager authManager;
	
	/**
	 * Default Servlet properties
	 */
	private Hashtable<String, String> defaultProps;
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A prefix that should be used for each servlet- and resource-alias
	 */
	private String pathPrefix = "";
	
	private ComponentContext context;
	
	protected String getBundleLocation(Bundle bundle) {
		if (bundle == null) return null;
		
		URL bundleURL = bundle.getEntry("");                     // for equinox
		if (bundleURL == null) bundleURL = bundle.getEntry("/"); // for apache felix
		
		String bundleLocation = bundleURL.toString();
		if (bundleLocation != null && bundleLocation.endsWith("/")) {
			bundleLocation = bundleLocation.substring(0,bundleLocation.length()-1);
		}
		
		return bundleLocation;
	}
	
	protected synchronized void activate(ComponentContext context) {
		this.context = context;
		
		// add class as bundle listener
		this.context.getBundleContext().addBundleListener(this);
		
		// add some properties to the servlet props
		this.defaultProps = new Hashtable<String, String>();
		
		this.defaultProps.put(VelocityView.PROPERTIES_KEY, "/resources/config/velocity.properties");
		this.defaultProps.put(VelocityView.TOOLS_KEY, "/resources/config/velocity-toolbox.properties");
		this.defaultProps.put(VelocityView.LOAD_DEFAULTS_KEY, "false");
		
		// the default location to use for template-loading
		final String defaultBundleLocation = this.getBundleLocation(context.getBundleContext().getBundle());
		this.defaultProps.put("bundle.location.default",defaultBundleLocation);
		
		// getting the path prefix to use
		String newPathPrefix = (String) context.getProperties().get("org.paxle.gui.IServletManager.pathPrefix");
		this.changePath(newPathPrefix);		
		
		// register all servlets/resources
		this.registerAll();
	}
	
	protected synchronized void deactivate(ComponentContext context) {
		// unregister all servlets/resources
		this.unregisterAll();
		
		// clear properties
		this.defaultProps.clear();
		
		// clear cache
		this.factories.clear();
		
		// remove class as bundle listener
		context.getBundleContext().removeBundleListener(this);
	}
	
	protected synchronized void addServlet(ServiceReference servletRef) {
		// remember the servlet in our internal servlet-list
		final String path = (String)servletRef.getProperty(SERVLET_PATH);
		this.servlets.put(path, servletRef);
		
		// registering the servlet to the http-service
		this.registerServlet(servletRef);
		
		// registering the servlet menu item
		this.registerMenuItem(servletRef);
	}
	
	protected synchronized void removeServlet(ServiceReference servletRef) {
		// unregistering the servlet menu item
		this.unregisterMenuItem(servletRef);
		
		// removing the servlet from our internal servlet-list
		final String path = (String)servletRef.getProperty(SERVLET_PATH);
		this.servlets.remove(path);
		
		// unregistering the servlet from the http-service
		this.unregisterServlet(servletRef);
	}
	
	private HttpContext createHttpAuthContext(ServiceReference servletRef) {
		// checking if we need an authentication
		Boolean userAuth = (Boolean)servletRef.getProperty(SERVLET_DO_USER_AUTH);		
		if (userAuth == null) userAuth = Boolean.FALSE;
		if (!userAuth.booleanValue()) return null;
		
		// getting the bundle the servlet belongs to
		Bundle bundle = servletRef.getBundle();
		
		// creating an authentication context
		return this.authManager.createHttpAuthContext(bundle);
	}
	
	private void registerMenuItem(ServiceReference servletRef) {
		if (this.menuManager == null) return;
		
		// the name of the menu-item
		String menuName = (String)servletRef.getProperty(SERVLET_MENU_NAME);
		if (menuName == null || menuName.length() == 0) return;
		
		Integer menuPos = (Integer)servletRef.getProperty(SERVLET_MENU_POS);
		if (menuPos == null) menuPos = Integer.valueOf(IMenuManager.DEFAULT_MENU_POS);
		
		// getting the path to use
		String path = (String)servletRef.getProperty(SERVLET_PATH);
		
		// path to an icon
		URL iconURL = null;
		String iconPath = (String)servletRef.getProperty(SERVLET_MENU_ICON);
		if (iconPath != null && iconPath.startsWith("/")) {
			// getting the bundle 
			iconURL = servletRef.getBundle().getEntry(iconPath);
		}
		
		// convert it into a full alias (pathprefix + alias)
		final String fullAlias = this.getFullAlias(path);		
		
		// the osgi-bundle that should be used
		final Bundle osgiBundle = servletRef.getBundle();
		
		// the resource-bundle base-name that should be used
		String resourceBundleBase = null;
		if (menuName.startsWith("%") || menuName.contains("/%")) {
			/* 
			 * The menu-name needs to be localized.
			 * We are trying to finde the proper resource-bundle to use
			 */
			
			// the resource-bundle basename
			resourceBundleBase = (String) servletRef.getProperty(SERVLET_MENU_LOCALIZATION);
			if (resourceBundleBase == null)
				resourceBundleBase = (String) servletRef.getBundle().getHeaders().get(Constants.BUNDLE_LOCALIZATION);
			if (resourceBundleBase == null)
				resourceBundleBase = Constants.BUNDLE_LOCALIZATION_DEFAULT_BASENAME;
		}
		this.menuManager.addItem(fullAlias, menuName, resourceBundleBase, osgiBundle, menuPos.intValue(), iconURL);
	}
	
	private void unregisterMenuItem(ServiceReference servletRef) {
		if (this.menuManager == null) return;
		
		String menuName = (String)servletRef.getProperty(SERVLET_MENU_NAME);
		if (menuName == null || menuName.length() == 0) return;
		
		this.menuManager.removeItem(menuName);
	}
	
	public String getFullAlias(String alias) {
		return this.getFullAlias(this.pathPrefix,alias);
	}
	
	public String getFullAlias(String path, String alias) {
		if (path == null) path = "";
		if (path.length() > 1 && !path.startsWith("/")) path = "/" + path;
		if (path.endsWith("/")) path = path.substring(0, path.length() - 1);					
		
		String fullAlias = path + alias;
		if (fullAlias.length() > 1 && fullAlias.endsWith("/")) {
			fullAlias = fullAlias.substring(0,fullAlias.length()-1);
		}
		return fullAlias;
	}
	
	public String getFullServletPath(String servletPID) {
		if (servletPID == null) return null;
		
		for (ServiceReference ref : this.servlets.values()) {
			String servicePID = (String) ref.getProperty(Constants.SERVICE_PID);
			if (servicePID != null && servicePID.equals(servletPID)) {
				final String path = (String)ref.getProperty(SERVLET_PATH);
				return this.getFullAlias(path);
			}
		}
		
		return null;
	}
		
	private void registerServlet(ServiceReference servletRef) {
		if (this.http == null) return;
		if (this.context == null) return;
		
		String fullAlias = null;
		String servletPID = null;
		try {
			// getting the servlet class
			Servlet servlet = (Servlet) this.context.locateService("servlets", servletRef);
			servletPID = (String) servletRef.getProperty(Constants.SERVICE_PID);
			
			// getting the path to use
			final String path = (String)servletRef.getProperty(SERVLET_PATH);
			
			// convert it into a full alias (pathprefix + alias)
			fullAlias = this.getFullAlias(path);
			
			// getting the httpContext to use
			HttpContext httpContext = this.createHttpAuthContext(servletRef);
			
			// init servlet properties
			@SuppressWarnings("unchecked")
			Hashtable<String, String> props = (Hashtable<String, String>) this.defaultProps.clone();
			if (servlet instanceof VelocityViewServlet) {
				final Bundle bundle = servletRef.getBundle();
				final BundleContext bundleContext = bundle.getBundleContext();
				
				// get or create a new velocity factory
				VelocityViewFactory factory = this.factories.get(Long.valueOf(bundle.getBundleId()));
				if (factory == null) {					
					factory = new VelocityViewFactory(bundleContext, this);
					this.factories.put(bundle.getBundleId(), factory);
				}
				
				// configuring the bundle location to use for template loading
				final String bundleLocation = this.getBundleLocation(servletRef.getBundle());	
				props.put("bundle.location", bundleLocation);
				
				// wrapping the servlet into a wrapper
				servlet = (Servlet) Proxy.newProxyInstance(
						servlet.getClass().getClassLoader(), 
						new Class[] {Servlet.class}, 
						new VelocityViewServletWrapper((VelocityViewServlet)servlet,factory)
				);
			}
			
			
			
			// registering the servlet
			this.logger.info(String.format(
					"Registering servlet '%s' for alias '%s'.", 
					servletPID, 
					fullAlias
			));
			this.http.registerServlet(fullAlias, servlet, props, httpContext);
		} catch (Throwable e) {
			this.logger.error(String.format(
					"Unexpected '%s' while registering servlet '%s' for alias '%s'.",
					e.getClass().getName(),
					servletPID,
					fullAlias
			),e);
		}
	}
	

	
	private void unregisterServlet(ServiceReference servletRef) {
		if (this.http == null) return;
		
		String fullAlias = null;
		String servletPID = null;
		try {
			// getting the path of the servlet
			final String path = (String)servletRef.getProperty(SERVLET_PATH);
			servletPID = (String)servletRef.getProperty(Constants.SERVICE_PID);
			
			// convert it into a full alias (pathprefix + alias)
			fullAlias = this.getFullAlias(path);
			
			// unregistering the servlet
			this.logger.info(String.format(
					"Unregistering servlet '%s' for alias '%s'.", 
					servletPID, 
					fullAlias
			));
			this.http.unregister(fullAlias);
		} catch (Throwable e) {
			this.logger.error(String.format("Unexpected '%s' while unregistering servlet '%s' for alias '%s'.",
					e.getClass().getName(),
					servletPID,
					fullAlias
			),e);
		}
	}
	
	public void addResources(String alias, String name) {
		this.addResources(alias, name, null);
	}
	
	public void addResources(String alias, String name, HttpContext httpContext) {
		// remember the resource in our internal structure
		this.resources.put(alias, new ResourceReference(alias, name, httpContext));
		
		// registering the resource to the http-service
		this.registerResource(alias, name, httpContext);
	}
	
	private void registerResource(String alias, String name, HttpContext context) {
		if (this.http == null) return;
		
		String fullAlias = null;
		try {
			// convert the alias into a full alias (pathprefix + alias)
			fullAlias = this.getFullAlias(alias);
			
			// registering resource
			this.logger.info(String.format(
					"Registering resource '%s' for alias '%s'.", 
					name, 
					fullAlias
			));
			this.http.registerResources(fullAlias, name, context);
		} catch (Throwable e) {
			this.logger.error(String.format(
					"Unexpected '%s' while registering resource '%s' for alias '%s'.",
					e.getClass().getName(),
					name,
					alias
			),e);
		}
	}

	private void unregisterResource(String alias) {
		if (this.http == null) return;
		
		String fullAlias = null;
		try {
			// convert the alias into a full alias (pathprefix + alias)
			fullAlias = this.getFullAlias(alias);
			
			// registering resource
			this.logger.info(String.format(
					"Unregistering resource for alias '%s'.", 
					fullAlias
			));	
			this.http.unregister(fullAlias);
		} catch (Throwable e) {
			this.logger.error(String.format(
					"Unexpected '%s' while unregistering resource for alias '%s'.",
					fullAlias,
					alias
			),e);
		}
	}
	
	public void removeResource( String alias) {
		this.unregisterResource(alias);
	}
	
	/**
	 * Function to register all known servlets and resources
	 * @see #registerAllResources()
	 * @see #registerAllServlets()
	 */
	private void registerAll() {
		this.registerAllResources();
		this.registerAllServlets();
	}
	
	/**
	 * Function to register all known servlets. 
	 * This function loops through {@link #servlets} and calls 
	 * {@link #addServlet(String, Servlet, HttpContext, boolean)} for each entry.
	 */
	private void registerAllServlets() {
		// register all servlets
		for (ServiceReference servletRef: this.servlets.values()) {
			// register servlet to the http-service
			this.registerServlet(servletRef);
						
			// registering the servlet menu item
			this.registerMenuItem(servletRef);
		}	
	}
	
	/**
	 * Function to register all resources.
	 * This function loops through {@link #resources} and calls
	 * {@link #addResources(String, String, HttpContext, boolean)} for each entry.
	 * 
	 */
	private void registerAllResources() {
		// register all servlets
		for (Map.Entry<String, ResourceReference> entry : this.resources.entrySet()) {
			String alias = entry.getValue().alias;
			String name = entry.getValue().name;			
			HttpContext context = entry.getValue().context;
			
			this.registerResource(alias, name, context);
		}			
	}
		
	/**
	 * Function to unregister all servlets and resources
	 * @see #unregisterAllServlets()
	 * @see #unregisterAllResources()
	 */
	private void unregisterAll() {
		this.unregisterAllServlets();
		this.unregisterAllResources();
	}	
	
	/**
	 * Function to unregister all servlets
	 */
	private void unregisterAllServlets() {
		for (ServiceReference servletRef : this.servlets.values()) {
			// unregister servlet from the menu-manager
			this.unregisterMenuItem(servletRef);
			
			// unregister servlet from the http-service
			this.unregisterServlet(servletRef);
		}
	}	
	
	/**
	 * Function to unregister resources that were added
	 * to {@link #resources}
	 */
	public void unregisterAllResources() {
		for (Map.Entry<String, ResourceReference> entry : this.resources.entrySet()) {
			String alias = entry.getKey();
			this.unregisterResource(alias);
		}		
	}
	
	/**
	 * @see IServletManager#getServlets()
	 */
	public Map<String, Servlet> getServlets() {
		final HashMap<String, Servlet> servletList = new HashMap<String, Servlet>();
		for (Entry<String, ServiceReference> entry : this.servlets.entrySet()) {
			String alias = entry.getKey();
			Servlet servlet = (Servlet) this.context.locateService("servlets",entry.getValue());
			servletList.put(alias, servlet);
		}
		return servletList;
	}
	
	/**
	 * @see IServletManager#getResources()
	 */
	public Map<String, String> getResources() {
		final HashMap<String, String> resourceList = new HashMap<String, String>();
		for (Entry<String, ResourceReference> entry : this.resources.entrySet()) {
			String alias = entry.getKey();
			String name = entry.getValue().name;
			resourceList.put(alias, name);
		}
		return resourceList;
	}

	/**
	 * @see IServletManager#hasServlet(String)
	 */
	public boolean hasServlet(String alias) {
		return this.servlets.containsKey(alias);
	}

	private void changePath(String path) {
		if (path == null) path = "";
		if (path.length() > 1 && !path.startsWith("/")) path = "/" + path;
		if (path.endsWith("/")) path = path.substring(0, path.length() - 1);				
		
		if (!this.pathPrefix.equals(path)) {
			try {
				// allow the config-servlet to finish redirect
				Thread.sleep(1000);
			} catch (InterruptedException e) { /* ignore this */}
			
//			this.unregisterAll();
			this.pathPrefix = path;
//			this.registerAll();
		}
	}
	
	static class ResourceReference {
		public String alias;
		public String name;
		public HttpContext context;
		
		public ResourceReference(String alias, String name) {
			this(alias, name, null);
		}
		
		public ResourceReference(String alias, String name, HttpContext context) {
			this.alias = alias;
			this.name = name;
			this.context = context;
		}
	}

	public synchronized void bundleChanged(BundleEvent event) {
		if (event.getType() == BundleEvent.STOPPED) {
			final Long bundleId = Long.valueOf(event.getBundle().getBundleId());
			if (this.factories.containsKey(bundleId)) {
				this.factories.remove(bundleId);
			}
		}
		
	}
}
