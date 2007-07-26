package org.paxle.core.threading.impl;

import org.apache.commons.pool.PoolableObjectFactory;
import org.paxle.core.queue.IOutputQueue;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;


/**
 * Wraps a {@link IWorkerFactory} into a {@link PoolableObjectFactory}
 * which is required by Apache commons pool. 
 */
public class WorkerFactoryWrapper implements PoolableObjectFactory {
	private IPool pool = null;
	private IWorkerFactory factory = null;
	private IOutputQueue outQueue = null;
	
	public void setPool(IPool pool) {
		this.pool = pool;
	}
	
	public void setFactory(IWorkerFactory factory) {
		this.factory = factory;
	}
	
	public void setOutQueue(IOutputQueue outQueue) {
		this.outQueue = outQueue;
	}
	
	public void activateObject(Object obj) throws Exception {
		this.factory.initWorker((IWorker)obj);
	}

	public void destroyObject(Object obj) throws Exception {
//		 nothing todo here
	}

	public Object makeObject() throws Exception {
		IWorker newWorker = this.factory.createWorker();	
		
		// setting some dependencies
		newWorker.setOutQueue(this.outQueue);
		newWorker.setPool(this.pool);
		
		// return the initialized worker
		return newWorker;
	}

	public void passivateObject(Object obj) throws Exception {
//		 nothing todo here
	}

	public boolean validateObject(Object obj) {
		return true;
	}

}
