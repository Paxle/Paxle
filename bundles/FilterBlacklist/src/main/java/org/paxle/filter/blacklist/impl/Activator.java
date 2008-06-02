
package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.paxle.core.filter.IFilter;
import org.paxle.desktop.DIComponent;
import org.paxle.filter.blacklist.BlacklistServlet;

public class Activator implements BundleActivator, BundleListener {
	
	public static final String FILTER = String.format("(%s=org.paxle.desktop)", Constants.BUNDLE_SYMBOLICNAME);
	
	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	private static BundleContext bc;
	private ServiceRegistration dialogueReg = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception {
		bc = context;
		
//		File list = bc.getDataFile("../../../blacklist");
		File list = new File("blacklist");
		list.mkdirs();
		new File(list, "default.list");
		
		Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {"org.paxle.crawler.in", "org.paxle.parser.out"});
		BlacklistFilter blacklistFilter = new BlacklistFilter(list);
		bc.registerService(IFilter.class.getName(), blacklistFilter, filterProps);
		
//		ServiceReference sr = bc.getServiceReference(HttpService.class.getName());
//		http = (HttpService) bc.getService(sr);
//		if(http != null) {  
//			http.registerServlet("/blacklist", new BlacklistServlet(), null, null);
//		}
		
		for (final Bundle b : context.getBundles())
			if (b.getState() == Bundle.ACTIVE && b.getHeaders().get(Constants.BUNDLE_SYMBOLICNAME).equals("org.paxle.desktop")) {
				registerBlacklistDialogue();
				break;
			}
		context.addBundleListener(this);
		
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
		if (dialogueReg != null) {
			dialogueReg.unregister();
			dialogueReg = null;
		}
		bc = null;
	}
	
	private void registerBlacklistDialogue() {
		dialogueReg = bc.registerService(DIComponent.class.getName(), new BlacklistDialogue(), null);
	}
	
	public void bundleChanged(BundleEvent event) {
		if (event.getType() == BundleEvent.STARTED &&
				event.getBundle().getHeaders().get(Constants.BUNDLE_SYMBOLICNAME).equals("org.paxle.desktop"))
			registerBlacklistDialogue();
	}
}