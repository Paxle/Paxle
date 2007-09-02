package org.paxle.core.impl;

import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.paxle.core.IMWComponent;
import org.paxle.core.IMWComponentFactory;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.impl.FilterInputQueue;
import org.paxle.core.queue.impl.FilteringOutputQueue;
import org.paxle.core.queue.impl.InputQueue;
import org.paxle.core.queue.impl.OutputQueue;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IPool;
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
	 * @param bundle reference to the {@link Bundle} which has requested the
	 * {@link IMWComponentFactory}-service.
	 */
	MWComponentFactory(Bundle bundle) {
		this.bundle = bundle;
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
		
		// creating the queues
		//InputQueue<Data> inQueue = new InputQueue<Data>(queueBufferSize);
		FilterInputQueue<Data> inQueue = new FilterInputQueue<Data>(queueBufferSize);
		FilteringOutputQueue<Data> outQueue = new FilteringOutputQueue<Data>(queueBufferSize);
		
		/* TODO: use bundle-specific informations to ...
		 * 1.) plugin all queue-filters
		 */
		
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
		IPool<Data> pool = new Pool<Data>(factoryWrapper);
		
		// init the factory wrapper
		factoryWrapper.setPool(pool);
		factoryWrapper.setFactory(workerFactory);
		factoryWrapper.setOutQueue(outQueue);
		
		// create a master thread
		IMaster master = new Master<Data>(pool, inQueue);
		((Master)master).setName(this.bundle.getSymbolicName() + ".Master");
		
		// create the component and return it
		MWComponent<Data> component = new MWComponent<Data>(master,pool,inQueue,outQueue);
		this.components.put(clazz, component);
		return component;
	}

	public void registerComponentServices(IMWComponent component, BundleContext bc) {
		this.registerComponentServices(bc.getBundle().getSymbolicName(),component, bc);
	}
	
	public void registerComponentServices(String componentID, IMWComponent component, BundleContext bc) {
		// register component
		Hashtable<String, String> componentProps = new Hashtable<String, String>();
		componentProps.put(IMWComponent.COMPONENT_ID, componentID);
		bc.registerService(IMWComponent.class.getName(), component, componentProps);
		
		// register data-sink
		Hashtable<String,String> dataSinkProps = new Hashtable<String, String>();
		dataSinkProps.put(IDataSink.PROP_DATASINK_ID, componentID + ".sink");		
		bc.registerService(IDataSink.class.getName(), component.getDataSink(), dataSinkProps);
		
		// register data-source
		Hashtable<String,String> dataSourceProps = new Hashtable<String, String>();
		dataSourceProps.put(IDataSource.PROP_DATASOURCE_ID, componentID + ".source");			
		bc.registerService(IDataSource.class.getName(), component.getDataSource(), dataSourceProps);
		
		// register filter queues
		if (component.getDataSource() instanceof IFilterQueue) {
			Hashtable<String,String> filterQueueProps = new Hashtable<String, String>();
			filterQueueProps.put(IFilterQueue.PROP_FILTER_QUEUE_ID, componentID + ".out");			
			bc.registerService(IFilterQueue.class.getName(), component.getDataSource(), filterQueueProps);			
		}
		
		if (component.getDataSink() instanceof IFilterQueue) {
			Hashtable<String,String> filterQueueProps = new Hashtable<String, String>();
			filterQueueProps.put(IFilterQueue.PROP_FILTER_QUEUE_ID, componentID + ".out");			
			bc.registerService(IFilterQueue.class.getName(), component.getDataSink(), filterQueueProps);			
		}		
	}

	public void unregisterComponentServices(String componentID, IMWComponent component, BundleContext bc) {
		// TODO:
	}
}
