package org.paxle.gui.impl;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpContext;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IMenuManager;
import org.paxle.gui.IServletManager;
import org.paxle.gui.IStyleManager;
import org.paxle.gui.impl.servlets.BundleView;
import org.paxle.gui.impl.servlets.CrawlerView;
import org.paxle.gui.impl.servlets.LogView;
import org.paxle.gui.impl.servlets.LoginView;
import org.paxle.gui.impl.servlets.OpenSearchDescription;
import org.paxle.gui.impl.servlets.OverView;
import org.paxle.gui.impl.servlets.P2PView;
import org.paxle.gui.impl.servlets.QueueView;
import org.paxle.gui.impl.servlets.RootView;
import org.paxle.gui.impl.servlets.SearchView;
import org.paxle.gui.impl.servlets.SettingsView;
import org.paxle.gui.impl.servlets.StatusView;
import org.paxle.gui.impl.servlets.SysDown;
import org.paxle.gui.impl.servlets.TheaddumpView;


public class Activator implements BundleActivator {

	private MenuManager menuManager = null;

	private StyleManager styleManager = null;
	
	private ServletManager servletManager = null;
	
	private ServiceTracker userAdminTracker = null;

	public void start( BundleContext bc) throws Exception {
		// initialize service Manager for toolbox usage (don't remove this!)
		ServiceManager.context = bc;

		// init user administration
		this.initUserAdmin(bc);
		
		// GUI menu manager
		this.menuManager = new MenuManager();
		bc.registerService( IMenuManager.class.getName(), this.menuManager, null);

		// servlet manager
		this.servletManager = new ServletManager(bc.getBundle().getEntry("/").toString());
		bc.registerService( IServletManager.class.getName(), this.servletManager, null);

		// style manager
		this.initStyleManager(bc);
		
		// register classloader
		Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader());

		/*
		 * ========================================================== Register
		 * Service Listeners
		 * ==========================================================
		 */
		bc.addServiceListener( new ServletListener( servletManager, menuManager, bc), ServletListener.FILTER);
		bc.addServiceListener( new HttpServiceListener( servletManager, bc), HttpServiceListener.FILTER);

		/*
		 * ==========================================================
		 * Register Servlets
		 * ==========================================================
		 */
		registerServlet( "/", new RootView(), null);
		registerServlet( "/search", new SearchView(), "Search");
		registerServlet( "/status", new StatusView(), "Status");
//		registerServlet( "/p2p", new P2PView(), "P2P");
		registerServlet( "/crawler", new CrawlerView(), "Crawler", new HttpContextAuth(bc.getBundle() ,this.userAdminTracker));
		registerServlet( "/bundle", new BundleView(), "Bundles");
		registerServlet( "/log", new LogView(), "Logging");
		registerServlet( "/queue", new QueueView(), "Queues");
		registerServlet( "/opensearch/osd.xml", new OpenSearchDescription(), null);
		registerServlet( "/config", new SettingsView(), "Settings", new HttpContextAuth(bc.getBundle() ,this.userAdminTracker));
		registerServlet( "/threads", new TheaddumpView(), null);
		registerServlet( "/overview", new OverView(), "Overview");
		registerServlet( "/sysdown", new SysDown(), null, new HttpContextAuth(bc.getBundle() ,this.userAdminTracker));
		registerServlet( "/login", new LoginView(), null);
	}

	private void registerServlet( final String location, final ALayoutServlet servlet, final String menuName) {
		this.registerServlet(location, servlet, menuName,null);
	}

	private void registerServlet( final String location, final ALayoutServlet servlet, final String menuName, HttpContext context) {
		servlet.init( null, location);
		servletManager.addServlet(location, servlet, context);
		if (menuName != null) {
			menuManager.addItem( location, menuName);
		}
	}

	/**
	 * This function
	 * <ul>
	 * <li>initialized the {@link StyleManager}</li>
	 * <li>creates a default {@link Configuration} if no {@link Configuration} exists</li>
	 * <li>registers the {@link StyleManager} as {@link ManagedService}</li>
	 * <li>registers the {@link StyleManager} as {@link MetaTypeProvider}</li>
	 * </ul>
	 * 
	 * @param context the bundle context needed for service registration
	 * @throws IOException
	 */
	private void initStyleManager(BundleContext context) throws IOException {
		// create the style manager
		this.styleManager = new StyleManager(new File("styles"),this.servletManager);
		
		// get the config-admin service and set the default configuration if not available
		ServiceReference cmRef = context.getServiceReference(ConfigurationAdmin.class.getName());
		if (cmRef != null) {
			ConfigurationAdmin cm = (ConfigurationAdmin) context.getService(cmRef);
			Configuration config = cm.getConfiguration(StyleManager.PID);
			if (config.getProperties() == null) {
				config.update(this.styleManager.getDefaults());
			}
		}		
		
		// service properties for registration
		Hashtable<String, Object> styleManagerProps = new Hashtable<String, Object>();
		styleManagerProps.put(Constants.SERVICE_PID, StyleManager.PID);
		
		// register as services
		context.registerService(IStyleManager.class.getName(), this.styleManager, null);
		context.registerService(new String[]{ManagedService.class.getName(),MetaTypeProvider.class.getName()}, this.styleManager, styleManagerProps);
	}
	
	@SuppressWarnings("unchecked")
	private void initUserAdmin(BundleContext context) throws IOException {
		// create a tracker used by the http-context
		this.userAdminTracker = new ServiceTracker(context, UserAdmin.class.getName(),null);
		this.userAdminTracker.open();
		
		// create a admin role if needed
		UserAdmin userAdmin = (UserAdmin) this.userAdminTracker.getService();
		if (userAdmin != null) {
			// check if an Administrator group is already available
			Group admins = (Group) userAdmin.getRole("Administrators");
			if (admins == null) {
				admins = (Group) userAdmin.createRole("Administrators", Role.GROUP);
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

	public void stop( BundleContext context) throws Exception {
		// unregister all servlets
		servletManager.close();
		
		this.userAdminTracker.close();

		// cleanup
		ServiceManager.context = null;
		this.servletManager = null;
		this.styleManager = null;
		this.menuManager = null;
	}
}