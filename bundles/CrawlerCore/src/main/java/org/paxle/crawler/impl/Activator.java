package org.paxle.crawler.impl;

import java.io.IOException;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.metatype.MetaTypeProvider;
import org.paxle.core.IMWComponent;
import org.paxle.core.IMWComponentFactory;
import org.paxle.core.filter.IFilter;
import org.paxle.core.io.IOTools;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.IMaster;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ISubCrawlerManager;

public class Activator implements BundleActivator {
	
	/**
	 * A reference to the {@link IMWComponent master-worker-component} used
	 * by this bundle.
	 */
	private IMWComponent<ICommand> mwComponent;
	
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
		
		// init crawler context
		CrawlerContextLocal crawlerLocal = new CrawlerContextLocal();		
		crawlerLocal.setTempFileManager(IOTools.getTempFileManager());
		
		// init the thread factory
		WorkerFactory workerFactory = new WorkerFactory(this.subCrawlerManager);
		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */		
		// registering a service listener to notice if a new sub-crawler was (un)deployed
		bc.addServiceListener(new SubCrawlerListener((SubCrawlerManager)this.subCrawlerManager, bc),SubCrawlerListener.FILTER);
		
		// a listener for the mimetype detector
		bc.addServiceListener(new DetectorListener(crawlerLocal,bc),DetectorListener.FILTER);		
		
		/* ==========================================================
		 * Get services provided by other bundles
		 * ========================================================== */			
		// getting a reference to the threadpack generator service
		ServiceReference reference = bc.getServiceReference(IMWComponentFactory.class.getName());

		if (reference != null) {
			// getting the service class instance
			IMWComponentFactory componentFactory = (IMWComponentFactory)bc.getService(reference);
			this.mwComponent = componentFactory.createCommandComponent(workerFactory, 5, ICommand.class);
			componentFactory.registerComponentServices(this.mwComponent, bc);
		}
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */
		
		// register the protocol filter as service
		Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {"org.paxle.crawler.in","org.paxle.parser.out"});
		bc.registerService(IFilter.class.getName(), new ProtocolFilter(this.subCrawlerManager), filterProps);
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
		
		// creating class
		SubCrawlerManager subCrawlerManager = new SubCrawlerManager(cm.getConfiguration(SubCrawlerManager.PID));		
		
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
		// shutdown the thread pool
		if (this.mwComponent != null) {
			IMaster master = this.mwComponent.getMaster();
			master.terminate();
		}
		
		// cleanup
		this.subCrawlerManager = null;
	}
}