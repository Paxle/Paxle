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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.ObjectClassDefinition;

public class ConfigurationResource {
	@DefaultValue("-1") @QueryParam("filter") 
	private int filter = -1;
	
	/**
	 * The OSGI {@link ConfigurationAdmin} service
	 */
	private final ConfigurationAdmin configAdmin;
	
	/**
	 * A {@link MetaTypeProvider} providing metatype-data for this configuration
	 */
	private final MetaTypeProvider metaTypeProvider;		
	
	/**
	 * The CM {@link Configuration}
	 */
	private final Configuration config;
		
	/**
	 * A list of acceptable languages by the client
	 */
	private List<String> preferedLocales;
	
	public ConfigurationResource(ConfigurationAdmin configAdmin, Configuration config, MetaTypeProvider metaTypeProvider, List<String> locale) throws IOException {
		this.configAdmin = configAdmin;
		this.config = config;
		this.metaTypeProvider = metaTypeProvider;
		this.preferedLocales = locale;
	}

	/**
	 * This method is called when using the URL
	 * <pre>http://localhost:8282/configurations/[bundleID]/[servicePID]/</pre>
	 * 
	 * @return this {@link ConfigurationResource} itself
	 */
	@GET
	public ConfigurationResource returnThis() {
		return this;
	}
	
	@DELETE
	public void delete() throws IOException {
		this.config.delete();
	}
	
	/**
	 * @return the {@link Constants#SERVICE_PID} to which this {@link ConfigurationResource} belongs to
	 * @see ObjectClassDefinition#getID()
	 */
	public String getPid() {
		return this.config.getPid();
	}
	
	/**
	 * @return the name of this managed-service
	 * @see ObjectClassDefinition#getName()
	 */
	public String getName() {
		ObjectClassDefinition ocd = this.getObjectClassDefinition();
		return (ocd == null) ? null : ocd.getName();
	}
	
	/**
	 * @return the description of this managed-service
	 * @see ObjectClassDefinition#getDescription()
	 */
	public String getDescription() {
		ObjectClassDefinition ocd = this.getObjectClassDefinition();
		return (ocd == null) ? null : ocd.getDescription();
	}
		
	@GET
	@Path("icon")
	@Produces("image/png")
	public byte[] getIcon(@DefaultValue("16") @QueryParam("size") int size) throws IOException {
		ObjectClassDefinition ocd = this.getObjectClassDefinition();
		if (ocd == null) return null;
		
		InputStream iconStream = null;
		try {
			iconStream = ocd.getIcon(16);
			if (iconStream == null) return null;
			
			BufferedImage img = ImageIO.read(iconStream);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ImageIO.write(img, "png", bout);
			
			return bout.toByteArray();
		} finally {
			if (iconStream != null) try { iconStream.close(); } catch (Exception e) {/* ignore this */}
		}
	}
	
	/**
	 * This method is called when using the URL
	 * <pre>http://localhost:8282/configurations/[bundleID]/[servicePID]/properties</pre>
	 * 
	 * @return a list of {@link PropertyResource properties} that belong to this {@link ConfigurationResource configuration}
	 */
	@GET
	@Path("properties")
	public List<PropertyResource> getProperties() {
		List<PropertyResource> configProps = new ArrayList<PropertyResource>();
		
		// getting the property values (may be null)
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> props = this.config.getProperties();
		
		// getting the meta-type information for the properties
		Map<String, AttributeDefinition> attribs = this.getAttributeDefinitions(filter);
		
		// loop through all attribs
		for (Entry<String, AttributeDefinition> attrib : attribs.entrySet()) {
			String propertyID = attrib.getKey();
			AttributeDefinition propertyMetaData = attrib.getValue();
			Object propertyValue = (props==null)?null:props.get(propertyID);
			
			configProps.add(new PropertyResource(
					propertyID,
					propertyValue,
					propertyMetaData
			));
		}
		
		return configProps; 
	}	
	
	/**
	 * This method is called when using the URL
	 * <pre>http://localhost:8282/configurations/[bundleID]/[servicePID]/[propertyID]</pre>
	 * 
	 * @param propertyID the name of the property
	 * @param filter
	 * @return the {@link PropertyResource} for the given propertyID
	 */
	@Path("{propertyID}")
	public PropertyResource getProperty(@PathParam("propertyID") String propertyID) {
		List<PropertyResource> properties = this.getProperties();
		
		for (PropertyResource property : properties) {
			if (property.getID().equalsIgnoreCase(propertyID)) {
				return property;
			}
		}
		
		throw new WebApplicationException(Status.NOT_FOUND);
	}
	
	@POST
	@Path("properties")
	public Response setProperties(List<PropertyResource> props) throws IOException {
		return this.setProperties(props, false);
	}
	
	/**
	 * A function to update the current configuration with the new values provided by the {@link PropertyResource property}-list
	 * received as input parameter.
	 * 
	 * @param props the new configuration to use
	 * @param validateOnly can be set to <code>true</code> to just validate the received configuration but without updating the current configuration
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@POST
	@Path("properties")
	public Response setProperties(
			List<PropertyResource> props, 
			@DefaultValue("false") @QueryParam("validateOnly") boolean validateOnly
	) throws IOException {
		if (props == null || props.size() == 0) {
			ResponseBuilder builder = Response.status(Status.BAD_REQUEST);
			return builder.build();
		}
		
		// metatype-data about the configurable-properties
		Map<String, AttributeDefinition> metaData = this.getAttributeDefinitions(-1);
		
		// a list containing all validation errors
		List<PropertyValidationResult> validationResults = new ArrayList<PropertyValidationResult>();
		
		// a list containing all converted properties
		Map<String,Object> convertedProps = new HashMap<String, Object>();
		
		for (PropertyResource prop : props) {
			// getting the received data
			String key = prop.getID();
			Object originalValue = prop.getValue();
			AttributeDefinition attrMetaData = metaData.get(key);
			
			// checking if the property is known
			if (!metaData.containsKey(key)) {
				validationResults.add(new PropertyValidationResult(
						key,
						"Unknown property" // TODO: localization required
				));
				continue;
			}

			// converting everything into strings (if required
			List<String> stringValues = new ArrayList<String>();
			if (List.class.isInstance(originalValue)) {
				for (Object item : (List<Object>)originalValue) {					
					stringValues.add(item.toString());
				}
			} else if (originalValue.getClass().isArray()) {
				for (int i=0; i < Array.getLength(originalValue); i++) {
					Object item = Array.get(originalValue, i);
					stringValues.add(item.toString());
				}
			} else {
				stringValues.add(originalValue.toString());
			}
			
			// validating values
			for (String valueItem : stringValues) {
				String validation = attrMetaData.validate(valueItem);
				if (validation != null && validation.length() > 0) {
					// an validation error occured
					validationResults.add(new PropertyValidationResult(key,validation));
					continue;
				}
			}			
			
			// value conversion
			try {
				Object convertedValue = PropertyResource.convertValue(stringValues, attrMetaData);
				convertedProps.put(key, convertedValue);
			} catch (Exception e) {
				validationResults.add(new PropertyValidationResult(key,e.getMessage()));
			}
		}
		
		// if there are errors we send an error-message back
		if (validationResults.size() > 0) {
			ResponseBuilder builder = Response.status(Status.BAD_REQUEST);
			builder.entity(new GenericEntity<List<PropertyValidationResult>>(validationResults) {});
			return builder.build();
		}
		
		if (!validateOnly) {		
			// getting the current property values (may be null)
			Dictionary<String, Object> currentProps = this.config.getProperties();
			if (currentProps == null) currentProps = new Hashtable<String, Object>();
		
			// updating values
			for (Entry<String,Object> convertedProp : convertedProps.entrySet()) {
				currentProps.put(convertedProp.getKey(), convertedProp.getValue());
			}
			
			try {
				// updating CM properties
				this.config.update(currentProps);				
			} catch (Exception e) {
				throw new WebApplicationException(e);
			}
		}
		return Response.ok()
				.entity(new GenericEntity<List<PropertyValidationResult>>(validationResults) {})
				.build();
	}
	
	private Map<String, AttributeDefinition> getAttributeDefinitions(int filter) {
		LinkedHashMap<String, AttributeDefinition> attribs = new LinkedHashMap<String, AttributeDefinition>();

		// metatype-data about the managed-service	
		ObjectClassDefinition ocd = this.getObjectClassDefinition();
		if (ocd == null) return attribs;
		
		// metatype-data about the configurable-properties
		AttributeDefinition[] attribArray = ocd.getAttributeDefinitions(filter);
		if (attribArray != null) {
			for (AttributeDefinition attrib : attribArray) {
				String paramID = attrib.getID();
				attribs.put(paramID, attrib);
			}
		}
		
		return attribs;
	}
	
	private String getPreferedLocale() {
		// the default locale is engish
		String localeToUse = Locale.ENGLISH.getLanguage();
		
		// a list of locales supported by the meta-type provider
		String[] supportedLocalesArray = this.metaTypeProvider.getLocales();
		HashSet<String> supportedLocale = new HashSet<String>(Arrays.asList(supportedLocalesArray==null?new String[0]:supportedLocalesArray));
		
		// find best match
		for (String preferedLocale : this.preferedLocales) {
			if (supportedLocale.contains(preferedLocale.toString())) {
				localeToUse = preferedLocale.toString();
				break;
			}
		}
		
		return localeToUse;
	}
	
	private ObjectClassDefinition getObjectClassDefinition() {
		// the PID of the managed-service for which we would like to read the attribute-definitions
		String managedServicePID = this.getPid();
		
		// the preferred locale to use
		String locale = this.getPreferedLocale();
		
		// metatype-data about the managed-service	
		return this.metaTypeProvider.getObjectClassDefinition(managedServicePID, locale);
	}
}
