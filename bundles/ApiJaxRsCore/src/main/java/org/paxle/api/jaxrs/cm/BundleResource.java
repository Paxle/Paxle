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
package org.paxle.api.jaxrs.cm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class BundleResource {
	/**
	 * The OSGI {@link ConfigurationAdmin} service
	 */
	private final ConfigurationAdmin configAdmin;	
	
	private final Bundle bundle;
	
	public BundleResource(ConfigurationAdmin configAdmin, Bundle bundle) {
		this.configAdmin = configAdmin;
		this.bundle = bundle;
	}

	@GET
	public BundleResource returnThis() {
		return this;
	}
	
	private Configuration[] getConfigArray() throws IOException {
		try {
			return this.configAdmin.listConfigurations(String.format(
					"(service.bundleLocation=%s)",
					this.bundle.getLocation()
			));
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public Long getBundleId() {
		return new Long(this.bundle.getBundleId());
	}
	
	public boolean hasConfigs() throws IOException {
		Configuration[] configs = this.getConfigArray();
		return configs != null && configs.length > 0;
	}
	
	@GET
	@Path("configs")
	public List<ConfigurationResource> getConfigs() throws IOException {
		Configuration[] configs = this.getConfigArray();

		List<ConfigurationResource> configList = new ArrayList<ConfigurationResource>();
		if (configs != null) {
			for (Configuration config : configs) {
				configList.add(new ConfigurationResource(this.configAdmin, config));
			}
		}
		
		return configList;
	}
	
	@Path("{servicePID}")
	public ConfigurationResource getConfig(@PathParam("servicePID") String servicePID) throws IOException, InvalidSyntaxException {
		List<ConfigurationResource> configs = this.getConfigs();
		
		for (ConfigurationResource config : configs) {
			if (config.getPid().equals(servicePID)) {
				return config;
			}
		}
		
		throw new WebApplicationException(Status.NOT_FOUND);
	}
}
