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
package org.paxle.api.jaxrs.cm.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.MetaTypeService;
import org.paxle.api.jaxrs.cm.BundleResource;

/**
 * @scr.component 
 * @scr.service interface="java.lang.Object"
 * @scr.property name="javax.ws.rs" type="Boolean" value="true" private="true"
 * @scr.reference name="metaTypeProviders" 
 * 				  interface="org.osgi.service.metatype.MetaTypeProvider" 
 * 				  cardinality="0..n" 
 * 				  policy="dynamic" 
 * 				  bind="addProvider" 
 * 				  unbind="removeProvider"
 * 				  target="(service.pid=*)
 */  
@Path("/configurations")
public class Configuration {
	
	/**
	 * The context of this component
	 */
	protected ComponentContext ctx;	
	
	/** @scr.reference */
	private ConfigurationAdmin configAdmin;

	/** 
	 * The OSGi {@link MetaTypeService}
	 * @scr.reference 
	 */
	private MetaTypeService metaTypeService;
	
	/**
	 * A map of additional {@link MetaTypeProvider}s. 
	 * The key of this map is the {@link Constants#SERVICE_PID} of the {@link MetaTypeProvider provider},
	 * the value is a {@link ServiceReference} to the {@link MetaTypeProvider provider}.
	 */
	private Map<String, ServiceReference> providersRefs = new HashMap<String, ServiceReference>();
	
	/**
	 * Activation of this component
	 * @param ctx
	 */
	protected void activate(ComponentContext ctx) {
		this.ctx = ctx;
	}
	
	/**
	 * A new {@link MetaTypeProvider} was registered to the system.
	 * @param providerRef
	 */
	protected void addProvider(ServiceReference providerRef) {
		String providerPID = (String) providerRef.getProperty(Constants.SERVICE_PID);		
		this.providersRefs.put(providerPID, providerRef);
	}

	/**
	 * A {@link MetaTypeProvider} was unregistered from the system.
	 * @param providerRef
	 */
	protected void removeProvider(ServiceReference providerRef) {
		String providerPID = (String) providerRef.getProperty(Constants.SERVICE_PID);	
		this.providersRefs.remove(providerPID);
	}	
	
	/**
	 * This method is called when using the URL
	 * <pre>http://localhost:8282/configurations</pre>
	 * or
	 * <pre>http://localhost:8282/configurations/?bundleId=[bundleID]</pre>
	 * 
	 * @param bundleIdList a list of {@link Bundle}-IDs for which a {@link BundleResource} object should be returned. 
	 * 		If this is <code>null</code> all available {@link BundleResource bundles} are returned.
	 * 
	 * @return a list of {@link BundleResource}
	 */
	@GET
    public List<BundleResource> getBundles(@QueryParam("bundleId") List<String> bundleIdList, @HeaderParam("Accept-Language") List<String> languages) throws IOException {
		List<Long> bundleIDs = new ArrayList<Long>();
		
		// getting the IDs of all bundles for which the configuration should be returned
		if (bundleIdList == null || bundleIdList.size() == 0) {		
			for (Bundle bundle : ctx.getBundleContext().getBundles()) {
				bundleIDs.add(new Long(bundle.getBundleId()));
			}
		} else {
			for (String bundleID : bundleIdList) {
				bundleIDs.add(Long.valueOf(bundleID));
			}
		}
		
		// get a list of acceptable languages
		List<String> locales = this.localeList(languages);
		
		// loop through all bundles to find bundles with configurations
		List<BundleResource> bundles = new ArrayList<BundleResource>();
		for (Long bundleID : bundleIDs) {
			// lookup the bundle by ID
			Bundle bundle = this.getBundleByID(bundleID.longValue());
			if (bundle == null) continue;
			
			// determine if there is any metaType-data for the bundle
			Map<String, MetaTypeProvider> metaTypes = this.getMetaTypes(bundle);
			if (metaTypes == null || metaTypes.size() == 0) continue;
			
			BundleResource bundleResource = new BundleResource(this.configAdmin, bundle, metaTypes, locales);
			bundles.add(bundleResource);
		}
		
    	return bundles;
	}  
	
	/**
	 * This method is called when using the URL
	 * <pre>http://localhost:8282/configurations/[bundleId]</pre>
	 */
	@Path("{bundleId}")
	public BundleResource getBundle(@PathParam("bundleId") long bundleID, @HeaderParam("Accept-Language") List<String> languages) throws IOException {
		Bundle bundle = this.getBundleByID(bundleID);
		if (bundle == null) throw new WebApplicationException(Status.NOT_FOUND);
		
		// determine if there is any metaType-data for the bundle
		Map<String, MetaTypeProvider> metaTypes = this.getMetaTypes(bundle);
		if (metaTypes == null || metaTypes.size() == 0) return null;
		
		// get a list of acceptable languages
		List<String> locales= this.localeList(languages);
		
		BundleResource bundleResource = new BundleResource(this.configAdmin, bundle, metaTypes, locales);
		return bundleResource;
	}	
	
	/**
	 * Find an OSGi {@link Bundle} for a given bundleID.
	 * @param bundleID
	 * @return
	 */
	private Bundle getBundleByID(long bundleID) {
		return ctx.getBundleContext().getBundle(bundleID);
	}
	
	/**
	 * Convert the <code>Accept-Languages</code> HTTP-Header into a list
	 * of locale-strings
	 * 
	 * @param languages
	 * @return
	 */
	private List<String> localeList(List<String> languages) {
		ArrayList<String> locales = new ArrayList<String>();
		
		if (languages != null) {
			for (String lang : languages) {
				if (lang != null && lang.length() > 0) {
					int idx = lang.indexOf(';');
					if (idx != -1) {
						lang = lang.substring(0, idx);
					}
					locales.add(lang.trim());
				}
			}
		}
		
		if (locales.size() == 0) locales.add("en");
		return locales;
	}
	
	/**
	 * This method returns a map containing all {@link MetaTypeProvider}s found for the given bundle.
	 * The key of this map is the {@link Constants#SERVICE_PID} of a {@link ManagedService} for which
	 * the {@link MetaTypeProvider} provides metaType-data.
	 * 
	 * @param bundle
	 * @return
	 */
	private Map<String, MetaTypeProvider> getMetaTypes(Bundle bundle) {
		final HashMap<String, MetaTypeProvider> metaTypes = new HashMap<String, MetaTypeProvider>();
		
		// metaType PIDs managed by the metatype service
		MetaTypeInformation mti = this.metaTypeService.getMetaTypeInformation(bundle);
		if (mti != null) {
			String[] pidArray = mti.getPids();
			if (pidArray != null) {
				for (String servicePID : pidArray) {
					metaTypes.put(servicePID, mti);
				}
			}
		}		
		
		// metaType PIDs managed by metatype-providers
		for (Entry<String, ServiceReference> providerRef : this.providersRefs.entrySet()) {
			if (!providerRef.getValue().getBundle().equals(bundle)) continue;
			
			String providerPID = providerRef.getKey();
			MetaTypeProvider provider = (MetaTypeProvider) this.ctx.locateService("metaTypeProviders", providerRef.getValue());
			metaTypes.put(providerPID, provider);
		}		
		
		return metaTypes;
	}
}
