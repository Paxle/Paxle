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
package org.paxle.core.metadata.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.metadata.IMetaData;
import org.paxle.core.metadata.IMetaDataService;

/**
 * @scr.component
 * @scr.service interface="org.paxle.core.metadata.IMetaDataService"
 */
public class MetaDataServiceImpl implements IMetaDataService {
	
	/**
	 * The context of this component
	 */
	protected ComponentContext ctx;	
	/**
	 * for logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());

	protected void activate(ComponentContext context) {
		this.ctx = context;
	}	

	public IMetaData getMetadata(String servicePID, String localeStr) {
		// the locale to use
		final Locale locale = localeStr==null ? Locale.ENGLISH : new Locale(localeStr);
		
		// determine if there is a resource-bundle containing all metadata
		MetaDataResourceBundle rb = this.getMetaDataResourceBundle(servicePID, locale);
		if (rb != null) return rb;
		
		// TODO Auto-generated method stub
		return null;
	}

	private MetaDataResourceBundle getMetaDataResourceBundle(String servicePID, Locale locale) {
		try {
			final BundleContext context = this.ctx.getBundleContext();
			
			// getting a reference to the service with the given ID
			ServiceReference[] refs = context.getServiceReferences(
					null, 
					String.format("(%s=%s)",
							Constants.SERVICE_PID,
							servicePID
					)
			);
			if (refs == null || refs.length == 0) return null;
			
			for (int i=0; i < refs.length; i++) {
				Boolean metaData = (Boolean) refs[i].getProperty("org.paxle.metadata");
				if (metaData == null || !metaData.booleanValue()) continue;
				
				// getting the bundle base name
				String bundleBase = (String) refs[i].getProperty("org.paxle.metadata.localization");
				if (bundleBase == null) continue;
				
				// getting the classloader to use
				ClassLoader cl = null; 
				try {
					cl = context.getService(refs[i]).getClass().getClassLoader();
				} finally {
					context.ungetService(refs[i]);
				}
					
    			// loading the resource-bundle
				ResourceBundle rb = (cl == null)
				   ? ResourceBundle.getBundle(bundleBase, locale)
				   : ResourceBundle.getBundle(bundleBase, locale, cl);
				   
				return new MetaDataResourceBundle(servicePID, cl, rb);
			}
		} catch (Exception e) {
			this.logger.debug(e);
		}
		return null;		
	}
	
	private static class MetaDataResourceBundle implements IMetaData {
		private final String pid;
		private final ClassLoader cl;
		private final ResourceBundle rb;
		
		public MetaDataResourceBundle(String pid, ClassLoader cl, ResourceBundle rb) {
			this.pid = pid;
			this.cl = cl;
			this.rb = rb;
		}
		public String getDescription() {
			return this.getString("description");
		}
		
		public String getName() {
			return this.getString("name");
		}
		
		public String getVersion() {
			return this.getString("version");
		}
		
		public InputStream getIcon(int size) throws IOException {
			String iconName = this.getString("icon." + size);
			if (iconName.startsWith(this.pid)) iconName = this.getString("icon");
				
			if (!iconName.startsWith(this.pid)) {
				return this.cl.getResourceAsStream(iconName);
			}
			return null;
		}
		
		private String getString(String keyPostFix) {
			String key = this.pid + "." + keyPostFix;
			try {
				return this.rb.getString(key);
			} catch (MissingResourceException e) {
				return key;
			}
		}
	}
}
