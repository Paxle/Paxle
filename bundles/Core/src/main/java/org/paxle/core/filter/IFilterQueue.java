/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

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
	 * @return the system-width unique ID of this queue
	 */
	public String getFilterQueueID();
	
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
