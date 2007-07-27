package org.paxle.core.threading.impl;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;


public class Pool<Data> extends GenericObjectPool implements IPool<Data> {

	public Pool(PoolableObjectFactory factory) {
		super(factory);
	}
	
	/**
	 * @see IPool#getWorker()
	 * @see GenericObjectPool#borrowObject()
	 */
	@SuppressWarnings("unchecked")
	public IWorker<Data> getWorker() throws Exception {
		return (IWorker<Data>) super.borrowObject();
	}

	/**
	 * @see IPool#returnWorker(IWorker)
	 * @see GenericObjectPool#returnObject(Object)
	 */
	public void returnWorker(IWorker<Data> worker) {
		try {
			super.returnObject(worker);
		} catch (Exception e) {
			worker.terminate();
		}
	}
	
	/**
	 * @see IPool#invalidateWorker(IWorker)
	 * @see GenericObjectPool#invalidateObject(Object)
	 */
	public void invalidateWorker(IWorker<Data> worker) {
		try {
			super.invalidateObject(worker);
		} catch (Exception e) {
			worker.terminate();
		}
	}

	/**
	 * @see IPool#close()
	 * @see GenericObjectPool#close()
	 */
	public void close() throws Exception {
		// TODO Auto-generated method stub

		super.close();
	}

	/**
	 * @see IPool#closed()
	 */
	public boolean closed() {
		// TODO Auto-generated method stub
		return false;
	}
}
