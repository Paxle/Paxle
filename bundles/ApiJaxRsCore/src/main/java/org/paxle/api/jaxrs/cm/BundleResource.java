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

package org.paxle.api.jaxrs.cm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.metatype.MetaTypeProvider;

public class BundleResource {
	/**
	 * The OSGI {@link ConfigurationAdmin} service 
	 */
	private final ConfigurationAdmin configAdmin;	
	
	/**
	 * The OSGi {@link Bundle} wrapped by this resource
	 */
	private final Bundle bundle;
	
	/**
	 * A list of acceptable languages by the client
	 */
	private List<String> preferedLocales;
	
	/**
	 * All {@link MetaTypeProvider}s found for this bundle.
	 * The key of this map is the {@link Constants#SERVICE_PID} of a {@link ManagedService} for which
	 * the {@link MetaTypeProvider} provides metaType-data.
	 */
	private Map<String, MetaTypeProvider> metaTypeProviders; 
	
	public BundleResource(ConfigurationAdmin configAdmin, Bundle bundle, Map<String, MetaTypeProvider> metaTypeData, List<String> locale) {
		this.configAdmin = configAdmin;
		this.bundle = bundle;
		this.metaTypeProviders = metaTypeData;
		this.preferedLocales = locale;
	}

	/**
	 * This method is called when using the URL
	 * <pre>http://localhost:8282/configurations/[bundleID]</pre>
	 * 
	 * @return this {@link BundleResource} itself
	 */
	@GET
	public BundleResource returnThis() {
		return this;
	}
	
	/**
	 * @return the ID of the {@link Bundle} wrapped by this resource
	 * @see Bundle#getBundleId()
	 */
	public Long getBundleId() {
		return Long.valueOf(this.bundle.getBundleId());
	}
	
	/**
	 * @return the Name of the {@link Bundle} wrapped by this resource
	 */
	public String getBundleName() {
		return (String) this.bundle.getHeaders().get(Constants.BUNDLE_NAME);
	}
	
	/**
	 * This method is called when using the URL
	 * <pre>http://localhost:8282/configurations/[bundleID]/configs</pre>
	 * 
	 * @return a list of {@link ConfigurationResource} objects that belong to this {@link BundleResource} or <code>null</code>
	 * @throws IOException
	 */
	@GET
	@Path("configs")
	public List<ConfigurationResource> getConfigs() throws IOException {
		List<ConfigurationResource> configList = new ArrayList<ConfigurationResource>();

		for (Entry<String, MetaTypeProvider> metaTypeProviderEntry : this.metaTypeProviders.entrySet()) {
			// the service.pid of the managed-service the configuration belongs to
			String managedServicePID = metaTypeProviderEntry.getKey();
			
			// metadata about the configuration options
			MetaTypeProvider metaTypeProvider = metaTypeProviderEntry.getValue();
			
			// the configuration itself
			Configuration config = this.configAdmin.getConfiguration(managedServicePID, this.bundle.getLocation());
			
			// creating a config-resource
			configList.add(new ConfigurationResource(this.configAdmin, config, metaTypeProvider, this.preferedLocales));
		}
		
		return configList;
	}
	
	/**
	 * A function to query the {@link ConfigurationResource configuration} for a give OSGi-service. 
	 * 
	 * This method is called when using the URL
	 * <pre>http://localhost:8282/configurations/[bundleID]/[servicePID]</pre>
	 * 
	 * @param servicePID the {@link Constants#SERVICE_PID} of the OSGi service whose {@link ConfigurationResource configuration} should be returned
	 * @return the configuration of the given OSGi-service
	 * 
	 * @throws IOException
	 * @throws InvalidSyntaxException
	 */
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
