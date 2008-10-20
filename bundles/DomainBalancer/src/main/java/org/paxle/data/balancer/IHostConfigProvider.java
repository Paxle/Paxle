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

package org.paxle.data.balancer;

import java.net.URI;

/**
 * An {@link IHostConfigProvider} is a service that is able to provide {@link IHostConfig}s
 * for specific {@link URI}s. The {@link IHostConfig} once provided must remain the same
 * object for the same {@link URI}, however the {@link IHostConfig} itself may provide
 * different information each time it is queried. It may provide the same object for
 * different {@link URI}s though.
 * @see IHostConfig
 */
public interface IHostConfigProvider {
	
	/**
	 * @param uri the {@link URI} for which an {@link IHostConfig} is requested
	 * @return the {@link IHostConfig} which is permanently attached to the URI or
	 *         <code>null</code> if either this provider does not know the specified
	 *         URI or the resource at the URI does not provide the required information.
	 */
	public IHostConfig getHostConfig(final URI uri);
}
