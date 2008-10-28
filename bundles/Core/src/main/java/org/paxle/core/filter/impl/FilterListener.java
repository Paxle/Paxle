/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.core.filter.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
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
import org.osgi.util.tracker.ServiceTracker;
import org.paxle.core.filter.FilterQueuePosition;
import org.paxle.core.filter.FilterTarget;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.core.queue.ICommandProfileManager;

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
		final StringBuilder sb = new StringBuilder("(|");
		final Formatter formatter = new Formatter(sb);
		for (String intrface : INTERFACES)
			formatter.format("(%s=%s)", Constants.OBJECTCLASS, intrface);
		formatter.close();
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
	 * A component to normalize {@link URI URIs}. This component is accessible to all 
	 * from all {@link IFilter filters} via function {@link IFilterContext#getReferenceNormalizer()}.
	 * 
	 * @see IReferenceNormalizer#normalizeReference(String)
	 */
	private IReferenceNormalizer referenceNormalizer;
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());

	private final ServiceTracker cmdProfileManagerTracker;
	
	public FilterListener(
			FilterManager filterManager, 
			ITempFileManager tempFileManager,
			IReferenceNormalizer referenceNormalizer,
			BundleContext context
	) throws InvalidSyntaxException {
		if (filterManager == null) throw new NullPointerException("The filter-manager is null.");
		if (tempFileManager == null) throw new NullPointerException("The temp-file-manager is null.");
		if (referenceNormalizer == null) throw new NullPointerException("The reference-normalizer is null.");
		if (context == null) throw new NullPointerException("The bundle-context is null.");
		
		this.filterManager = filterManager;
		this.tempFileManager = tempFileManager;
		this.referenceNormalizer = referenceNormalizer;
		this.context = context;
		
		this.cmdProfileManagerTracker = new ServiceTracker(context, ICommandProfileManager.class.getName(),null);
		this.cmdProfileManagerTracker.open();

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
			// ignore unknown interfaces
			if (!INTERFACES.contains(interfaceName)) continue;

			if (interfaceName.equals(IFilter.class.getName())) 
				this.handleFilter(reference, eventType);
			else if (interfaceName.equals(IFilterQueue.class.getName())) 
				this.handleFilterQueue(reference, eventType);
		}
	}
	
	private void handleFilter(ServiceReference reference, int eventType) {
		// the service ID of the registered filter
		Long serviceID = (Long) reference.getProperty(Constants.SERVICE_ID);
		if (serviceID == null) {
			this.logger.error("Unable to (un)register filter. No OSGi service-id found!");
			return;
		}
		
		// the id's of the target queues
		String targetIDs[] = this.getTargetIDs(reference.getProperty(IFilter.PROP_FILTER_TARGET));
		if (targetIDs == null) {
			this.logger.error("Unable to (un)register filter. No target-id found!");
			return;
		}				
		
		if (eventType == ServiceEvent.REGISTERED) {
			// get a reference to the filter
			IFilter<?> filter = (IFilter<?>) this.context.getService(reference);	

			// getting the filter PID
			String filterPID = (String) reference.getProperty(Constants.SERVICE_PID);
			if (filterPID == null) {
				// generating a PID
				filterPID = reference.getBundle().getSymbolicName() + "#" + filter.getClass().getName();
			}
			
			final HashMap<String,FilterQueuePosition> annotProps = new HashMap<String,FilterQueuePosition>();
			final FilterTarget target = filter.getClass().getAnnotation(FilterTarget.class);
			if (target != null)
				for (final FilterQueuePosition pos : target.value())
					annotProps.put(pos.value(), pos);
			
			// adding the filter to multiple targets
			for (String targetParamString : targetIDs) {
				final String[] params = targetParamString.split(";");
				final FilterQueuePosition pos = annotProps.remove(params[0].trim());
				this.filterManager.addFilter(this.generateFilterMetadata(
						filterPID,
						serviceID,
						params,
						filter,
						pos
				));
			}
			
			// adding the filter to all targets defined only via annotation
			final String[] params = new String[1];
			for (final FilterQueuePosition pos : annotProps.values()) {
				params[0] = pos.value();
				this.filterManager.addFilter(this.generateFilterMetadata(
						filterPID,
						serviceID,
						params,
						filter,
						pos
				));
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
	
	/**
	 * @param filterPID persistent unique identifier for the filter 
	 * @param serviceID temp. unique servicer ID needed for unregistration of the filter
	 * @param target unique ID of the {@link IFilterQueue} the filter should be applied to
	 * @param filter
	 * @return
	 */
	private FilterContext generateFilterMetadata(
			String filterPID,
			Long serviceID, 
			String[] params, 
			IFilter<?> filter,
			final FilterQueuePosition annot
	) {
		Properties filterProps = new Properties();
		String targetID = params[0].trim();
		int filterPos = 0;
		boolean enabled = true;
		
		if (annot != null) {
			filterPos = annot.position();
			enabled = annot.enabled();
		}
		
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
				} else if (key.equals(IFilter.PROP_FILTER_TARGET_DISABLED)) {
					// defines if the filter is disabled per default
					enabled = !Boolean.valueOf(val).booleanValue();
				} else {
					/*
					 * Just take over the rest of the parameters
					 */
					filterProps.setProperty(key, val);
				}
			}
		}

		final FilterContext filterContext = new FilterContext(
				filterPID,
				serviceID,
				filter,
				targetID,
				filterPos,
				enabled,
				filterProps
		);
		
		filterContext.setTempFileManager(this.tempFileManager);
		filterContext.setReferenceNormalizer(this.referenceNormalizer);
		filterContext.setCommandProfileManagerTracker(this.cmdProfileManagerTracker);
		
		return filterContext;
	}
}
