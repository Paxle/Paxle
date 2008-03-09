package org.paxle.core.filter;

import java.util.List;

import org.paxle.core.filter.impl.FilterManager;
import org.paxle.core.impl.MWComponentFactory;


public interface IFilterQueue {
	/**
	 * A system-width unique ID that is used to register the {@link IFilterQueue} as
	 * service to the OSGi framework. 
	 * 
	 *  @see MWComponentFactory#registerComponentServices(org.paxle.core.IMWComponent, org.osgi.framework.BundleContext)
	 */
	public static final String PROP_FILTER_QUEUE_ID = IFilterQueue.class.getName() + ".id";
	
	/**
	 * This function is called by {@link MWComponentFactory#registerComponentServices(String, org.paxle.core.IMWComponent, org.osgi.framework.BundleContext)}
	 * to set the filter-queue-ID that was used by the {@link MWComponentFactory} to register 
	 * this {@link IFilterQueue} to the OSGi framework. 
	 * @param filterQueueID
	 */
	public void setFilterQueueID(String filterQueueID);	
	
	/**
	 * This function is used by the {@link FilterManager} to
	 * update the list of registered {@link IFilter filters} if a
	 * new filter was removed or added.
	 * 
	 * @param filters
	 */
	public void setFilters(List<IFilterContext> filters);
	
	/**
	 * @return the list of {@link IFilter filters} that are
	 * 		   currently applied to this queue.
	 */
	public List<IFilterContext> getFilters();
}
