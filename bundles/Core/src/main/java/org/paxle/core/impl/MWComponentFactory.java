package org.paxle.core.impl;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.util.tracker.ServiceTracker;
import org.paxle.core.IMWComponent;
import org.paxle.core.IMWComponentFactory;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.queue.AQueue;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.InputQueue;
import org.paxle.core.queue.OutputQueue;
import org.paxle.core.queue.impl.CommandFilterInputQueue;
import org.paxle.core.queue.impl.CommandFilterOutputQueue;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;
import org.paxle.core.threading.impl.Master;
import org.paxle.core.threading.impl.Pool;
import org.paxle.core.threading.impl.WorkerFactoryWrapper;

public class MWComponentFactory implements IMWComponentFactory {
	
	/**
	 * Reference to the bundle that has requested the Threadpack. We need this
	 * information to initialize the implementation-specific parts of the 
	 * queues correctly
	 */
	private Bundle bundle = null;
	
	/**
	 * The {@link MWComponent master-worker-component}s created by this factory, sorted by data-class. 
	 */
	//private MWComponent<?> component = null;

	private Hashtable<Class<?>,IMWComponent<?>> components = new Hashtable<Class<?>,IMWComponent<?>>();
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	private final String[] locales;
	
	/**
	 * @param bundle reference to the {@link Bundle} which has requested the
	 * {@link IMWComponentFactory}-service.
	 */
	MWComponentFactory(Bundle bundle, final String[] locales) {
		this.bundle = bundle;
		this.locales = locales;
	}
	
	/**
	 * @see IMWComponentFactory#createComponent(IWorkerFactory, int)
	 */
	public <Data, W extends IWorker<Data>> IMWComponent<Data> createComponent(
			IWorkerFactory<W> workerFactory,
			int queueBufferSize,
			Class<Data> clazz) {
		//if (this.component != null) return this.component;
		if (this.components.containsKey(clazz))
			return (MWComponent<Data>)this.components.get(clazz);
		
		if (workerFactory == null) throw new NullPointerException("The worker-factory is null");

		// creating the queues
		InputQueue<Data> inQueue = new InputQueue<Data>(queueBufferSize);
		OutputQueue<Data> outQueue = new OutputQueue<Data>(queueBufferSize);
		
		return createComponent(workerFactory, inQueue, outQueue, clazz);
	}
	
	public <Data extends ICommand,W extends IWorker<Data>> IMWComponent<Data> createCommandComponent(
			IWorkerFactory<W> workerFactory,
			int queueBufferSize,
			Class<Data> clazz) {
		if (this.components.containsKey(clazz))
			return (MWComponent<Data>)this.components.get(clazz);
		
		if (workerFactory == null) throw new NullPointerException("The worker-factory is null");
		
		// creating the filter-queues
		CommandFilterInputQueue<Data> inQueue = new CommandFilterInputQueue<Data>(queueBufferSize);
		CommandFilterOutputQueue<Data> outQueue = new CommandFilterOutputQueue<Data>(queueBufferSize);
		
		return createComponent(workerFactory, inQueue, outQueue, clazz);
	}
	
	private <Data,W extends IWorker<Data>> IMWComponent<Data> createComponent(
			IWorkerFactory<W> workerFactory,
			InputQueue<Data> inQueue,
			OutputQueue<Data> outQueue,
			Class<Data> clazz) {
		// wrap the IWorkerFactory into a PoolableObjectFactory
		WorkerFactoryWrapper<Data,W> factoryWrapper = new WorkerFactoryWrapper<Data,W>();
		
		// create a new thread pool
		Pool<Data> pool = new Pool<Data>(factoryWrapper);
		
		// init the factory wrapper
		factoryWrapper.setPool(pool);
		factoryWrapper.setFactory(workerFactory);
		factoryWrapper.setInQueue(inQueue);
		factoryWrapper.setOutQueue(outQueue);
		
		// create a master thread
		IMaster master = new Master<Data>(pool, inQueue);
		((Master<?>)master).setName(this.bundle.getSymbolicName() + ".Master");
		
		// create the component and return it
		MWComponent<Data> component = new MWComponent<Data>(master,pool,inQueue,outQueue, locales);
		this.components.put(clazz, component);
		return component;
	}

	public void registerComponentServices(IMWComponent<?> component, BundleContext bc) throws IOException {
		this.registerComponentServices(
				bc.getBundle().getSymbolicName(),
				(String) bc.getBundle().getHeaders().get(Constants.BUNDLE_NAME),
				(String) bc.getBundle().getHeaders().get(Constants.BUNDLE_DESCRIPTION),
				component, 
				bc
		);
	}
	
	public void registerComponentServices(
			final String componentID, 
			final String componentName,
			final String componentDescription,
			final IMWComponent<?> component, 
			final BundleContext bc
	) throws IOException {
		// creating a service-tracker for the event-admin service
		ServiceTracker eventServiceTracker = new ServiceTracker(bc,EventAdmin.class.getName(),null);
		eventServiceTracker.open();
				
		/**
		 * TODO: we should use the servicetracker instead!
		 */
		ServiceReference ref = bc.getServiceReference(EventAdmin.class.getName());
		EventAdmin eventService = (EventAdmin) ((ref == null) ? null : bc.getService(ref));
		if (eventService == null) {
			this.logger.warn("Event-admin service not found. Command-tracking will not work!");
		}
		
		// register component
		Hashtable<String, String> componentProps = new Hashtable<String, String>();
		componentProps.put(IMWComponent.COMPONENT_ID, componentID);
		bc.registerService(IMWComponent.class.getName(), component, componentProps);
		
		// register data-sink
		Hashtable<String,String> dataSinkProps = new Hashtable<String, String>();
		dataSinkProps.put(IDataSink.PROP_DATASINK_ID, componentID + IMWComponent.POSTFIX_SINK_ID);		
		bc.registerService(IDataSink.class.getName(), component.getDataSink(), dataSinkProps);
		
		// register data-source
		Hashtable<String,String> dataSourceProps = new Hashtable<String, String>();
		dataSourceProps.put(IDataSource.PROP_DATASOURCE_ID, componentID + IMWComponent.POSTFIX_SOURCE_ID);
		bc.registerService(IDataSource.class.getName(), component.getDataSource(), dataSourceProps);
		
		// register filter queues
		IDataSource<?> compDataSource = component.getDataSource();
		if (compDataSource instanceof IFilterQueue) {
			// generating the filterqueue-ID
			String filterQueueID = componentID + ".out";
			
			// setting the filter-queue ID
			((IFilterQueue)compDataSource).setFilterQueueID(filterQueueID);
			
			// set the event-admin service
			if (eventService != null && compDataSource instanceof AQueue) {
				((AQueue<?>)compDataSource).setEventService(eventService);
			}
			
			// register the queue as osgi-service
			Hashtable<String,String> filterQueueProps = new Hashtable<String, String>();
			filterQueueProps.put(IFilterQueue.PROP_FILTER_QUEUE_ID, filterQueueID);			
			bc.registerService(IFilterQueue.class.getName(), compDataSource, filterQueueProps);			
		}
		
		IDataSink<?> compDataSink = component.getDataSink();
		if (compDataSink instanceof IFilterQueue) {
			// generating the filterqueue-ID
			String filterQueueID = componentID + ".in";
			
			// setting the filter-queue ID
			((IFilterQueue)compDataSink).setFilterQueueID(filterQueueID);	
			
			// set the event-admin service
			if (eventService != null && compDataSink instanceof AQueue) {
				((AQueue<?>)compDataSink).setEventService(eventService);
			}			
			
			Hashtable<String,String> filterQueueProps = new Hashtable<String, String>();
			filterQueueProps.put(IFilterQueue.PROP_FILTER_QUEUE_ID, filterQueueID);			
			bc.registerService(IFilterQueue.class.getName(), compDataSink, filterQueueProps);			
		}		
				
		// configure some properties needed by CM
		((MWComponent<?>)component).setComponentID(componentID);
		((MWComponent<?>)component).setEventSender(new MWComponentEventSender(eventServiceTracker));
		((MWComponent<?>)component).setComponentName(componentName==null?componentID:componentName);
		((MWComponent<?>)component).setComponentDescription(componentDescription==null?"":componentDescription);		
		
		// get the config-admin service and set the default configuration if not available
		ServiceReference cmRef = bc.getServiceReference(ConfigurationAdmin.class.getName());
		if (cmRef != null) {
			ConfigurationAdmin cm = (ConfigurationAdmin) bc.getService(cmRef);
			Configuration config = cm.getConfiguration(componentID);
			((MWComponent<?>)component).setConfiguration(config);
			if (config.getProperties() == null) {
				config.update(((MWComponent<?>)component).getDefaults());
			}
		}	
		
		// sevice properties for registration
		Hashtable<String, Object> managedServiceProps = new Hashtable<String, Object>();
		managedServiceProps.put(Constants.SERVICE_PID, componentID);
		
		// register as services
		bc.registerService(new String[]{ManagedService.class.getName(),MetaTypeProvider.class.getName()}, component, managedServiceProps);		
	}

	public void unregisterComponentServices(String componentID, IMWComponent<?> component, BundleContext bc) {
		// TODO:
	}
}
