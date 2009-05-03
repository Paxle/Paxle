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
package org.paxle.indexer.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.paxle.core.IMWComponent;
import org.paxle.core.IMWComponentFactory;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IWorkerFactory;

public class Activator implements BundleActivator {
	/**
	 * A reference to the {@link IMWComponent master-worker-component} used
	 * by this bundle.
	 */
	public static IMWComponent<ICommand> mwComponent;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext bc) throws Exception {
		// getting a document-factory to build indexer-docs
		final ServiceReference[] docFactoryRefs = bc.getServiceReferences(
				IDocumentFactory.class.getName(), 
				String.format("(%s=%s)",IDocumentFactory.DOCUMENT_TYPE,IIndexerDocument.class.getName())
		);
		if (docFactoryRefs == null || docFactoryRefs.length == 0) throw new NullPointerException("No doc-factory found.");
		
		final IDocumentFactory idocFactory = (IDocumentFactory) bc.getService(docFactoryRefs[0]);
		if (idocFactory == null) throw new NullPointerException("No doc-factory found.");		
		
		/* ==========================================================
		 * Get services provided by other bundles
		 * ========================================================== */			
		// getting a reference to the threadpack generator service
		ServiceReference reference = bc.getServiceReference(IMWComponentFactory.class.getName());

		if (reference != null) {
			// getting the service class instance
			IMWComponentFactory componentFactory = (IMWComponentFactory)bc.getService(reference);
			IWorkerFactory<IndexerWorker> workerFactory = new WorkerFactory(idocFactory);
			mwComponent = componentFactory.createCommandComponent(workerFactory, 5, ICommand.class);
			componentFactory.registerComponentServices(mwComponent, bc);
		}			
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */				
		// TODO ...
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
		// shutdown the thread pool
		if (mwComponent != null) {
			IMaster<ICommand> master = mwComponent.getMaster();
			master.terminate();
		}
	}
}