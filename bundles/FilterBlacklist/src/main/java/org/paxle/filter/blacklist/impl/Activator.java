
package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.filter.IFilter;
import org.paxle.filter.blacklist.impl.desktop.DesktopIntegrationListener;
import org.paxle.filter.blacklist.impl.gui.GuiListener;

public class Activator implements BundleActivator {
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext bc) throws Exception {
		
		// creating blacklist data dir
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
		 * Registering the bundle listeners
		 */
		bc.addBundleListener(new DesktopIntegrationListener(bc));
		bc.addBundleListener(new GuiListener(bc, blacklistFilter));
	}
	
	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		/* nothing todo here */
	}
}