package org.paxle.core.filter;

import java.util.List;


public interface IFilterQueue {
	public static final String PROP_FILTER_QUEUE_ID = IFilterQueue.class.getName() + ".id";
	
	public void setFilters(List<IFilterContext> filters);
}
