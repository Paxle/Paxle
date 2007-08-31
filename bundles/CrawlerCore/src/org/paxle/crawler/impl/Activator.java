package org.paxle.crawler.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.paxle.core.IMWComponent;
import org.paxle.core.IMWComponentManager;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.filter.IFilter;
import org.paxle.core.io.IOTools;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.IMaster;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ISubCrawlerManager;

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
	 * A component to manage {@link ISubCrawler sub-crawlers}
	 */
	public static SubCrawlerManager subCrawlerManager = null;

	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */
	public void start(BundleContext context) throws Exception {		
		bc = context;
		subCrawlerManager = new SubCrawlerManager();
		
		WorkerFactory workerFactory = new WorkerFactory(subCrawlerManager, IOTools.getTempFileManager());
		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */		
		// registering a service listener to notice if a new sub-crawler was (un)deployed
		bc.addServiceListener(new SubCrawlerListener(subCrawlerManager, bc),SubCrawlerListener.FILTER);
		
		// a listener for the mimetype detector
		bc.addServiceListener(new DetectorListener((WorkerFactory)workerFactory,bc),DetectorListener.FILTER);		
		
		/* ==========================================================
		 * Get services provided by other bundles
		 * ========================================================== */			
		// getting a reference to the threadpack generator service
		ServiceReference reference = bc.getServiceReference(IMWComponentManager.class.getName());

		if (reference != null) {
			// getting the service class instance
			IMWComponentManager componentFactory = (IMWComponentManager)bc.getService(reference);
			mwComponent = componentFactory.createCommandComponent(workerFactory, 5, ICommand.class);
		}
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */
		// register crawler
		Hashtable<String, String> crawlerProps = new Hashtable<String, String>();
		crawlerProps.put(IMWComponent.COMPONENT_ID, bc.getBundle().getSymbolicName());
		bc.registerService(IMWComponent.class.getName(), mwComponent, crawlerProps);
		
		// register the SubCrawler-Manager as service
		bc.registerService(ISubCrawlerManager.class.getName(), subCrawlerManager, null);
		
		// register the protocol filter as service
		/* TODO: which properties should be set for the filter service?
		 *  - maybe set the filtering-output-queue(s) it shall be added to?
		 *    Respectively the data-source, but this would require a special FilteringDataSource-class
		 *    to avoid interferences between DataSource<Data> and DataSource<Data extends ICommand>.
		 *    Another approach might be to remove the restriction for <Cmd extends ICommand> from the IFilter-class
		 *    and to therefore enable filtering other than ICommand-data-types which might redundantise the
		 *    FilteringOutputQueue but then we might create other problems concerning generics */
		bc.registerService(IFilter.class.getName(), new ProtocolFilter(subCrawlerManager), null);
		
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
		subCrawlerManager = null;
	}
}