
package org.paxle.core.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.IMWComponentManager;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.data.impl.DataListener;
import org.paxle.core.data.impl.DataManager;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterManager;
import org.paxle.core.filter.impl.FilterListener;
import org.paxle.core.filter.impl.FilterManager;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.io.temp.impl.TempFileManager;


public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	public static BundleContext bc;	
	
	/**
	 * A class to manage {@link IFilter filters}
	 */
	public static FilterManager filterManager = null;
	
	/**
	 * A class to manage:
	 * <ul>
	 * 	<li>{@link IDataSource}</li>
	 * 	<li>{@link IDataSink}</li>
	 * 	<li>{@link IDataProvider}</li>
	 * 	<li>{@link IDataConsumer}</li>
	 * </ul>
	 */
	public static DataManager dataManager = null;
	
	public static TempFileManager tempFileManager = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception {
		bc = context;
		filterManager = new FilterManager();
		dataManager = new DataManager();
		tempFileManager = new TempFileManager();
		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */
		// register the filter listener
		bc.addServiceListener(new FilterListener(filterManager,bc),FilterListener.FILTER);		
		
		// register a data-source/sink- and data-producer/consumer-listener
		DataListener dataListener = new DataListener(dataManager,bc);
		bc.addServiceListener(dataListener);
//		bc.addServiceListener(dataListener,DataListener.DATASOURCE_FILTER);
//		bc.addServiceListener(dataListener,DataListener.DATASINK_FILTER);
//		bc.addServiceListener(dataListener,DataListener.DATAPROVIDER_FILTER);
//		bc.addServiceListener(dataListener,DataListener.DATACONSUMER_FILTER);

		/* ==========================================================
		 * Register Services
		 * ========================================================== */		
		// register the master-worker-factory as a service
		context.registerService(IMWComponentManager.class.getName(), new MWComponentFactory(), null);
		
		// register the filter-manager as service
		context.registerService(IFilterManager.class.getName(), filterManager, null);
		
		context.registerService(ITempFileManager.class.getName(), tempFileManager, null);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {		
		// cleanup
		tempFileManager = null;
		dataManager = null;
		filterManager = null;
		bc = null;
	}
}