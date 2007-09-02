package org.paxle.core.filter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterManager;
import org.paxle.core.filter.IFilterQueue;

public class FilterManager implements IFilterManager {
	private HashMap<String, List<IFilter>> filters = new HashMap<String, List<IFilter>>();
	private HashMap<String, IFilterQueue> queues = new HashMap<String, IFilterQueue>();

	public void addFilter(String[] targetIDs, IFilter<?> filter) {
		for (String targetID : targetIDs) {
			this.addFilter(targetID, filter);
		}
	}
	
	public void addFilter(String targetID, IFilter<?> filter) {
		List<IFilter> filterList = null;
		if (this.filters.containsKey(targetID)) {
			filterList = this.filters.get(targetID);
		} else {
			filterList = new ArrayList<IFilter>();
		}

		filterList.add(filter);		
		this.filters.put(targetID, filterList);
		
		if (this.queues.containsKey(targetID)) {
			IFilterQueue queue = this.queues.get(targetID);
			queue.setFilters(filterList);
		}		
	}
	
	public void addFilterQueue(String queueID, IFilterQueue queue) {
		this.queues.put(queueID, queue);
		if (this.filters.containsKey(queueID)) {
			List<IFilter> filters = this.filters.get(queueID);
			queue.setFilters(filters);
		}
	}
}
