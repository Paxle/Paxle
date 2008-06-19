package org.paxle.core.filter;

import java.util.SortedSet;

import org.paxle.core.filter.impl.FilterContext;

public interface IFilterManager {
	public SortedSet<FilterContext> getFilterContextSet(String queueID);
}
