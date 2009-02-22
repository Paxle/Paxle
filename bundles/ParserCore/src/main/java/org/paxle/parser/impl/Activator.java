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
import org.paxle.core.IMWComponent;
import org.paxle.core.IMWComponentFactory;
import org.paxle.core.filter.IFilter;
import org.paxle.core.io.IOTools;
import org.paxle.core.io.IResourceBundleTool;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.core.prefs.IPropertiesStore;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IWorkerFactory;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;

public class Activator implements BundleActivator {
	
	/**
	 * A reference to the {@link IMWComponent master-worker-component} used
	 * by this bundle.
	 */
	private IMWComponent<ICommand> mwComponent;	
	
	/**
	 * A class to manage {@link ISubParser sub-parsers}
	 */
	private ISubParserManager subParserManager = null;
	
	/**
	 * A worker-factory to create new parser-worker threads
	 */
	private IWorkerFactory<ParserWorker> workerFactory = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext bc) throws Exception {
		// init the sub-parser manager
		this.subParserManager = this.createAndRegisterSubParserManager(bc);
		
		ServiceReference ref = bc.getServiceReference(IReferenceNormalizer.class.getName());
		IReferenceNormalizer refNorm = null;
		if (ref != null) refNorm = (IReferenceNormalizer)bc.getService(ref);		
		
		// init thead worker-factory
		workerFactory = new WorkerFactory(subParserManager, IOTools.getTempFileManager(), refNorm);
		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */		
		// registering a service listener to notice if a new sub-parser
		// was (un)deployed
		bc.addServiceListener(new SubParserListener((SubParserManager) subParserManager,bc),SubParserListener.FILTER);	
		
		// a listener for the mimetype detector
		bc.addServiceListener(new DetectorListener((WorkerFactory)workerFactory,bc),DetectorListener.FILTER);
		
		/* ==========================================================
		 * Get services provided by other bundles
		 * ========================================================== */			
		// getting a reference to the threadpack generator service
		ServiceReference reference = bc.getServiceReference(IMWComponentFactory.class.getName());
		
		if (reference != null) {
			// getting the service class instance
			IMWComponentFactory componentFactory = (IMWComponentFactory)bc.getService(reference);			
			mwComponent = componentFactory.createCommandComponent(workerFactory, 5, ICommand.class);
			componentFactory.registerComponentServices(mwComponent, bc);
		}			
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */		
		// register the SubParser-Manager as service
		bc.registerService(ISubParserManager.class.getName(), subParserManager, null);		
		
		// register the MimeType filter as service
		Hashtable<String, Object> filterProps = new Hashtable<String, Object>();
		filterProps.put(Constants.SERVICE_PID, MimeTypeFilter.class.getName());
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[]{"org.paxle.parser.in"});
		bc.registerService(IFilter.class.getName(), new MimeTypeFilter(subParserManager), filterProps);				
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
		
		// find available locales for metatye-translation
		List<String> supportedLocale = bt.getLocaleList(ISubParserManager.class.getSimpleName(), Locale.ENGLISH);		
		
		final ServiceReference propertiesRef = bc.getServiceReference(IPropertiesStore.class.getName());
		final IPropertiesStore propsStore = (IPropertiesStore)bc.getService(propertiesRef);
		
		// creating class
		SubParserManager subParserManager = new SubParserManager(
				cm.getConfiguration(SubParserManager.PID),
				supportedLocale.toArray(new String[supportedLocale.size()]),
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
		// shutdown the thread pool
		if (this.mwComponent != null) {
			IMaster master = this.mwComponent.getMaster();
			master.terminate();
		}
		if (this.subParserManager != null) {
			this.subParserManager.close();
			this.subParserManager = null;
		}
		
		// cleanup
		this.subParserManager = null;
		this.workerFactory = null;
	}
}