package org.paxle.crawler.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.crawler.ISubCrawler;


public class SubCrawlerListener implements ServiceListener {

	/**
	 * A LDAP styled expression used for the service-listener
	 */
	public static String FILTER = String.format("(&(%s=%s)(%s=*))",
			Constants.OBJECTCLASS, ISubCrawler.class.getName(), ISubCrawler.PROP_PROTOCOL);	
	
	/**
	 * A class to manage {@link ISubCrawler sub-crawlers}
	 */
	private SubCrawlerManager manager = null;
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */	
	private BundleContext context = null;
	
	public SubCrawlerListener(SubCrawlerManager manager, BundleContext context) throws InvalidSyntaxException {
		this.manager = manager;
		this.context = context;
		
		ServiceReference[] services = context.getServiceReferences(null,FILTER);
		if (services != null) for (ServiceReference service : services) serviceChanged(service, ServiceEvent.REGISTERED);	
	}
	
	/**
	 * @see ServiceListener#serviceChanged(ServiceEvent)
	 */
	public void serviceChanged(ServiceEvent event) {
		// getting the service reference
		ServiceReference reference = event.getServiceReference();
		this.serviceChanged(reference, event.getType());
	}		
	
	private void serviceChanged(ServiceReference reference, int eventType) {
		if (reference == null) return;		
		
		// the protocol supported by the detected sub-crawler
		String[] protocols = null;
		Object tmp = reference.getProperty(ISubCrawler.PROP_PROTOCOL);
		if (tmp instanceof String) protocols = new String[]{(String)tmp};
		else if (tmp instanceof String[]) protocols = (String[])tmp;
		
		if (eventType == ServiceEvent.REGISTERED) {			
			// a reference to the service
			ISubCrawler subCrawler = (ISubCrawler) this.context.getService(reference);			
			
			// new service was installed
			manager.addSubCrawler(protocols, subCrawler);
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// service was uninstalled
			manager.removeSubCrawler(protocols);
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}		
	}
}
