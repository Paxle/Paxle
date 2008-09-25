package org.paxle.core.filter;

import java.util.Set;

import org.paxle.core.filter.impl.FilterContext;

public interface IFilterManager {
	/**
	 * @param queueID the unique ID of a {@link IFilterQueue filter-queue}
	 * @return <code>true</code> if there are anly {@link IFilter filters} applied to the given {@link IFilterQueue filter-queue}
	 */
	public boolean hasFilters(String queueID);

	/**
	 * @param queueID the unique ID of a {@link IFilterQueue filter-queue}
	 * @return return a set of filters available for the given {@link IFilterQueue filter-queue}
	 */
	public Set<FilterContext> getFilters(String queueID);
}
