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
	
	public SubCrawlerListener(SubCrawlerManager manager, BundleContext context) throws InvalidSyntaxException {
		this.manager = manager;
		
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
		
		if (eventType == ServiceEvent.REGISTERED) {
			// new service was installed
			manager.addSubCrawler(reference);
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// service was uninstalled
			manager.removeSubCrawler(reference);
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}		
	}
}
