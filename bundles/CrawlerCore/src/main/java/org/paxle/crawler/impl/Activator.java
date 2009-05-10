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
package org.paxle.crawler.impl;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.metatype.MetaTypeProvider;
import org.paxle.core.io.IResourceBundleTool;
import org.paxle.core.prefs.IPropertiesStore;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ISubCrawlerManager;

public class Activator implements BundleActivator {
	
	/**
	 * A component to manage {@link ISubCrawler sub-crawlers}
	 */
	private ISubCrawlerManager subCrawlerManager = null;

	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */
	public void start(BundleContext bc) throws Exception {		

		// init the subcrawl manager
		this.subCrawlerManager = this.createAndRegisterSubCrawlerManager(bc);
		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */		
		// registering a service listener to notice if a new sub-crawler was (un)deployed
		bc.addServiceListener(new SubCrawlerListener((SubCrawlerManager)this.subCrawlerManager, bc),SubCrawlerListener.FILTER);		
	}

	/**
	 *  Creates a {@link ISubCrawlerManager subcrawler-manager} and registeres it as
	 *  <ul>
	 *  	<li>{@link ISubCrawlerManager}</li>
	 *  	<li>{@link ManagedService}</li>
	 *  	<li>{@link MetaTypeProvider}</li>
	 *  </ul>
	 *  to the OSGi framework
	 * @throws IOException 
	 * @throws ConfigurationException if the initial configuration of the {@link SubCrawlerManager} fails
	 */
	private ISubCrawlerManager createAndRegisterSubCrawlerManager(BundleContext bc) throws IOException, ConfigurationException {
		final ServiceReference cmRef = bc.getServiceReference(ConfigurationAdmin.class.getName());
		final ConfigurationAdmin cm = (ConfigurationAdmin) bc.getService(cmRef);
		
		final ServiceReference btRef = bc.getServiceReference(IResourceBundleTool.class.getName());
		final IResourceBundleTool bt = (IResourceBundleTool) bc.getService(btRef); 
		
		// find available locales for metatye-translation
		List<String> supportedLocale = bt.getLocaleList(ISubCrawlerManager.class.getSimpleName(), Locale.ENGLISH);
		 
		final ServiceReference propsStoreRef = bc.getServiceReference(IPropertiesStore.class.getName());
		final IPropertiesStore propsStore = (IPropertiesStore)bc.getService(propsStoreRef);
		
		// creating class
		SubCrawlerManager subCrawlerManager = new SubCrawlerManager(
					cm.getConfiguration(SubCrawlerManager.PID), 
					supportedLocale.toArray(new String[supportedLocale.size()]),
					bc,
					propsStore.getProperties(bc)
		);
		
		// initializing service registration properties
		Hashtable<String, Object> crawlerManagerProps = new Hashtable<String, Object>();
		crawlerManagerProps.put(Constants.SERVICE_PID, SubCrawlerManager.PID);
		
		// registering as services to the OSGi framework
		bc.registerService(new String[]{ManagedService.class.getName(), MetaTypeProvider.class.getName()}, subCrawlerManager, crawlerManagerProps);
		bc.registerService(ISubCrawlerManager.class.getName(), subCrawlerManager, null);
				
		return subCrawlerManager;
	}
	

	
	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
		if (this.subCrawlerManager != null) {
			this.subCrawlerManager.close();
			this.subCrawlerManager = null;
		}
		
		// cleanup
		this.subCrawlerManager = null;
	}
}