package org.paxle.core.threading.impl;

import org.apache.commons.pool.PoolableObjectFactory;
import org.paxle.core.queue.IInputQueue;
import org.paxle.core.queue.IOutputQueue;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;


/**
 * Wraps a {@link IWorkerFactory} into a {@link PoolableObjectFactory}
 * which is required by Apache commons pool. 
 */
public class WorkerFactoryWrapper<Data,E extends IWorker<Data>> implements PoolableObjectFactory {
	private IPool<Data> pool = null;
	private IWorkerFactory<E> factory = null;
	private IOutputQueue<Data> outQueue = null;
	private IInputQueue<Data> inQueue = null;
	
	public void setPool(IPool<Data> pool) {
		this.pool = pool;
	}
	
	public void setFactory(IWorkerFactory<E> factory) {
		this.factory = factory;
	}
	
	public void setInQueue(IInputQueue<Data> inQueue) {
		this.inQueue = inQueue;
	}
	
	public void setOutQueue(IOutputQueue<Data> outQueue) {
		this.outQueue = outQueue;
	}
	
	public void activateObject(Object obj) throws Exception {
		this.factory.initWorker((E)obj);
	}

	public void destroyObject(Object obj) throws Exception {
		((E)obj).destroy();
	}

	public Object makeObject() throws Exception {
		E newWorker = this.factory.createWorker();	
		
		// setting some dependencies
		newWorker.setInQueue(this.inQueue);
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
