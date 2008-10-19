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

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.paxle.data.balancer.IHostConfig;
import org.paxle.data.balancer.IHostConfigProvider;

public class HostManager {
	
	private final Map<String,IHostConfig> configs = new ConcurrentHashMap<String,IHostConfig>();
	private final HostConfig defConfig = new HostConfig(5, TimeUnit.SECONDS);
	private final Map<Long,IHostConfigProvider> providers = new ConcurrentHashMap<Long,IHostConfigProvider>();
	
	public HostManager() {
	}
	
	public IHostConfig getHostConfig(final URI uri) {
		IHostConfig conf = configs.get(uri.getAuthority());
		if (conf != null)
			return conf;
		for (final IHostConfigProvider provider : providers.values())
			if (provider != null && (conf = provider.getHostConfig(uri)) != null) {
				configs.put(uri.getAuthority(), conf);
				return conf;
			}
		return defConfig;
	}
	
	public void addProvider(final Long id, final IHostConfigProvider provider) {
		providers.put(id, provider);
	}
	
	public void removeProvider(final Long id) {
		providers.remove(id);
	}
}
