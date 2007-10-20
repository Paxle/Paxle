package org.paxle.gui.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.paxle.gui.IMenuManager;
import org.paxle.gui.impl.servlets.BundleView;
import org.paxle.gui.impl.servlets.CrawlerView;
import org.paxle.gui.impl.servlets.LogView;
import org.paxle.gui.impl.servlets.P2PView;
import org.paxle.gui.impl.servlets.QueueView;
import org.paxle.gui.impl.servlets.SearchView;
import org.paxle.gui.impl.servlets.StatusView;

public class Activator implements BundleActivator {

	private static BundleContext bc;
	private static HttpService http;
	private static ServiceManager manager = null;
	private static MenuManager menuManager = null;

	public void start(BundleContext context) throws Exception {		
		bc = context;		
		manager = new ServiceManager(bc);
		
		// GUI menu manager
		menuManager = new MenuManager();
		bc.registerService(IMenuManager.class.getName(), menuManager, null);
		
		// getting a reference to the osgi http service
		ServiceReference sr = bc.getServiceReference(HttpService.class.getName());
		if(sr != null) {
			// getting the http service
			http = (HttpService)bc.getService(sr);
			if(http != null) {											
				// add some properties to the servlet props
				Hashtable<String, String> props = new Hashtable<String, String>();
				props.put("org.apache.velocity.properties", "/resources/velocity.properties");
				props.put("bundle.location",context.getBundle().getLocation());
				
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
				menuManager.addItem("/search", "Search");
				menuManager.addItem("/status", "Status");
				menuManager.addItem("/p2p", "P2P");
				menuManager.addItem("/crawler", "Crawler");
				menuManager.addItem("/bundle", "Bundles");
				menuManager.addItem("/log", "Logging");
				menuManager.addItem("/queue", "Queues");				
				
				
				// registering the servlet which will be accessible using 
                http.registerServlet("/search", new SearchView(manager), props, httpContext);
				http.registerServlet("/status", new StatusView(manager), props, httpContext);
                http.registerServlet("/p2p", new P2PView(manager), props, httpContext);
                http.registerServlet("/crawler", new CrawlerView(manager), props, httpContext);
                http.registerServlet("/bundle", new BundleView(manager), props, httpContext);
                http.registerServlet("/log", new LogView(manager), props, httpContext);
                http.registerServlet("/queue", new QueueView(manager), props, httpContext);
                http.registerServlet("/", new SearchView(manager), props, httpContext);
                http.registerServlet("/index.html", new SearchView(manager), props, httpContext);
                
                http.registerResources("/style.css", "resources/templates/layout/style.css", httpContext);

			}
		}		
	}

	public void stop(BundleContext context) throws Exception {
		// unregister servlet
		http.unregister("/status");
		http.unregister("/crawler");	
		http.unregister("/bundle");
		http.unregister("/log");
		
		// cleanup
		manager = null;
		menuManager = null;
		http = null;
		bc = null;
	}
}