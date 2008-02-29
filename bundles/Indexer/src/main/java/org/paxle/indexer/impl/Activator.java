package org.paxle.indexer.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.paxle.core.IMWComponent;
import org.paxle.core.IMWComponentFactory;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IWorkerFactory;

public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	public static BundleContext bc;		
	
	/**
	 * A reference to the {@link IMWComponent master-worker-component} used
	 * by this bundle.
	 */
	public static IMWComponent<ICommand> mwComponent;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception {
		bc = context;		
		
		/* ==========================================================
		 * Get services provided by other bundles
		 * ========================================================== */			
		// getting a reference to the threadpack generator service
		ServiceReference reference = bc.getServiceReference(IMWComponentFactory.class.getName());

		if (reference != null) {
			// getting the service class instance
			IMWComponentFactory componentFactory = (IMWComponentFactory)bc.getService(reference);
			IWorkerFactory<IndexerWorker> workerFactory = new WorkerFactory();
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
			IMaster master = mwComponent.getMaster();
			master.terminate();
		}
		
		// cleanup
		bc = null;
	}
}