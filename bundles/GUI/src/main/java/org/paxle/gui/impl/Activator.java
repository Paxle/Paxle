package org.paxle.gui.impl;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpContext;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IMenuManager;
import org.paxle.gui.IServletManager;
import org.paxle.gui.IStyleManager;
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

	private StyleManager styleManager = null;
	
	private ServletManager servletManager = null;

	public void start( BundleContext bc) throws Exception {
		// initialize service Manager for toolbox usage (don't remove this!)
		ServiceManager.context = bc;

		// GUI menu manager
		this.menuManager = new MenuManager();
		bc.registerService( IMenuManager.class.getName(), this.menuManager, null);

		// servlet manager
		this.servletManager = new ServletManager(bc.getBundle().getEntry("/").toString());
		bc.registerService( IServletManager.class.getName(), this.servletManager, null);

		// style manager
		this.styleManager = new StyleManager(new File("styles"),this.servletManager);
		bc.registerService(IStyleManager.class.getName(), this.styleManager, null);
		
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
		this.styleManager.setStyle("default");
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
		this.servletManager = null;
		this.styleManager = null;
		this.menuManager = null;
	}
}