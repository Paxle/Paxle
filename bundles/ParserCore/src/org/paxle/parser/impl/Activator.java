package org.paxle.parser.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.paxle.core.IMWComponent;
import org.paxle.core.IMWComponentManager;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.filter.IFilter;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;

public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	public static BundleContext bc;		
	
	/**
	 * A reference to the {@link IMWComponent master-worker-component} used
	 * by this bundle.
	 */
	public static IMWComponent mwComponent;	
	
	/**
	 * A class to manage {@link ISubParser sub-parsers}
	 */
	public static SubParserManager subParserManager = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception {
		bc = context;
		subParserManager = new SubParserManager();		
		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */		
		// registering a service listener to notice if a new sub-parser
		// was (un)deployed
		bc.addServiceListener(new SubParserListener(subParserManager,bc),SubParserListener.FILTER);		
		
		/* ==========================================================
		 * Get services provided by other bundles
		 * ========================================================== */			
		// getting a reference to the threadpack generator service
		ServiceReference reference = bc.getServiceReference(IMWComponentManager.class.getName());

		if (reference != null) {
			// getting the service class instance
			IMWComponentManager componentFactory = (IMWComponentManager)bc.getService(reference);
			IWorkerFactory<IWorker> workerFactory = new WorkerFactory(subParserManager);
			mwComponent = componentFactory.createComponent(workerFactory, 5);
		}			
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */
		// register the SubParser-Manager as service
		bc.registerService(ISubParserManager.class.getName(), subParserManager, null);		
		
		// register the MimeType filter as service
		// TODO: which properties should be set for the filter service?
		bc.registerService(IFilter.class.getName(), new MimeTypeFilter(subParserManager), null);				
		
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
		subParserManager = null;
	}
}