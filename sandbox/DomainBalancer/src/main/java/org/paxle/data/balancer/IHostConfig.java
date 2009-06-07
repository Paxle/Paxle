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

/**
 * An {@link IHostConfig}-object provides information about the specific URI it was
 * generated for. Initially, the information must have been retrieved from the resource
 * the URI (or in the case of <code>robots.txt</code> the host of the URI) points to.
 * It must not be generated using default values for URIs which do not provide the
 * required information.
 * <p>
 * The {@link IHostConfig}-object for one URI should remain the same, once it has been
 * retrieved. However, the information it provides may change. Since the
 * {@link IHostConfig}s are polled during the run of the balancer, a change of this
 * information will not be applied directly. The frequency of the polling is
 * indeterminate as a direct consequence of the balancer's purpose.
 * <p>
 * Though it is recommended to cache the information internally instead of retrieving
 * it every time a method of this interface is being invoked, this is not a necessity.
 * {@link IHostConfigProvider}s may choose the appropriate behaviour upon the standards
 * they obey and/or the capabilities of the methods and internal structure used.
 * <p>
 * <i>Implementation note on the DomainBalancer</i>: Currently, delays in returning the
 * requested information have direct impact on the speed of the balancer. This may change
 * in the future.
 */
public interface IHostConfig {
	
	/**
	 * @return the amount of time between successive requests to the host, this {@link IHostConfig}
	 *         provides information for.
	 */
	public long getDelayMs();
}
