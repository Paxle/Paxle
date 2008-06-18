
package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.filter.IFilter;
import org.paxle.filter.blacklist.BlacklistServlet;

public class Activator implements BundleActivator {
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext bc) throws Exception {
		
//		File list = bc.getDataFile("../../../blacklist");
		File list = new File("blacklist");
		list.mkdirs();
		new File(list, "default.list");
		
		/*
		 * Registering the filter
		 */
		Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {"org.paxle.crawler.in", "org.paxle.parser.out"});
		BlacklistFilter blacklistFilter = new BlacklistFilter(list);
		bc.registerService(IFilter.class.getName(), blacklistFilter, filterProps);
		
		/*
		 * Registering the bundle listener
		 */
		bc.addBundleListener(new DesktopIntegrationListener(bc));
		
		/*
		 * Registering the servlet
		 */
		BlacklistServlet servlet = new BlacklistServlet(blacklistFilter);
		servlet.init(bc.getBundle().getEntry("/").toString(),"/blacklist");
		Hashtable<String, String> props = new Hashtable<String, String>();
		props.put("path", "/blacklist");
		props.put("menu", "Blacklist");
		bc.registerService("javax.servlet.Servlet", servlet, props);
	}
	
	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		/* nothing todo here */
	}
}