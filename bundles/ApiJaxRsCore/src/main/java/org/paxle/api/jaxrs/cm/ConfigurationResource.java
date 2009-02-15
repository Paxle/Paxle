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
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class ConfigurationResource {
	/**
	 * The OSGI {@link ConfigurationAdmin} service
	 */
	private final ConfigurationAdmin configAdmin;
	
	/**
	 * The CM {@link Configuration}
	 */
	private final Configuration config;
	
	/**
	 * TODO: does not work yet
	 */
	@HeaderParam("Accept-Language")
	String language;
	
	public ConfigurationResource(ConfigurationAdmin configAdmin, Configuration config) throws IOException {
		this.configAdmin = configAdmin;
		this.config = config;
	}
	
	@GET
	public ConfigurationResource returnThis() {
		return this;
	}
	
	@DELETE
	public void delete() throws IOException {
		this.config.delete();
	}
	
	public String getPid() {
		return this.config.getPid();
	}
		
	@Path("{propertyID}")
	public PropertyResource getProperty(@PathParam("propertyID") String propertyID) {
		@SuppressWarnings("unchecked")
		Dictionary props = this.config.getProperties();
		if (props == null) return null;
		
		Object value = props.get(propertyID);
		if (value == null) throw new WebApplicationException(Status.NOT_FOUND);
		
		return new PropertyResource(propertyID,value);
	}
	
	@GET
	@Path("properties")
	public List<PropertyResource> getProperties() {
		List<PropertyResource> configProps = new ArrayList<PropertyResource>();
		
		@SuppressWarnings("unchecked")
		Dictionary props = this.config.getProperties();
		if (props != null) {
			@SuppressWarnings("unchecked")
			Enumeration<String> keys = props.keys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				if (key.equalsIgnoreCase(Constants.SERVICE_PID)) continue;
				
				Object value = props.get(key);
				// XXX: should we skip null values? 
				// if (value == null) continue;
								
				configProps.add(new PropertyResource(key,value));
			}
		}
		return configProps; 
	}
		
	@POST
	@Path("properties")
	public void setProperties(List<PropertyResource> props) throws IOException {
		if (props == null || props.size() == 0) return;
		
		// getting the currently active properties
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> currentProps = this.config.getProperties();
		
		// processing the received properties
		for (PropertyResource prop : props) {
			String key = prop.getKey();
			Object value = prop.getValue();
			
			String type = prop.getType();
			if (type == null) {
				// TODO: we need to do a type conversion here ...
			}
			
			currentProps.put(key, value);
		}
		
		// updating CM properties
		this.config.update(currentProps);
	}
}
