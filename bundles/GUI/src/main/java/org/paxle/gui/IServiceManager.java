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
package org.paxle.gui;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public interface IServiceManager {

	/**
	 * A constant to fetch the service-manager from the velocity context.
	 */
	public static final String SERVICE_MANAGER = "manager";

	public String getProperty(final String name);

	public void shutdownFramework() throws BundleException;
	
	/** Shuts down the OSGi framework after the given time (in seconds), but returns immediately */
	public void shutdownFrameworkDelayed(int delay) throws BundleException;

	public void restartFramework() throws BundleException;
	
	/** Shuts down and restarts the OSGi framework after the given time (in seconds), but returns immediately */
	public void restartFrameworkDelayed(int delay) throws BundleException;

	public Object getService(String serviceName);

	public ServiceReference[] getServiceReferences(String serviceName)
			throws InvalidSyntaxException;

	public Object getServiceProperty(String serviceName, String propertyname);

	public boolean hasService(String serviceName);

	public Object[] getServices(String serviceName, String query)
			throws InvalidSyntaxException;

	public boolean hasService(String serviceName, String query)
			throws InvalidSyntaxException;

	public Bundle[] getBundles();

	public Bundle getBundle(long bundleID);
	public Bundle getBundle(String bundleSymbolicName);
	public Bundle[] getBundles(String filterString) throws InvalidSyntaxException;
	
	public Bundle installBundle(String arg0) throws BundleException;

}