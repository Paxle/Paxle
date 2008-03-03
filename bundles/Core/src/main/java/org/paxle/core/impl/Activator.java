
package org.paxle.core.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceListener;
import org.paxle.core.ICryptManager;
import org.paxle.core.IMWComponentFactory;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.data.impl.DataListener;
import org.paxle.core.data.impl.DataManager;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterManager;
import org.paxle.core.filter.impl.AscendingPathUrlExtractionFilter;
import org.paxle.core.filter.impl.FilterListener;
import org.paxle.core.filter.impl.FilterManager;
import org.paxle.core.filter.impl.ReferenceNormalizationFilter;
import org.paxle.core.filter.impl.URLStreamHandlerListener;
import org.paxle.core.io.IOTools;
import org.paxle.core.io.temp.impl.TempFileManager;
import org.paxle.core.prefs.IPropertiesStore;
import org.paxle.core.prefs.impl.PropertiesStore;

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
	
	public static CryptManager cryptManager = null;
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
		cryptManager = new CryptManager();
		
		System.out.println("Starting ...");
		System.out.println(
				"\t    ____             __    \r\n" +
				"\t   / __ \\____ __  __/ /__  \r\n" +
				"\t  / /_/ / __ `/ |/_/ / _ \\ \r\n" +
				"\t / ____/ /_/ />  </ /  __/ \r\n" +
			    "\t/_/    \\__,_/_/|_/_/\\___/ \r\n"
		);

		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */
		// register the filter listener
		bc.addServiceListener(new FilterListener(filterManager,tempFileManager,bc),FilterListener.FILTER);		
		
		// register a data-source/sink- and data-producer/consumer-listener
		DataListener dataListener = new DataListener(dataManager,bc);
		bc.addServiceListener(dataListener);
//		bc.addServiceListener(dataListener,DataListener.DATASOURCE_FILTER);
//		bc.addServiceListener(dataListener,DataListener.DATASINK_FILTER);
//		bc.addServiceListener(dataListener,DataListener.DATAPROVIDER_FILTER);
//		bc.addServiceListener(dataListener,DataListener.DATACONSUMER_FILTER);
		
		final CryptListener cryptListener = new CryptListener(bc, cryptManager);
		bc.addServiceListener(cryptListener, CryptListener.FILTER);
		
		/* ==========================================================
		 * Register Services
		 * ========================================================== */		
		// register the master-worker-factory as a service
		context.registerService(IMWComponentFactory.class.getName(), new MWComponentServiceFactory(), null);
		
		// register the filter-manager as service
		context.registerService(IFilterManager.class.getName(), filterManager, null);
		
		// register crypt-manager
		context.registerService(ICryptManager.class.getName(), cryptManager, null);
		IOTools.setTempFileManager(tempFileManager);
		
		// register property store
		context.registerService(IPropertiesStore.class.getName(), new PropertiesStore(), null);
		
		// register protocol-handlers listener which updates the table of known protocols for the reference normalization filter below
		final ServiceListener protocolUpdater = new URLStreamHandlerListener(bc, ReferenceNormalizationFilter.DEFAULT_PORTS);
		bc.addServiceListener(protocolUpdater, URLStreamHandlerListener.FILTER);
		
		// add reference normalization filter
		final Hashtable<String,String[]> props = new Hashtable<String,String[]>();
        props.put(IFilter.PROP_FILTER_TARGET, new String[] {"org.paxle.crawler.in", "org.paxle.parser.out; pos=50"});
        bc.registerService(IFilter.class.getName(), new ReferenceNormalizationFilter(), props);
        
        // add AscendingPathUrlExtraction filter
		final Hashtable<String,String[]> props2 = new Hashtable<String,String[]>();
        props2.put(IFilter.PROP_FILTER_TARGET, new String[] {"org.paxle.parser.out; pos=60;"});
        bc.registerService(IFilter.class.getName(), new AscendingPathUrlExtractionFilter(), props2);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		IOTools.setTempFileManager(null);
		// cleanup
		tempFileManager = null;
		dataManager = null;
		filterManager = null;
		bc = null;
	}
}
