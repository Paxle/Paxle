package org.paxle.core.threading.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;


public class Pool<Data> extends GenericObjectPool implements IPool<Data> {
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();
	
	/**
	 * A list of currently active workers
	 * TODO: whould we keep using a list or should we use something else?
	 */
	private ArrayList<IWorker<Data>> activeWorkers = new ArrayList<IWorker<Data>>();
	
	public Pool(PoolableObjectFactory factory) {
		super(factory);
	}
	
	/**
	 * @see IPool#getActiveJobs()
	 */
	public List<Data> getActiveJobs() {
		ArrayList<Data> activeJobs = new ArrayList<Data>(this.activeWorkers.size());
		try {
			this.r.lock();
			for (IWorker<Data> worker : this.activeWorkers) {
				Data job = worker.getAssigned();
				if (job != null) activeJobs.add(job);
			}
		} finally {
			this.r.unlock();
		}
		return activeJobs;
	}
	
	/**
	 * @see IPool#getActiveJobCount()
	 */
	public int getActiveJobCount() {
		return this.activeWorkers.size();
	}
	
	private void addActiveWorker(IWorker<Data> worker) {
		try {
			this.w.lock();
			this.activeWorkers.add(worker);
		} finally {
			this.w.unlock();
		}
	}
	
	private void removeActiveWorker(IWorker<Data> worker) {
		try {
			this.w.lock();
			this.activeWorkers.remove(worker);
		} finally {
			this.w.unlock();
		}		
	}
	
	/**
	 * @see IPool#getWorker()
	 * @see GenericObjectPool#borrowObject()
	 */
	@SuppressWarnings("unchecked")
	public IWorker<Data> getWorker() throws Exception {
		IWorker<Data> newWorker = (IWorker<Data>) super.borrowObject();
		this.addActiveWorker(newWorker);
		return newWorker;
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
		this.removeActiveWorker(worker);		
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
		this.removeActiveWorker(worker);	
	}

	/**
	 * TODO: implementation required ...
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
