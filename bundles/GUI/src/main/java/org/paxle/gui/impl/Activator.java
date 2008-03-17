package org.paxle.gui.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IMenuManager;
import org.paxle.gui.IServletManager;
import org.paxle.gui.impl.servlets.BundleView;
import org.paxle.gui.impl.servlets.CrawlerView;
import org.paxle.gui.impl.servlets.LogView;
import org.paxle.gui.impl.servlets.OpenSearchDescription;
import org.paxle.gui.impl.servlets.OverView;
import org.paxle.gui.impl.servlets.P2PView;
import org.paxle.gui.impl.servlets.QueueView;
import org.paxle.gui.impl.servlets.RootView;
import org.paxle.gui.impl.servlets.SearchView;
import org.paxle.gui.impl.servlets.SettingsView;
import org.paxle.gui.impl.servlets.StatusView;
import org.paxle.gui.impl.servlets.TheaddumpView;

public class Activator implements BundleActivator {

	private static BundleContext bc;
	private static HttpService http;
	private static MenuManager menuManager = null;
	private static ServletManager servletManager = null; 
	
	public void start(BundleContext context) throws Exception {		
		bc = context;		
		
		// initialize service Manager for toolbox usage (don't remove this!)
		ServiceManager.context = bc;
		
		// GUI menu manager
		menuManager = new MenuManager();
		bc.registerService(IMenuManager.class.getName(), menuManager, null);
		
		// servlet manager
		servletManager = new ServletManager(menuManager, context.getBundle().getLocation());	
		bc.registerService(IServletManager.class.getName(),servletManager,null);
		
		// register classloader
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());		
		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */
		bc.addServiceListener(new ServletListener(servletManager, menuManager, bc),ServletListener.FILTER);
		bc.addServiceListener(new HttpServiceListener(servletManager, bc),HttpServiceListener.FILTER);
		
		// getting a reference to the osgi http service
		ServiceReference sr = bc.getServiceReference(HttpService.class.getName());
		if(sr != null) {
			// getting the http service
			http = (HttpService)bc.getService(sr);
			if(http != null) {											
				// add some properties to the servlet props
				Hashtable<String, String> props = new Hashtable<String, String>();
				props.put("org.apache.velocity.properties", "/resources/config/velocity.properties");
				props.put("org.apache.velocity.toolbox", "/resources/config/velocity.toolbox");
				props.put("bundle.location",context.getBundle().getLocation());
				
				@SuppressWarnings("unused")
				HttpContext httpContext = null;
 
				/* TODO: do user authentication here.
				 * Please read http://www2.osgi.org/javadoc/r4/index.html to see how this could be used in combination 
				 * with the OSGI User Admin Service (http://www2.osgi.org/javadoc/r4/org/osgi/service/useradmin/package-summary.html)
				 */
//				httpContext = new HttpContext() {
//					/**
//					 * @see <a href="http://www2.osgi.org/javadoc/r4/index.html">HttpContext.handleSecurity</a>
//					 */
//					public boolean handleSecurity(HttpServletRequest  request, HttpServletResponse response) 
//					throws java.io.IOException {
//						// TODO: authenticate the user here ...
//						if (false) {
//							response.setStatus(401);
//							response.setHeader("WWW-Authenticate", "Basic realm=\"paxle log-in\"");
//							return false;
//						}
//						return true;
//					}
//
//					public String getMimeType(String arg0) {
//						return null;
//					}
//
//					public URL getResource(String arg0) {
//						return bc.getBundle().getResource(arg0);
//					}
//				};
								
				// configure menu
				// TODO: this will be generated dynamically later
				/*
				menuManager.addItem("/search", "Search");
				menuManager.addItem("/status", "Status");
				menuManager.addItem("/p2p", "P2P");
				menuManager.addItem("/crawler", "Crawler");
				menuManager.addItem("/bundle", "Bundles");
				menuManager.addItem("/log", "Logging");
				menuManager.addItem("/queue", "Queues");				
				menuManager.addItem("/config", "Settings");
				menuManager.addItem("/overview", "Overview");
				*/
				
				// registering the servlet which will be accessible using 
//                http.registerServlet("/search", new SearchView(manager), props, httpContext);
//				  http.registerServlet("/status", new StatusView(manager), props, httpContext);
//                http.registerServlet("/p2p", new P2PView(manager), props, httpContext);
//                http.registerServlet("/crawler", new CrawlerView(manager), props, httpContext);
//                http.registerServlet("/bundle", new BundleView(manager), props, httpContext);
//                http.registerServlet("/log", new LogView(manager), props, httpContext);
//                http.registerServlet("/queue", new QueueView(manager), props, httpContext);
//                http.registerServlet("/", new SearchView(manager), props, httpContext);
//                http.registerServlet("/index.html", new SearchView(manager), props, httpContext);                
//                http.registerResources("/style.css", "resources/templates/layout/style.css", httpContext);

			}
			
			HttpContext httpContextAuth = new HttpContextAuth( bc.getBundle());
			
			/* ==========================================================
			 * Register Servlets
			 * ========================================================== */
			/*
			servletManager.addServlet("/", new RootView(null));
			servletManager.addServlet("/search", new SearchView(null));
			servletManager.addServlet("/status", new StatusView(null));
			servletManager.addServlet("/p2p", new P2PView(null));
			servletManager.addServlet("/crawler", new CrawlerView(null));
			servletManager.addServlet("/bundle", new BundleView(null));
			servletManager.addServlet("/log", new LogView(null), httpContextAuth);
			servletManager.addServlet("/queue", new QueueView(null));
			servletManager.addServlet("/opensearch/osd.xml", new OpenSearchDescription(null));
			servletManager.addServlet("/config", new SettingsView(null));
			servletManager.addServlet("/threads", new TheaddumpView(null));
			servletManager.addServlet("/overview", new OverView(null));
			*/
			registerServlet("/", new RootView(), null);
			registerServlet("/search", new SearchView(), "Search");
			registerServlet("/status", new StatusView(), "Status");
			registerServlet("/p2p", new P2PView(), "P2P");
			registerServlet("/crawler", new CrawlerView(), "Crawler");
			registerServlet("/bundle", new BundleView(), "Bundles");
			registerServlet("/log", new LogView(), "Logging");
			registerServlet("/queue", new QueueView(), "Queues");
			registerServlet("/opensearch/osd.xml", new OpenSearchDescription(), null);
			registerServlet("/config", new SettingsView(), "Settings");
			registerServlet("/threads", new TheaddumpView(), null);
			registerServlet("/overview", new OverView(), "Overview");
			
			/*
			 * Add stylesheets
			 * TODO: use some kind of addResourceFolder instead
			 */
//			servletManager.addResources("/style.css", "/resources/templates/layout/style.css");
			servletManager.addResources("/css","/resources/templates/layout/css");
			
			/*
			 * Add javascript files
			 * TODO: use some kind of addResourceFolder instead
			 */
//			servletManager.addResources("/js/wz_tooltip.js", "/resources/js/wz_tooltip.js");
			servletManager.addResources("/js","/resources/js");
			
			/*
			 * Add images 
			 * TODO: use some kind of addResourceFolder instead
			 */
//			servletManager.addResources("/images/bullet_arrow_up.png", "/resources/images/bullet_arrow_up.png");
			servletManager.addResources("/images", "/resources/images");

		}
	}
	
	private static void registerServlet(final String location, final ALayoutServlet servlet, final String menuName) {
		servlet.init(null, location);
		servletManager.addServlet(location, servlet);
		if (menuName != null && http != null)
			menuManager.addItem(location, menuName);
	}

	public void stop(BundleContext context) throws Exception {
		// unregister all servlets
		servletManager.close();
		
		// cleanup
		menuManager = null;
		http = null;
		bc = null;
	}
	
	public static ServletManager getServletManager() {
		
		return servletManager;
		
	}
}