package org.paxle.core.impl;

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
	 * The {@link MWComponent master-worker-component} created by this factory. 
	 */
	private MWComponent component = null;	

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
	public IMWComponent createComponent(IWorkerFactory<IWorker> workerFactory, int queueBufferSize) {	
		if (workerFactory == null) throw new NullPointerException("The worker-factory is null");
		if (this.component != null) return this.component;

		// creating the queues
		InputQueue inQueue = new InputQueue(queueBufferSize);
		OutputQueue outQueue = new OutputQueue(queueBufferSize);		
		
		/* TODO: use bundle-specific informations to ...
		 * 1.) plugin all queue-filters
		 * 2.) connect the queues with data-providers and data-consumers
		 */
		
		// wrap the IWorkerFactory into a PoolableObjectFactory
		WorkerFactoryWrapper factoryWrapper = new WorkerFactoryWrapper();

		// create a new thread pool
		IPool pool = new Pool(factoryWrapper);

		// init the factory wrapper
		factoryWrapper.setPool(pool);
		factoryWrapper.setFactory(workerFactory);
		factoryWrapper.setOutQueue(outQueue);

		// create a master thread
		IMaster master = new Master(pool, inQueue);
		((Master)master).setName(this.bundle.getSymbolicName() + ".Master");

// generate a dummy message
//		try {
//			((InputQueue)inQueue).put(new Command());
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// create the component and return it
		component = new MWComponent(master,pool,inQueue,outQueue);
		return component;
	}
}
