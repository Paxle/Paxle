
package org.paxle.crawler.http.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class RobotsFilterListener implements ServiceListener {
	
	static final String FILTER = String.format("(%s=org.paxle.filter.robots.IRobotsTxtManager)", Constants.OBJECTCLASS);
	static final String ROBOTS_TXT_FILTER = new String();
	
	private final BundleContext context;
	private final HttpCrawler crawler;
	
	public RobotsFilterListener(final BundleContext context, final HttpCrawler crawler) {
		this.context = context;
		this.crawler = crawler;
	}
	
	public void serviceChanged(ServiceEvent event) {
		final ServiceReference ref = event.getServiceReference();
		switch (event.getType()) {
			case ServiceEvent.REGISTERED:
				final Object service = context.getService(ref);
				crawler.setRobotsTxtFilter(service);
				break;
				
			case ServiceEvent.UNREGISTERING:
				crawler.setRobotsTxtFilter(null);
				break;
				
			case ServiceEvent.MODIFIED:
				break;
		}
	}
}
