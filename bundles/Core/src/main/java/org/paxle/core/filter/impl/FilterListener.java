package org.paxle.core.filter.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.io.temp.ITempFileManager;

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
	
	// generating filter expression
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
	
	/**
	 * Temp-File-Manager. This class is accessible from all {@link IFilter filters} via
	 * function {@link IFilterContext#getTempFileManager()}.
	 */
	private ITempFileManager tempFileManager = null;
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());

	public FilterListener(FilterManager filterManager, ITempFileManager tempFileManager, BundleContext context) throws InvalidSyntaxException {
		this.filterManager = filterManager;
		this.tempFileManager = tempFileManager;
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
		// the service ID of the registered filter
		Object serviceID = reference.getProperty(Constants.SERVICE_ID);
		if (serviceID == null) {
			this.logger.error("Unable to (un)register filter. No OSGi service-id found!");
			return;
		}
		
		// the id's of the target queues
		String targetIDs[] = this.getTargetIDs(reference.getProperty(IFilter.PROP_FILTER_TARGET));
		if (serviceID == null) {
			this.logger.error("Unable to (un)register filter. No target-id found!");
			return;
		}				
		
		if (eventType == ServiceEvent.REGISTERED) {
			// get a reference to the filter
			IFilter filter = (IFilter) this.context.getService(reference);	

			// adding the filter to multiple targets
			for (String targetID : targetIDs) {
				this.filterManager.addFilter(this.generateFilterMetadata(serviceID,targetID, filter));
			}

			this.logger.info(String.format("Filter '%s' with serviceID '%s' registered.",filter.getClass().getName(), serviceID.toString()));			
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			for (String targetID : targetIDs) {
				this.filterManager.removeFilter(serviceID, targetID);
			}
			this.logger.info(String.format("Filter with serviceID '%s' unregistered.",serviceID.toString()));	
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}			
	}

	private void handleFilterQueue(ServiceReference reference, int eventType) {
		String queueID = (String) reference.getProperty(IFilterQueue.PROP_FILTER_QUEUE_ID);
		if (queueID == null) {
			this.logger.error("Unable to (un)register filterqueue. No queue-id found!");
			return;
		}
		
		if (eventType == ServiceEvent.REGISTERED) {
			// get the filterqueue
			IFilterQueue queue = (IFilterQueue) this.context.getService(reference);				
			
			// register it
			this.filterManager.addFilterQueue(queueID, queue);			
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// removing the filter-queue
			this.filterManager.removeFilterQueue(queueID);
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}		
	}
	
	private String[] getTargetIDs(Object targetProperty) { 
		if (targetProperty == null) return null;
		
		ArrayList<String> targetIDs = new ArrayList<String>();
		
		if (targetProperty instanceof String) {
			targetIDs.add((String)targetProperty);
		} else if (targetProperty instanceof String[]) {
			for (String target : (String[]) targetProperty) {
				targetIDs.add(target);
			}
		} else {
			throw new IllegalArgumentException(String.format(
					"%s has wrong type '%s'.",
					IFilter.PROP_FILTER_TARGET,
					targetProperty.getClass().getName()
			));
		}
		
		return targetIDs.toArray(new String[targetIDs.size()]);
	}

	private FilterContext generateFilterMetadata(Object serviceID, String target, IFilter filter) {
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
				
				if (key.equals(IFilter.PROP_FILTER_TARGET_POSITION)) {
					/*
					 * determining the filter-position
					 */
					try {
						// parse the position
						filterPos = Integer.valueOf(val).intValue();
					} catch (NumberFormatException e) {/* ignore this */}
					
					/*
					 * add position to the filter-properties
					 */
					filterProps.setProperty(IFilter.PROP_FILTER_TARGET_POSITION, Integer.toString(filterPos));
				} else {
					/*
					 * Just take over the rest of the parameters
					 */
					filterProps.setProperty(key, val);
				}
			}
		}

		FilterContext filterContext = new FilterContext(
				serviceID,
				filter,
				targetID,
				filterPos,
				filterProps
		);
		filterContext.setTempFileManager(this.tempFileManager);
		return filterContext;
	}
}
