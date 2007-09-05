package org.paxle.core.filter.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterQueue;

/**
 * A class to listen for registered and unregistered {@link IFilter filters}.
 */
public class FilterListener implements ServiceListener {	
	/**
	 * The interfaces to listen for
	 */
	private static final HashSet<String> INTERFACES = new HashSet<String>(Arrays.asList(new String[]{	
			IFilter.class.getName(),
			IFilterQueue.class.getName()
	}));	

	/**
	 * A LDAP styled expression used for the {@link IFilter filter}-listener
	 */
	public static final String FILTER;
	static {
		StringBuilder sb = new StringBuilder("(|");
		for (String intrface : INTERFACES) sb.append(String.format("(%s=%s)",Constants.OBJECTCLASS,intrface));
		FILTER = sb.append(')').toString();
	}

	/**
	 * A class to manage {@link IFilter filters} and
	 * {@link IFilterQueue filter-queues}.
	 */
	private FilterManager filterManager = null;

	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */
	private BundleContext context = null;

	public FilterListener(FilterManager filterManager, BundleContext context) throws InvalidSyntaxException {
		this.filterManager = filterManager;
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

		// get the names of the registered interfaces 
		String[] interfaceNames = ((String[])reference.getProperty(Constants.OBJECTCLASS));

		// loop through the interfaces
		for (String interfaceName : interfaceNames) {
			if (!INTERFACES.contains(interfaceName)) continue;

			if (interfaceName.equals(IFilter.class.getName())) 
				this.handleFilter(reference, eventType);
			else if (interfaceName.equals(IFilterQueue.class.getName())) 
				this.handleFilterQueue(reference, eventType);
		}
	}

	private void handleFilter(ServiceReference reference, int eventType) {		
		if (eventType == ServiceEvent.REGISTERED) {
			// get the filter
			IFilter filter = (IFilter) this.context.getService(reference);	
			Object targets = reference.getProperty(IFilter.PROP_FILTER_TARGET);

			if (targets instanceof String)  
				this.filterManager.addFilter(this.generateFilterMetadata((String) targets, filter));
			else if (targets instanceof String[]) {
				for (String target : (String[]) targets) {
					this.filterManager.addFilter(this.generateFilterMetadata(target, filter));
				}
			} else throw new IllegalArgumentException(IFilter.PROP_FILTER_TARGET + " has wrong type.");


			System.out.println("New filter '" + filter.getClass().getName() + "' registered.");			
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// TODO: filter was uninstalled
			System.out.println("Filter unregistered.");
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}			
	}

	private void handleFilterQueue(ServiceReference reference, int eventType) {
		if (eventType == ServiceEvent.REGISTERED) {
			// get the filter
			IFilterQueue queue = (IFilterQueue) this.context.getService(reference);	
			String queueID = (String) reference.getProperty(IFilterQueue.PROP_FILTER_QUEUE_ID);

			this.filterManager.addFilterQueue(queueID, queue);			
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// TODO: new filter was uninstalled
			System.out.println("Filter unregistered.");
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}		
	}

	private FilterContext generateFilterMetadata(String target, IFilter filter) {
		Properties filterProps = new Properties();
		String[] params = target.split(";");
		String targetID = params[0].trim();
		int filterPos = 0;

		if (params.length > 1) {
			for (int i=1; i < params.length; i++) {
				String param = params[i];
				String[] paramParts = param.split("=");
				String key = paramParts[0].trim();
				String val = paramParts[1].trim();
				
				if (key.equals("pos")) {
					try {
						filterPos = Integer.valueOf(val).intValue();
					} catch (NumberFormatException e) {/* ignore this */}
				} else {
					filterProps.setProperty(key, val);
				}
			}
		}

		return new FilterContext(
				filter,
				targetID,
				filterPos,
				filterProps
		);
	}
}
