package org.paxle.core.impl;

import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.paxle.core.IMWComponent;
import org.paxle.core.IMWComponentManager;
import org.paxle.core.queue.impl.InputQueue;
import org.paxle.core.queue.impl.OutputQueue;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;
import org.paxle.core.threading.impl.Master;
import org.paxle.core.threading.impl.Pool;
import org.paxle.core.threading.impl.WorkerFactoryWrapper;

public class MWComponentManager implements IMWComponentManager {
	
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
	 * {@link IMWComponentManager}-service.
	 */
	MWComponentManager(Bundle bundle) {
		this.bundle = bundle;
	}
	
	/**
	 * @see IMWComponentManager#createComponent(IWorkerFactory, int)
	 */
	public <Data, W extends IWorker<Data>> IMWComponent<Data> createComponent(
			IWorkerFactory<W> workerFactory,
			int queueBufferSize,
			Class<Data> clazz) {	
		if (workerFactory == null) throw new NullPointerException("The worker-factory is null");
		
		//if (this.component != null) return this.component;
		if (this.components.containsKey(clazz))
			return (MWComponent<Data>)this.components.get(clazz);

		// creating the queues
		InputQueue<Data> inQueue = new InputQueue<Data>(queueBufferSize);
		OutputQueue<Data> outQueue = new OutputQueue<Data>(queueBufferSize);		
		
		/* TODO: use bundle-specific informations to ...
		 * 1.) plugin all queue-filters
		 * 2.) connect the queues with data-providers and data-consumers
		 */
		
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

// generate a dummy message
//		try {
//			((InputQueue)inQueue).put(new Command());
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// create the component and return it
		MWComponent<Data> component = new MWComponent<Data>(master,pool,inQueue,outQueue);
		this.components.put(clazz, component);
		return component;
	}
}
