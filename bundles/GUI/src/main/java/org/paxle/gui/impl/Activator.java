package org.paxle.gui.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpContext;
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

	private MenuManager menuManager = null;

	private static ServletManager servletManager = null;

	public void start( BundleContext bc) throws Exception {
		// initialize service Manager for toolbox usage (don't remove this!)
		ServiceManager.context = bc;

		// GUI menu manager
		menuManager = new MenuManager();
		bc.registerService( IMenuManager.class.getName(), menuManager, null);

		// servlet manager
		servletManager = new ServletManager( menuManager, bc.getBundle().getEntry("/").toString());
		bc.registerService( IServletManager.class.getName(), servletManager, null);

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
		 * User authentication here. Please read
		 * http://www2.osgi.org/javadoc/r4/index.html to see how this could
		 * be used in combination with the OSGI User Admin Service
		 * (http://www2.osgi.org/javadoc/r4/org/osgi/service/useradmin/package-summary.html)
		 */

		HttpContext httpContextAuth = new HttpContextAuth( bc.getBundle());

		/*
		 * ==========================================================
		 * Register Servlets
		 * ==========================================================
		 */
		registerServlet( "/", new RootView(), null);
		registerServlet( "/search", new SearchView(), "Search");
		registerServlet( "/status", new StatusView(), "Status");
		registerServlet( "/p2p", new P2PView(), "P2P");
		registerServlet( "/crawler", new CrawlerView(), "Crawler");
		registerServlet( "/bundle", new BundleView(), "Bundles");
		registerServlet( "/log", new LogView(), "Logging");
		registerServlet( "/queue", new QueueView(), "Queues");
		registerServlet( "/opensearch/osd.xml", new OpenSearchDescription(), null);
		registerServlet( "/config", new SettingsView(), "Settings");
		registerServlet( "/threads", new TheaddumpView(), null);
		registerServlet( "/overview", new OverView(), "Overview");

		// load the current style
		StyleManager.setStyle( "default");
	}


	private void registerServlet( final String location, final ALayoutServlet servlet, final String menuName) {
		servlet.init( null, location);
		servletManager.addServlet( location, servlet);
		if (menuName != null) {
			menuManager.addItem( location, menuName);
		}
	}


	public void stop( BundleContext context) throws Exception {
		// unregister all servlets
		servletManager.close();

		// cleanup
		ServiceManager.context = null;
		servletManager = null;
		menuManager = null;
	}


	public static ServletManager getServletManager() {
		return servletManager;
	}
}