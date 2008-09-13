
package org.paxle.core.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

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
import org.paxle.core.io.IOTools;
import org.paxle.core.io.IResourceBundleTool;
import org.paxle.core.io.impl.ResourceBundleToolFactory;
import org.paxle.core.io.temp.impl.CommandTempReleaser;
import org.paxle.core.io.temp.impl.TempFileManager;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.core.norm.impl.ReferenceNormalizer;
import org.paxle.core.norm.impl.URLStreamHandlerListener;
import org.paxle.core.prefs.IPropertiesStore;
import org.paxle.core.prefs.impl.PropertiesStore;
import org.paxle.core.queue.CommandEvent;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandTracker;
import org.paxle.core.queue.impl.CommandTracker;

public class Activator implements BundleActivator, InvocationHandler {
	
	/**
	 * A class to manage {@link IFilter filters}
	 */
	private FilterManager filterManager = null;
	
	/**
	 * A class to manage:
	 * <ul>
	 * 	<li>{@link IDataSource}</li>
	 * 	<li>{@link IDataSink}</li>
	 * 	<li>{@link IDataProvider}</li>
	 * 	<li>{@link IDataConsumer}</li>
	 * </ul>
	 */
	private DataManager<ICommand> dataManager = null;
	
	/**
	 * A component providing cypt-functions, e.g. 
	 * to genererate MD5 checksums, etc.
	 */
	private CryptManager cryptManager = null;
	
	/**
	 * A component to create (and cleanup) temp-files
	 */
	private TempFileManager tempFileManager = null;
	
	/**
	 * A component to normalize URLs
	 */
	private ReferenceNormalizer referenceNormalizer = null;
	
	/**
	 * A component used to track {@link org.paxle.core.queue.Command commands}
	 */
	private CommandTracker commandTracker = null;
	
	/**
	 * This component releases all the temporary files associated with a command on desctruction of the latter object
	 */
	private CommandTempReleaser commandReleaser = null;
	
	/**
	 * For logging
	 */
	private Log logger;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext bc) throws Exception {
		// init logger
		this.logger = LogFactory.getLog(this.getClass());
		
		filterManager = new FilterManager();
		dataManager = new DataManager<ICommand>();
		tempFileManager = new TempFileManager();
		cryptManager = new CryptManager();
		referenceNormalizer = new ReferenceNormalizer();
				
		logger.info("Starting ...");
		System.out.println(
				"\t    ____             __    \r\n" +
				"\t   / __ \\____ __  __/ /__  \r\n" +
				"\t  / /_/ / __ `/ |/_/ / _ \\ \r\n" +
				"\t / ____/ /_/ />  </ /  __/ \r\n" +
			    "\t/_/    \\__,_/_/|_/_/\\___/ \r\n" +
			    "\r\n" +
				"\tVersion: " + bc.getBundle().getHeaders().get(Constants.BUNDLE_VERSION)
		);

		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */
		// register the filter listener
		bc.addServiceListener(new FilterListener(this.filterManager,this.tempFileManager,this.referenceNormalizer,bc),FilterListener.FILTER);		
		
		// register a data-source/sink- and data-producer/consumer-listener
		DataListener dataListener = new DataListener(dataManager,bc);
		bc.addServiceListener(dataListener);
//		bc.addServiceListener(dataListener,DataListener.DATASOURCE_FILTER);
//		bc.addServiceListener(dataListener,DataListener.DATASINK_FILTER);
//		bc.addServiceListener(dataListener,DataListener.DATAPROVIDER_FILTER);
//		bc.addServiceListener(dataListener,DataListener.DATACONSUMER_FILTER);
		
		final CryptListener cryptListener = new CryptListener(bc, this.cryptManager);
		bc.addServiceListener(cryptListener, CryptListener.FILTER);
		
		/* ==========================================================
		 * Register Services
		 * ========================================================== */		
		// register the master-worker-factory as a service
		bc.registerService(IMWComponentFactory.class.getName(), new MWComponentServiceFactory(), null);
		
		// register the filter-manager as service
		bc.registerService(IFilterManager.class.getName(), this.filterManager, null);
		
		// register crypt-manager
		bc.registerService(ICryptManager.class.getName(), this.cryptManager, null);
		IOTools.setTempFileManager(this.tempFileManager);
		
		// register property store
		bc.registerService(IPropertiesStore.class.getName(), new PropertiesStore(), null);
		
		// register protocol-handlers listener which updates the table of known protocols for the reference normalization filter below
		final ServiceListener protocolUpdater = new URLStreamHandlerListener(bc, ReferenceNormalizer.DEFAULT_PORTS);
		bc.addServiceListener(protocolUpdater, URLStreamHandlerListener.FILTER);
		
		// add reference normalizer service
        bc.registerService(IReferenceNormalizer.class.getName(), this.referenceNormalizer, null);
        
        // register rb-tool
        bc.registerService(IResourceBundleTool.class.getName(), new ResourceBundleToolFactory(), null);
        
        // add AscendingPathUrlExtraction filter
		final Hashtable<String,String[]> props2 = new Hashtable<String,String[]>();
        props2.put(IFilter.PROP_FILTER_TARGET, new String[] {
        		// apply filter to the parser-output-queue at position 60
        		String.format("org.paxle.parser.out; %s=%d;",IFilter.PROP_FILTER_TARGET_POSITION, Integer.valueOf(60))
        });
        bc.registerService(IFilter.class.getName(), new AscendingPathUrlExtractionFilter(), props2);
        
        // getting the Event-Admin service
        ServiceReference eventAdminRef = bc.getServiceReference(EventAdmin.class.getName());
        if (eventAdminRef != null) {
        	EventAdmin eventAdmin = (EventAdmin) bc.getService(eventAdminRef);
        	
	        // the command-tracker
	        final Hashtable<String, Object> trackerProps = new Hashtable<String, Object>();
	        trackerProps.put(EventConstants.EVENT_TOPIC, new String[]{CommandEvent.TOPIC_ALL});
	        bc.registerService(new String[]{EventHandler.class.getName(),ICommandTracker.class.getName()}, this.commandTracker = new CommandTracker(eventAdmin), trackerProps);
	        
	        // the command temp releaser
	        commandReleaser = new CommandTempReleaser(tempFileManager, commandTracker);
	        final Hashtable<String,Object> releaserProps = new Hashtable<String,Object>();
	        releaserProps.put(EventConstants.EVENT_TOPIC, new String[] { CommandEvent.TOPIC_DESTROYED });
	        bc.registerService(EventHandler.class.getName(), commandReleaser, releaserProps);
        } else {
        	this.logger.warn("No EventAdmin-service found. Command-tracking will not work.");
        }
        
        this.initEclipseApplication(bc);
	}

	private void initEclipseApplication(BundleContext bc) {
		String osgiFrameworkVendor = bc.getProperty(Constants.FRAMEWORK_VENDOR);
		if (osgiFrameworkVendor.equalsIgnoreCase("Eclipse")) {
			 final Hashtable<String,Object> props = new Hashtable<String,Object>();
			 props.put("eclipse.application", "org.paxle.app");
			
			 try {
				 // we need to load the inferface classes we will implement from the system bundle
				 Class<?> applicationRunnable = bc.getBundle(0).loadClass("org.eclipse.osgi.service.runnable.ApplicationRunnable");
				 Class<?> parameterizedRunnable = bc.getBundle(0).loadClass("org.eclipse.osgi.service.runnable.ParameterizedRunnable");
				 
				 // now we play the role of an eclipse application
				 Object proxy = Proxy.newProxyInstance(
						 bc.getClass().getClassLoader(),
						 new Class[]{applicationRunnable, parameterizedRunnable},
						 this);
			        				 
				 // registering as Equinox application	        
				 bc.registerService(new String[]{
						 "org.eclipse.osgi.service.runnable.ApplicationRunnable",
						 "org.eclipse.osgi.service.runnable.ParameterizedRunnable"
				 }, proxy, props);
			 } catch (Exception e) {
				 e.printStackTrace();
			 }
		}
	}
	
	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		IOTools.setTempFileManager(null);
		// cleanup
		this.tempFileManager = null;
		this.referenceNormalizer = null;
		this.dataManager = null;
		this.filterManager = null;
		if (this.commandTracker != null) {
			this.commandTracker.terminate();
		}
		this.commandReleaser = null;
	}

	/**
	 * @see {@link org.eclipse.osgi.service.runnable.ParameterizedRunnable#run(Object)}
	 * @see {@link org.eclipse.osgi.service.runnable.ApplicationRunnable#stop()}
	 */
	public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("run")) {
			// wait until we need to shutdown
			this.wait();
			
			// return exit code
			return Integer.valueOf(0);
		} else if (method.getName().equals("stop")) {
			// signal equinox to shutdown
			System.setProperty("osgi.noShutdown", "false");
			
			// wakeup main thread
			this.notifyAll();
			
			// return void
			return null;
		} else {
			throw new IllegalArgumentException(String.format("Unknown function call %s", method.toString()));
		}
	}
}
