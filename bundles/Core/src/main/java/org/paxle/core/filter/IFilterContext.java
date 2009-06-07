/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
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

import java.net.URI;
import java.util.Properties;

import org.paxle.core.doc.ICommandProfileManager;
import org.paxle.core.filter.impl.FilterContext;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.norm.IReferenceNormalizer;

/**
 * A {@link IFilterContext} is a {@link IFilter} applied to a specific {@link IFilterQueue} at a specific {@link #getFilterPosition() position}.
 */
public interface IFilterContext {	
	/**
	 * @return the position of the {@link #getFilter() filter} within the
	 *         {@link org.paxle.core.filter.IFilterQueue filter-queue}. 
	 *         This value can set be set between {@link Integer#MIN_VALUE} and
	 *         {@link Integer#MAX_VALUE}. If not set the default value 
	 *         <code>0</code> is used.
	 */
	public int getFilterPosition();
	
	/**
	 * @return a systemwidth unique identifier specifying the 
	 * 		   {@link org.paxle.core.filter.IFilterQueue filter-queue}
	 * 		   where the filter should be applied to.
	 * @see org.paxle.core.filter.IFilter#PROP_FILTER_TARGET
	 */
	public String getTargetID();
	
	/**
	 * @return the {@link org.paxle.core.filter.IFilter} this context
	 * belongs to.
	 */
	public IFilter<?> getFilter();
	
	/**
	 * Specifies if this {@link FilterContext} was disabled by the user via configuration.
	 * @return <code>true<code> if this {@link FilterContext} is active
	 */
	public boolean isEnabled();
	
	/**
	 * @return some {@link #getTargetID() target-} and {@link #getFilterPosition() position-}
	 * specific properties.
	 */
	public Properties getFilterProperties();
	
	/**
	 * @return the {@link ITempFileManager temp-file-manager} that should be used by the filter
	 * to handle temp-files.
	 */
	public ITempFileManager getTempFileManager();
	
	/**
	 * @return a component to normalize {@link URI URIs}.
	 * @see IReferenceNormalizer#normalizeReference(String)
	 */
	public IReferenceNormalizer getReferenceNormalizer();
	
	/**
	 * @return a reference to the {@link ICommandProfileManager}
	 */
	public ICommandProfileManager getCommandProfileManager();
}
