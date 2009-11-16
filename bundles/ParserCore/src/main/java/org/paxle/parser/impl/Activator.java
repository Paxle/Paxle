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
package org.paxle.parser.impl;

import java.io.IOException;
import java.util.Hashtable;

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
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;

public class Activator implements BundleActivator {

	/**
	 * A class to manage {@link ISubParser sub-parsers}
	 */
	private ISubParserManager subParserManager = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext bc) throws Exception {
		// init the sub-parser manager
		this.subParserManager = this.createAndRegisterSubParserManager(bc);

		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */		
		// registering a service listener to notice if a new sub-parser
		// was (un)deployed
		bc.addServiceListener(new SubParserListener((SubParserManager) subParserManager,bc),SubParserListener.FILTER);			
	}
	
	/**
	 *  Creates a {@link ISubParserManager subparser-manager} and registeres it as
	 *  <ul>
	 *  	<li>{@link ISubParserManager}</li>
	 *  	<li>{@link ManagedService}</li>
	 *  	<li>{@link MetaTypeProvider}</li>
	 *  </ul>
	 *  to the OSGi framework
	 * @throws IOException 
	 * @throws ConfigurationException if the initial configuration of the {@link ISubParserManager} fails
	 */
	private ISubParserManager createAndRegisterSubParserManager(BundleContext bc) throws IOException, ConfigurationException {
		final ServiceReference cmRef = bc.getServiceReference(ConfigurationAdmin.class.getName());
		final ConfigurationAdmin cm = (ConfigurationAdmin) bc.getService(cmRef);
		
		final ServiceReference btRef = bc.getServiceReference(IResourceBundleTool.class.getName());
		final IResourceBundleTool bt = (IResourceBundleTool) bc.getService(btRef); 
		
		final ServiceReference propertiesRef = bc.getServiceReference(IPropertiesStore.class.getName());
		final IPropertiesStore propsStore = (IPropertiesStore)bc.getService(propertiesRef);
		
		// creating class
		SubParserManager subParserManager = new SubParserManager(
				cm.getConfiguration(SubParserManager.PID),
				bt,
				bc,
				propsStore.getProperties(bc)
		);		
		
		// initializing service registration properties
		Hashtable<String, Object> parserManagerProps = new Hashtable<String, Object>();
		parserManagerProps.put(Constants.SERVICE_PID, SubParserManager.PID);
		
		// registering as services to the OSGi framework
		bc.registerService(new String[]{ManagedService.class.getName(), MetaTypeProvider.class.getName()}, subParserManager, parserManagerProps);
		bc.registerService(ISubParserManager.class.getName(), subParserManager, null);
		
		return subParserManager;
	}	
	
	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
		if (this.subParserManager != null) {
			this.subParserManager.close();
			this.subParserManager = null;
		}
		
		// cleanup
		this.subParserManager = null;
	}
}