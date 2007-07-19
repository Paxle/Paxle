package org.paxle.crawler.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.crawler.ISubCrawler;


public class SubCrawlerListener implements ServiceListener {

	/**
	 * A LDAP styled expression used for the service-listener
	 */
	public static String FILTER = "(& (objectClass=" + ISubCrawler.class.getName () +") "+
	 							     "(" + ISubCrawler.PROP_PROTOCOL + "=*))";	
	
	/**
	 * A class to manage {@link ISubCrawler sub-crawlers}
	 */
	private SubCrawlerManager manager = null;
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */	
	private BundleContext context = null;
	
	public SubCrawlerListener(SubCrawlerManager manager, BundleContext context) {
		this.manager = manager;
		this.context = context;
		try {
			ServiceReference[] refs = context.getServiceReferences(ISubCrawler.class.getName(),"(" + ISubCrawler.PROP_PROTOCOL + "=*)");
			if (refs == null) return;
			for (ServiceReference ref : refs) {
				// the protocol supported by the detected sub-crawler
				String protocol = (String) ref.getProperty(ISubCrawler.PROP_PROTOCOL);				
				
				// get the subcrawler
				ISubCrawler subCrawler = (ISubCrawler) this.context.getService(ref);
				
				// pass it to the subCrawlerManager
				manager.addSubCrawler(protocol, subCrawler);
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @see ServiceListener#serviceChanged(ServiceEvent)
	 */	
	public void serviceChanged(ServiceEvent event) {
		ServiceReference reference = event.getServiceReference();
		
		// the protocol supported by the detected sub-crawler
		String protocol = (String) reference.getProperty(ISubCrawler.PROP_PROTOCOL);
		
		int eventType = event.getType();
		if (eventType == ServiceEvent.REGISTERED) {			
			// a reference to the service
			ISubCrawler subCrawler = (ISubCrawler) this.context.getService(reference);			
			
			// new service was installed
			manager.addSubCrawler(protocol, subCrawler);
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// service was uninstalled
			manager.removeSubCrawler(protocol);
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}		
	}
}
