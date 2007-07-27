package org.paxle.indexer.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.paxle.core.IMWComponent;
import org.paxle.core.IMWComponentManager;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
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
		ServiceReference reference = bc.getServiceReference(IMWComponentManager.class.getName());

		if (reference != null) {
			// getting the service class instance
			IMWComponentManager componentFactory = (IMWComponentManager)bc.getService(reference);
			IWorkerFactory<IndexerWorker> workerFactory = new WorkerFactory();
			mwComponent = componentFactory.createComponent(workerFactory, 5, ICommand.class);
		}			
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */				
		// register indexer
		Hashtable<String, String> parserProps = new Hashtable<String, String>();
		parserProps.put(IMWComponent.COMPONENT_ID, bc.getBundle().getSymbolicName());
		bc.registerService(IMWComponent.class.getName(), mwComponent, parserProps);				
		
		// publish data-sink
		Hashtable<String,String> dataSinkProps = new Hashtable<String, String>();
		dataSinkProps.put(IDataSink.PROP_DATASINK_ID, bc.getBundle().getSymbolicName() + ".sink");		
		bc.registerService(IDataSink.class.getName(), mwComponent.getDataSink(), dataSinkProps);
		
		// publish data-source
		Hashtable<String,String> dataSourceProps = new Hashtable<String, String>();
		dataSourceProps.put(IDataSource.PROP_DATASOURCE_ID, bc.getBundle().getSymbolicName() + ".source");			
		bc.registerService(IDataSource.class.getName(), mwComponent.getDataSource(), dataSourceProps);		
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