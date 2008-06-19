package org.paxle.core.filter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterManager;
import org.paxle.core.filter.IFilterQueue;

public class FilterManager implements IFilterManager {
	/**
	 * A map containing a sorted list of {@link FilterContext filters} for each {@link IFilterQueue targer}-id 
	 */
	private HashMap<String, SortedSet<FilterContext>> filters = new HashMap<String, SortedSet<FilterContext>>();
	
	/**
	 * A datastructure to map a target-id to a given {@link IFilterQueue}
	 */
	private HashMap<String, IFilterQueue> queues = new HashMap<String, IFilterQueue>();

	/**
	 * for logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
		
	/**
	 * registering a new {@link IFilter}
	 * @param filterContext the context-object for the {@link IFilter}
	 */
	public void addFilter(FilterContext filterContext) {
		String targetID = filterContext.getTargetID();
		
		// getting the filter-list of the target
		SortedSet<FilterContext> filterList = null;
		if (this.filters.containsKey(targetID)) {
			filterList = this.filters.get(targetID);
		} else {
			filterList = new TreeSet<FilterContext>();
		}

		// appending the filter to the filter-list
		filterList.add(filterContext);		
		this.filters.put(targetID, filterList);
		
		if (this.queues.containsKey(targetID)) {
			/*
			 * passing the changed filter-list to the filter-queue
			 */
			IFilterQueue queue = this.queues.get(targetID);
			this.setFilters(queue, filterList);
		}		
	}
	
	/**
	 * Unregistering a {@link IFilter}
	 * @param serviceID the {@link Constants#SERVICE_ID} of the {@link IFilter}
	 * @param targetID the property {@link IFilter#PROP_FILTER_TARGET} wich was used by the
	 * {@link IFilter} during service-registration.
	 */
	public void removeFilter(Long serviceID, String targetID) {
		if (serviceID == null) throw new NullPointerException("The serviceID must not be null.");
		if (targetID == null) throw new NullPointerException("The targetID must not be null.");
		
		int idx = targetID.indexOf(";");
		if (idx != -1) {
			targetID = targetID.substring(0,idx).trim();			
		}
		
		if (!this.filters.containsKey(targetID)) {
			this.logger.warn(String.format(
					"Unable to find a filter-queue with targetID '%s'. Unable to remove filter with service-id '%s'.",
					targetID,
					serviceID.toString()
			));
		}
		
		// get the queue from which the filter should be removed 
		SortedSet<FilterContext> filterList = this.filters.get(targetID);
		
		// find the desired filter
		Iterator<FilterContext> filterIter = filterList.iterator();
		while (filterIter.hasNext()) {
			FilterContext next = filterIter.next();
			if (next.getServiceID().equals(serviceID) /* TODO: && next.getFilterPosition() == position */) {				
				// remove it from the list
				filterIter.remove();
				
				// free resources
				next.close();
				
				this.logger.info(String.format(
						"Filter '%s' with service-id '%s' successfully removed from target '%s'.",
						next.getFilter().getClass().getName(),
						serviceID.toString(),
						targetID
				));
				break;
			}
		}
		
		// re-set the modified filter-list to the queue
		if (this.queues.containsKey(targetID)) {
			IFilterQueue queue = this.queues.get(targetID);
			this.setFilters(queue, filterList);
		}	
	}
	
	public void addFilterQueue(String queueID, IFilterQueue queue) {
		this.queues.put(queueID, queue);
		if (this.filters.containsKey(queueID)) {
			SortedSet<FilterContext> filters = this.filters.get(queueID);
			this.setFilters(queue, filters);
		}
	}
	
	/**
	 * Function to unregister a {@link IFilterQueue}
	 * @param queueID the ID of the {@link IFilterQueue}, which should be unregistered
	 */
	public void removeFilterQueue(String queueID) {
		if (this.queues.containsKey(queueID)) {
			this.queues.remove(queueID);
			this.logger.info(String.format("FilterQueue with id '%s' successfully removed.",queueID));			
		}
	}
	
	private void setFilters(IFilterQueue queue, SortedSet<FilterContext> filterList) {
		// sort filters
		ArrayList<IFilterContext> temp = new ArrayList<IFilterContext>();
		for (FilterContext filter : filterList) temp.add(filter); 
		
		// pass it to the queue
		queue.setFilters(temp);
	}
	
	public SortedSet<FilterContext> getFilterContextSet(String queueID) {
		return this.filters.get(queueID);
	}
}
