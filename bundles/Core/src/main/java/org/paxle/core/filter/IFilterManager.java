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

package org.paxle.core.filter;

import java.util.Set;

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
	public Set<IFilterContext> getFilters(String queueID);
}
