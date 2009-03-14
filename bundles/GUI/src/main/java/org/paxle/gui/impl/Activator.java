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

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpContext;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.monitor.MonitorAdmin;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.paxle.core.io.IResourceBundleTool;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IMenuManager;
import org.paxle.gui.IServletManager;
import org.paxle.gui.IStyleManager;
import org.paxle.gui.impl.servlets.BundleView;
import org.paxle.gui.impl.servlets.ChartServlet;
import org.paxle.gui.impl.servlets.CrawlerView;
import org.paxle.gui.impl.servlets.LogView;
import org.paxle.gui.impl.servlets.LoginView;
import org.paxle.gui.impl.servlets.MonitorableView;
import org.paxle.gui.impl.servlets.OpenSearchDescription;
import org.paxle.gui.impl.servlets.OverView;
import org.paxle.gui.impl.servlets.QueueView;
import org.paxle.gui.impl.servlets.RobotsTxt;
import org.paxle.gui.impl.servlets.RootView;
import org.paxle.gui.impl.servlets.SearchView;
import org.paxle.gui.impl.servlets.ConfigView;
import org.paxle.gui.impl.servlets.StatusView;
import org.paxle.gui.impl.servlets.SysCtrl;
import org.paxle.gui.impl.servlets.TheaddumpView;
import org.paxle.gui.impl.servlets.UserView;
import org.paxle.gui.impl.tools.MetaDataTool;

public class Activator implements BundleActivator {

	private MenuManager menuManager = null;

	private StyleManager styleManager = null;

	private ServletManager servletManager = null;

	private ServiceTracker userAdminTracker = null;

	public void start(BundleContext bc) throws Exception {
		// initialize service Manager for toolbox usage (don't remove this!)
		// XXX how can we initialize this without using a static field?
		ServiceManager.context = bc;
		MetaDataTool.context = bc;

		// init user administration
		this.initUserAdmin(bc);

		// servlet manager
		this.servletManager = this.createAndInitServletManager(bc);
		
		// menu manager
		this.menuManager = new MenuManager(this.servletManager);
		bc.registerService(IMenuManager.class.getName(),this.menuManager, null);		

		// style manager
		this.initStyleManager(bc);

		// register classloader
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

		/*
		 * ========================================================== 
		 * Register Service Listeners
		 * ==========================================================
		 */
		bc.addServiceListener(new ServletListener(servletManager, menuManager, userAdminTracker, bc), ServletListener.FILTER);
		bc.addServiceListener(new HttpServiceListener(servletManager, bc), HttpServiceListener.FILTER);

		/*
		 * ========================================================== 
		 * Register Servlets 
		 * ==========================================================
		 */
		HttpContextAuth httpAuth = null;
		if (System.getProperty("org.paxle.gui.auth.skip") == null) {		
			httpAuth = new HttpContextAuth(bc.getBundle(), this.userAdminTracker);
		}
		
		registerServlet("/", new RootView(), null);
		registerServlet("/search", new SearchView(), "Search");
		// registerServlet( "/p2p", new P2PView(), "P2P");		
		
		registerServlet("/sysctrl", new SysCtrl(), "%menu.administration/%menu.system/Shutdown", httpAuth);
		registerServlet("/bundle", new BundleView(), "%menu.administration/%menu.system/%menu.bundleControl");	
		registerServlet("/users", new UserView(), "%menu.administration/%menu.system/%menu.userAdmin", httpAuth);
		
		registerServlet("/config", new ConfigView(), "%menu.administration/%menu.bundles/Config-Management", httpAuth);
		registerServlet("/crawler", new CrawlerView(), "%menu.administration/%menu.bundles/Crawler", httpAuth);
		registerServlet("/threads", new TheaddumpView(), null);
		
		registerServlet("/overview", new OverView(), "Info");
		registerServlet("/status", new StatusView(), "Info/Status");
		registerServlet("/queue", new QueueView(), "Info/Queues");
		registerServlet("/log", new LogView(bc), "Info/Log");
		
		registerServlet("/opensearch/osd.xml", new OpenSearchDescription(),null);
		registerServlet("/monitorable", new MonitorableView(), null);
		registerServlet("/login", new LoginView(), null);
		RobotsTxt rt = new RobotsTxt();
		registerServlet("/robots.txt", rt, null);
		
		/*
		 * Create configuration if not available
		 */
		ServiceReference cmRef = bc.getServiceReference(ConfigurationAdmin.class.getName());
		if (cmRef != null) {
			ConfigurationAdmin cm = (ConfigurationAdmin) bc.getService(cmRef);
			
			Configuration config = cm.getConfiguration(RobotsTxt.class.getName());
			if (config.getProperties() == null) {
				config.update(rt.getDefaults());
			}
		}
		
		/* 
		 * Register as managed service
		 */
		Hashtable<String,Object> msProps = new Hashtable<String, Object>();
		msProps.put(Constants.SERVICE_PID, RobotsTxt.class.getName());
		bc.registerService(ManagedService.class.getName(), rt, msProps);
		
		//this servlet may register with delay and is therefore registered at the end
		this.initChartServlet(bc);
	}
	
	

	private void registerServlet(final String location, final ALayoutServlet servlet, final String menuName) {
		this.registerServlet(location, servlet, menuName, null);
	}

	private void registerServlet(final String location, final ALayoutServlet servlet, final String menuName, HttpContext context) {
		servlet.setBundleLocation(null);
		
		// cut of query parameters (if any)
		String path = location.split("[?#]")[0];
	
		// register servlet (if not already registered)
		if (!this.servletManager.hasServlet(path)) {
			this.servletManager.addServlet(path, servlet, context);
		}
		
		// register menu item
		if (menuName != null) {
			menuManager.addItem(
					// path to the servlet
					location, 
					// name of the menu (and submenu)
					menuName
			);
		}
	}

	/**
	 * This function
	 * <ul>
	 * <li>initialized the {@link StyleManager}</li>
	 * <li>creates a default {@link Configuration} if no {@link Configuration}
	 * exists</li>
	 * <li>registers the {@link StyleManager} as {@link ManagedService}</li>
	 * <li>registers the {@link StyleManager} as {@link MetaTypeProvider}</li>
	 * </ul>
	 * 
	 * @param context
	 *            the bundle context needed for service registration
	 * @throws IOException
	 */
	private void initStyleManager(BundleContext context) throws IOException {
		final ServiceReference btRef = context.getServiceReference(IResourceBundleTool.class.getName());
		final IResourceBundleTool bt = (IResourceBundleTool) context.getService(btRef); 
		
		// find available locales for metatye-translation
		String[] supportedLocale = bt.getLocaleArray(IStyleManager.class.getSimpleName(), Locale.ENGLISH);		
		
		final String dataPath = System.getProperty("paxle.data") + File.separatorChar + "styles";
		final File styleDataDir = new File(dataPath);
		
		// create the style manager
		this.styleManager = new StyleManager(
				styleDataDir,
				this.servletManager,
				supportedLocale
		);

		// service properties for registration
		Hashtable<String, Object> styleManagerProps = new Hashtable<String, Object>();
		styleManagerProps.put(Constants.SERVICE_PID, StyleManager.PID);

		// register as services
		context.registerService(IStyleManager.class.getName(),this.styleManager, null);
		context.registerService(new String[] {
				ManagedService.class.getName(),
				MetaTypeProvider.class.getName() 
		}, this.styleManager, styleManagerProps);
	}

	@SuppressWarnings("unchecked")
	private void initUserAdmin(BundleContext context) throws IOException {
		// create a tracker used by the http-context
		this.userAdminTracker = new ServiceTracker(context, UserAdmin.class.getName(), null);
		this.userAdminTracker.open();

		// create a admin role if needed
		UserAdmin userAdmin = (UserAdmin) this.userAdminTracker.getService();
		if (userAdmin != null) {
			// check if an Administrator group is already available
			Group admins = (Group) userAdmin.getRole("Administrators");
			if (admins == null) {
				admins = (Group) userAdmin.createRole("Administrators",Role.GROUP);
			}

			User admin = (User) userAdmin.getRole("Administrator");
			if (admin == null) {
				// create a default admin user
				admin = (User) userAdmin.createRole("Administrator", Role.USER);
				admins.addMember(admin);

				// configure http-login data
				Dictionary<String, Object> props = admin.getProperties();
				props.put(HttpContextAuth.USER_HTTP_LOGIN, "admin");

				Dictionary<String, Object> credentials = admin.getCredentials();
				credentials.put(HttpContextAuth.USER_HTTP_PASSWORD, "");
			}
		}
	}
	
	private ServletManager createAndInitServletManager(BundleContext bc) {
		// creating component
		ServletManager sManager = new ServletManager(bc.getBundle().getEntry("/").toString());
		
		// managed- and metatype-provider-service properties
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put(Constants.SERVICE_PID, ServletManager.PID);		
		
		// register the filter-manager as service
		bc.registerService(IServletManager.class.getName(), sManager, null);
		bc.registerService(new String[]{ManagedService.class.getName()}, sManager, props);				
		
		return sManager;
	}
	
	/**
	 * The external library jfreechart is optional. Therfore we need to wait until the
	 * bundle containing this library is available
	 * 
	 * @param context
	 */
	private void initChartServlet(final BundleContext context) {
		new ChartServletInitializer(context);
	}
	
	private class ChartServletInitializer implements BundleListener, ServiceTrackerCustomizer {
		private BundleContext bc;
		private boolean jfreeChartAvailable = false;
		private MonitorAdmin monitorAdmin = null;
		private ChartServlet servlet = null;
		
		public ChartServletInitializer(final BundleContext context) {
			this.bc = context;
			
			// registering as servicetracker-customizer
			ServiceTracker st = new ServiceTracker(
					this.bc,
					"org.osgi.service.monitor.MonitorAdmin",
					this
			);
			st.open();
			
			// lookup if jfree bundle is available
			Bundle[] bundles = context.getBundles();
			for (Bundle bundle : bundles) {
				// register the servlet if the jfree bundle is already installed
				if (bundle.getSymbolicName().equalsIgnoreCase("com.springsource.org.jfree")) {
					this.jfreeChartAvailable = true;
					this.stateChanged();
				}
			}
		}

		public void bundleChanged(BundleEvent event) {
			if (event.getBundle().getSymbolicName().equals("com.springsource.org.jfree")) {
				if (event.getType() == BundleEvent.RESOLVED) {
					jfreeChartAvailable = true;
				} else if (event.getType() == BundleEvent.STOPPED || event.getType() == BundleEvent.UNINSTALLED) {
					jfreeChartAvailable = false;
				}
			}
			this.stateChanged();
		}

		public Object addingService(ServiceReference ref) {
			this.monitorAdmin = (MonitorAdmin) this.bc.getService(ref);
			this.stateChanged();
			return ref;
		}
		

		public void modifiedService(ServiceReference ref, Object service) {
			// not needed
		}		

		public void removedService(ServiceReference ref, Object service) {
			this.monitorAdmin = null;
			this.stateChanged();
		}
		
		private void stateChanged() {
			if (servlet == null && this.jfreeChartAvailable && this.monitorAdmin != null) {
				// creating servlet
				ChartServlet servlet = new ChartServlet(this.bc, this.monitorAdmin);

				// register servlet as http-servlet
				servletManager.addServlet("/chart", servlet, null);
			} else if (servlet != null && (!this.jfreeChartAvailable || this.monitorAdmin == null)){
				this.servlet = null;
				
				// unregister servlet
				servletManager.removeServlet("/chart");
			}
		}
	}

	public void stop(BundleContext context) throws Exception {
		// unregister all servlets
		servletManager.close();

		this.userAdminTracker.close();

		// cleanup
		ServiceManager.context = null;
		MetaDataTool.context = null;
		this.servletManager = null;
		this.styleManager = null;
		this.menuManager = null;
	}
}