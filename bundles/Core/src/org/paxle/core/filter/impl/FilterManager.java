package org.paxle.core.filter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterManager;
import org.paxle.core.filter.IFilterQueue;

public class FilterManager implements IFilterManager {
	private HashMap<String, SortedSet<FilterMetaData>> filters = new HashMap<String, SortedSet<FilterMetaData>>();
	private HashMap<String, IFilterQueue> queues = new HashMap<String, IFilterQueue>();

	public void addFilter(FilterMetaData[] filters) {
		for (FilterMetaData filter : filters) {
			this.addFilter(filter);
		}
	}
	
	public void addFilter(FilterMetaData filter) {
		String targetID = filter.getTargetID();
		
		SortedSet<FilterMetaData> filterList = null;
		if (this.filters.containsKey(targetID)) {
			filterList = this.filters.get(targetID);
		} else {
			filterList = new TreeSet<FilterMetaData>();
		}

		filterList.add(filter);		
		this.filters.put(targetID, filterList);
		
		if (this.queues.containsKey(targetID)) {
			IFilterQueue queue = this.queues.get(targetID);
			this.setFilters(queue, filterList);

		}		
	}
	
	public void addFilterQueue(String queueID, IFilterQueue queue) {
		this.queues.put(queueID, queue);
		if (this.filters.containsKey(queueID)) {
			SortedSet<FilterMetaData> filters = this.filters.get(queueID);
			this.setFilters(queue, filters);
		}
	}
	
	private void setFilters(IFilterQueue queue, SortedSet<FilterMetaData> filterList) {
		// sort filters
		ArrayList<IFilter> temp = new ArrayList<IFilter>();
		for (FilterMetaData filter : filterList) temp.add(filter.getFilterImpl()); 
		
		// pass it to the queue
		queue.setFilters(temp);
	}
}
