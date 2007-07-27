package org.paxle.core.filter.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.core.filter.IFilter;

/**
 * A class to listen for registered and unregistered {@link IFilter filters}.
 */
public class FilterListener implements ServiceListener {

	/**
	 * A LDAP styled expression used for the {@link IFilter filter}-listener
	 */
	public static String FILTER = "(" + Constants.OBJECTCLASS + "=" + IFilter.class.getName () +")";	
	
	/**
	 * A class to manage {@link IFilter filters}
	 */
	private FilterManager filterManager = null;
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */
	private BundleContext context = null;
	
	public FilterListener(FilterManager filterManager, BundleContext context) {
		this.filterManager = filterManager;
		this.context = context;
		try {
			ServiceReference[] refs = context.getServiceReferences(IFilter.class.getName(),"()");
			if (refs == null) return;
			for (ServiceReference ref : refs) {
				
				// get the filter
				IFilter filter = (IFilter) this.context.getService(ref);	
				System.out.println("New filter '" + filter.getClass().getName() + "' registered.");
				
				// TODO: what to do with this filters?
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
		
		int eventType = event.getType();
		if (eventType == ServiceEvent.REGISTERED) {
			// get the filter
			IFilter filter = (IFilter) this.context.getService(reference);		
			
			System.out.println("New filter '" + filter.getClass().getName() + "' registered.");			
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// TODO: new filter was uninstalled
			System.out.println("Filter unregistered.");
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}	
	}

}
