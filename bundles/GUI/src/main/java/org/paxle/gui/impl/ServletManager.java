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
package org.paxle.gui.impl;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.tools.view.VelocityView;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.useradmin.UserAdmin;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IMenuManager;
import org.paxle.gui.IServletManager;

/**
 * @scr.component immediate="true" metatype="false" name="org.paxle.gui.IServletManager"
 * @scr.service interface="org.paxle.gui.IServletManager"
 * @scr.property name="org.paxle.gui.IServletManager.pathPrefix" value=""
 * @scr.reference name="servlets" 
 * 				  interface="javax.servlet.Servlet" 
 * 				  cardinality="0..n" 
 * 				  policy="dynamic" 
 * 				  bind="addServlet" 
 * 				  unbind="removeServlet"
 * 				  target="(path=*)"
 */
public class ServletManager implements IServletManager {
	
	/**
	 * All registeres {@link Servlet servlets}
	 */
	private HashMap<String,ServiceReference> servlets = new HashMap<String,ServiceReference>();
	
	/**
	 * All registered resources
	 */
	private HashMap<String, ResourceReference> resources = new HashMap<String, ResourceReference>();
	
	/**
	 * The OSGI {@link HttpService Http-Service}
	 * @scr.reference
	 */
	private HttpService http;
	
	/**
	 * The OSGI {@link UserAdmin} service required for authentication
	 * @see #createHttpContext(ServiceReference)
	 * @scr.reference
	 */
	private UserAdmin userAdmin;
	
	/**
	 * @scr.reference
	 */
	private IMenuManager menuManager;
	
	/**
	 * Default Servlet properties
	 */
	private Hashtable<String, String> defaultProps;
	
	/**
	 * The default context that is used to register servlets and resources.
	 * Leaving this to <code>null</code> means that the {@link HttpService}
	 * creates a new context for each resource/servlet
	 */
	private HttpContext defaultContext = null;
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A prefix that should be used for each servlet- and resource-alias
	 */
	private String pathPrefix = "";
	
	private ComponentContext context;
	
	protected void activate(ComponentContext context) {
		this.context = context;
		
		// the default location to use for template-loading
		String defaultBundleLocation = context.getBundleContext().getBundle().getEntry("").toString();		
		if (defaultBundleLocation != null && defaultBundleLocation.endsWith("/")) {
			defaultBundleLocation = defaultBundleLocation.substring(0,defaultBundleLocation.length()-1);
		}
		
		// add some properties to the servlet props
		this.defaultProps = new Hashtable<String, String>();
		
		this.defaultProps.put(VelocityView.PROPERTIES_KEY, "/resources/config/velocity.properties");
		this.defaultProps.put(VelocityView.TOOLS_KEY, "/resources/config/velocity-toolbox.properties");
		this.defaultProps.put(VelocityView.LOAD_DEFAULTS_KEY, "false");
		
		this.defaultProps.put("bundle.location.default",defaultBundleLocation);
		
		// getting the path prefix to use
		String newPathPrefix = (String) context.getProperties().get("org.paxle.gui.IServletManager.pathPrefix");
		this.changePath(newPathPrefix);		
		
		// register all servlets/resources
		this.registerAll();
	}
	
	protected void deactivate(ComponentContext context) {
		// unregister all servlets/resources
		this.unregisterAll();
		
		// clear properties
		this.defaultProps.clear();
	}
	
	protected void addServlet(ServiceReference servletRef) {
		// remember the servlet in our internal servlet-list
		final String path = (String)servletRef.getProperty("path");
		this.servlets.put(path, servletRef);
		
		// registering the servlet to the http-service
		this.registerServlet(servletRef);
		
		// registering the servlet menu item
		this.registerMenuItem(servletRef);
	}
	
	protected void removeServlet(ServiceReference servletRef) {
		// unregistering the servlet menu item
		this.unregisterMenuItem(servletRef);
		
		// removing the servlet from our internal servlet-list
		final String path = (String)servletRef.getProperty("path");
		this.servlets.remove(path);
		
		// unregistering the servlet from the http-service
		this.unregisterServlet(servletRef);
	}
	
	private HttpContext createHttpAuthContext(ServiceReference servletRef) {
		// checking if we need an authentication
		Boolean userAuth = (Boolean)servletRef.getProperty("doUserAuth");		
		if (userAuth == null) userAuth = Boolean.FALSE;
		if (!userAuth.booleanValue()) return null;
		
		// getting the bundle the servlet belongs to
		Bundle bundle = servletRef.getBundle();
		
		// creating an authentication context
		return new HttpContextAuth(bundle, this.userAdmin);
	}
	
	private void registerMenuItem(ServiceReference servletRef) {
		if (this.menuManager == null) return;
		
		String menuName = (String)servletRef.getProperty("menu");
		if (menuName == null || menuName.length() == 0) return;
		
		Integer menuPos = (Integer)servletRef.getProperty("menu.pos");
		if (menuPos == null) menuPos = Integer.valueOf(IMenuManager.DEFAULT_MENU_POS);
		
		// getting the path to use
		String path = (String)servletRef.getProperty("path");
		
		// convert it into a full alias (pathprefix + alias)
		String fullAlias = this.getFullAlias(path);		
		
		String resourceBundleBase = null;
		ClassLoader resourceBundleLoader = null;
		
		if (menuName.startsWith("%") || menuName.contains("/%")) {
			/* 
			 * The menu-name needs to be localized.
			 * We are trying to finde the proper resource-bundle to use
			 */
			
			// the resource-bundle basename
			resourceBundleBase = (String) servletRef.getProperty("menu-localization");
			if (resourceBundleBase == null)
				resourceBundleBase = (String) servletRef.getBundle().getHeaders().get(Constants.BUNDLE_LOCALIZATION);
			if (resourceBundleBase == null)
				resourceBundleBase = Constants.BUNDLE_LOCALIZATION_DEFAULT_BASENAME;
			
			// the classloader to use
			resourceBundleLoader = servletRef.getBundle().getBundleContext().getService(servletRef).getClass().getClassLoader();
		}
		this.menuManager.addItem(fullAlias, menuName, resourceBundleBase, resourceBundleLoader, menuPos.intValue());
	}
	
	private void unregisterMenuItem(ServiceReference servletRef) {
		if (this.menuManager == null) return;
		
		String menuName = (String)servletRef.getProperty("menu");
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
		
	private void registerServlet(ServiceReference servletRef) {
		if (this.http == null) return;
		if (this.context == null) return;
		
		String fullAlias = null;
		String servletClass = null;
		try {
			// getting the servlet class
			Servlet servlet = (Servlet) this.context.locateService("servlets", servletRef);
			servletClass = servlet.getClass().getName();
			
			// getting the path to use
			final String path = (String)servletRef.getProperty("path");
			
			// convert it into a full alias (pathprefix + alias)
			fullAlias = this.getFullAlias(path);
			
			// getting the httpContext to use
			HttpContext httpContext = this.createHttpAuthContext(servletRef);
			
			// init servlet properties
			@SuppressWarnings("unchecked")
			Hashtable<String, String> props = (Hashtable<String, String>) this.defaultProps.clone();
			if (servlet instanceof ALayoutServlet) {
				Bundle bundle = servletRef.getBundle();
				BundleContext bundleContext = bundle.getBundleContext();
				
				// configuring the bundle location to use				
				final String bundleLocation = servletRef.getBundle().getEntry("").toString();
				props.put("bundle.location", bundleLocation);
				
				// injecting the velocity-view factory
				((ALayoutServlet)servlet).setVelocityViewFactory(new VelocityViewFactory(bundleContext));
			}
			
			// registering the servlet
			this.logger.info(String.format(
					"Registering servlet '%s' for alias '%s'.", 
					servlet.getClass().getName(), 
					fullAlias
			));
			this.http.registerServlet(fullAlias, servlet, props, httpContext);
		} catch (Throwable e) {
			this.logger.error(String.format(
					"Unexpected '%s' while registering servlet '%s' for alias '%s'.",
					e.getClass().getName(),
					servletClass,
					fullAlias
			),e);
		}
	}
	
	private void unregisterServlet(ServiceReference servletRef) {
		if (this.http == null) return;
		
		String fullAlias = null;
		String servletClass = null;
		try {
			// getting the path of the servlet
			final String path = (String)servletRef.getProperty("path");
			
			// convert it into a full alias (pathprefix + alias)
			fullAlias = this.getFullAlias(path);
			
			// unregistering the servlet
			this.logger.info(String.format(
					"Unregistering servlet '%s' for alias '%s'.", 
					servletClass, 
					fullAlias
			));
			this.http.unregister(fullAlias);
		} catch (Throwable e) {
			this.logger.error(String.format("Unexpected '%s' while unregistering servlet '%s' for alias '%s'.",
					e.getClass().getName(),
					servletClass,
					fullAlias
			),e);
		}
	}
	
	public void addResources(String alias, String name) {
		this.addResources(alias, name, this.defaultContext);
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
	private synchronized void unregisterAllServlets() {
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
	public synchronized void unregisterAllResources() {
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
			
			this.unregisterAll();
			this.pathPrefix = path;
			this.registerAll();
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
}
