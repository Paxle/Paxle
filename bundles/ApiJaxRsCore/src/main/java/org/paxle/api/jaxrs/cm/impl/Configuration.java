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
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.osgi.framework.Bundle;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.metatype.MetaTypeService;
import org.paxle.api.jaxrs.cm.BundleResource;

/**
 * @scr.component 
 * @scr.service interface="java.lang.Object"
 * @scr.property name="javax.ws.rs" type="Boolean" value="true" 
 */  
@Path("/configurations")
public class Configuration {
	
	/**
	 * The context of this component
	 */
	protected ComponentContext ctx;	
	
	/** @scr.reference */
	private ConfigurationAdmin configAdmin;

	/** @scr.reference */
	private MetaTypeService metaTypeService;
	
	protected void activate(ComponentContext ctx) {
		this.ctx = ctx;
	}
	
	@GET
    public List<BundleResource> getBundles(@QueryParam("bundleId") List<String> bundleIdList) throws IOException {
		List<Long> bundleIDs = new ArrayList<Long>();
		
		if (bundleIdList == null || bundleIdList.size() == 0) {		
			for (Bundle bundle : ctx.getBundleContext().getBundles()) {
				bundleIDs.add(new Long(bundle.getBundleId()));
			}
		} else {
			for (String bundleID : bundleIdList) {
				bundleIDs.add(Long.valueOf(bundleID));
			}
		}
		
		List<BundleResource> bundles = new ArrayList<BundleResource>();
		for (Long bundleID : bundleIDs) {
			Bundle bundle = this.getBundleByID(bundleID.longValue());
			if (bundle == null) continue;
			
			BundleResource bundleResource = new BundleResource(this.configAdmin,bundle);
			if (!bundleResource.hasConfigs()) continue;
			
			bundles.add(bundleResource);
		}
		
    	return bundles;
	}  
	
	@Path("{bundleId}")
	public BundleResource getBundle(@PathParam("bundleId") long bundleID) throws IOException {
		Bundle bundle = this.getBundleByID(bundleID);
		if (bundle == null) throw new WebApplicationException(Status.NOT_FOUND);
		
		BundleResource bundleResource = new BundleResource(this.configAdmin,bundle);
		if (!bundleResource.hasConfigs()) return null;
		
		return bundleResource;
	}	
	
	private Bundle getBundleByID(long bundleID) {
		return ctx.getBundleContext().getBundle(bundleID);
	}
}
