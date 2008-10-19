/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

package org.paxle.data.balancer.impl;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.data.balancer.IHostConfig;
import org.paxle.data.balancer.IHostConfigProvider;

public class ConfigListener implements ServiceListener {
	
	private static final class RobotsHostConfigProvider implements IHostConfigProvider {
		
		private final Object manager;
		private final Method getProps;
		
		public RobotsHostConfigProvider(final Object manager) throws SecurityException, NoSuchMethodException {
			this.manager = manager;
			getProps = manager.getClass().getMethod("getRobotsProperties", URI.class);
		}
		
		public IHostConfig getHostConfig(final URI uri) {
			try {
				final Map<?,?> map = (Map<?,?>)getProps.invoke(manager, uri);
				if (map == null)
					return null;
				final String delay = (String)map.get("Crawl-Delay");
				if (delay == null)
					return null;
				return new HostConfig(Integer.parseInt(delay), TimeUnit.SECONDS);
			} catch (Exception e) { e.printStackTrace(); }
			return null;
		}
	}
	
	private static final String IRobotsTxtManager = "org.paxle.filter.robots.IRobotsTxtManager";
	
	public static final String FILTER = String.format("(|(%s=%s)(%s=%s))",
			Constants.OBJECTCLASS, IRobotsTxtManager,
			Constants.OBJECTCLASS, IHostConfigProvider.class.getName());
	
	private final BundleContext context;
	private final HostManager manager;
	
	public ConfigListener(final BundleContext context, final HostManager manager) {
		this.context = context;
		this.manager = manager;
		
		try {
			for (final ServiceReference ref : context.getAllServiceReferences(null, FILTER))
				serviceChanged(ref, ServiceEvent.REGISTERED);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
	}
	
	private void serviceChanged(final ServiceReference ref, final int type) {
		final Long id = (Long)ref.getProperty(Constants.SERVICE_ID);
		switch (type) {
			case ServiceEvent.REGISTERED:
				final Object service = context.getService(ref);
				for (final String oc : (String[])ref.getProperty(Constants.OBJECTCLASS)) {
					if (IRobotsTxtManager.equals(oc)) try {
						manager.addProvider(id, new RobotsHostConfigProvider(service));
						return;
					} catch (Exception e) { e.printStackTrace(); }
				}
				manager.addProvider(id, (IHostConfigProvider)service);
				break;
			case ServiceEvent.UNREGISTERING:
				manager.removeProvider(id);
				break;
			case ServiceEvent.MODIFIED:
				// ignore
		}
	}
	
	public void serviceChanged(ServiceEvent event) {
		serviceChanged(event.getServiceReference(), event.getType());
	}
}
