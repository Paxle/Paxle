/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.core.threading.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;


public class Pool<Data> extends GenericObjectPool implements IPool<Data> {
	private Log logger = LogFactory.getLog(this.getClass());
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();
	
	/**
	 * indicates if the pool was {@link #closed()}.
	 */
	private boolean closed = false;
	
	/**
	 * A list of currently active workers
	 * XXX: should we keep using a list or should we use something else?
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
		if (this.closed) {
			// thread pool already closed. Notify the master thread about this circumstance
			throw new InterruptedException("Thread pool was closed.");
		}
		
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
			if (this.closed) {
				// thread pool already closed. Notify the worker thrad about this circumstance.
				throw new InterruptedException("Thread pool was closed");
			}
			super.returnObject(worker);	
		} catch (Exception e) {
			this.logger.error(String.format("Unexpected '%s' while returning worker thread into pool.",e.getClass().getName()),e);
			worker.terminate();
		} finally {
			if (!this.closed) this.removeActiveWorker(worker);
		}
	}
	
	/**
	 * @see IPool#invalidateWorker(IWorker)
	 * @see GenericObjectPool#invalidateObject(Object)
	 */
	public void invalidateWorker(IWorker<Data> worker) {
		try {
			if (!this.closed) super.invalidateObject(worker);
		} catch (Exception e) {
        	this.logger.error(String.format("Unexpected '%s' while invalidating worker thread.",e.getClass().getName()),e);
			worker.terminate();
		} finally {
			if (!this.closed) this.removeActiveWorker(worker);
		}
	}

	/**
	 * @see IPool#close()
	 * @see GenericObjectPool#close()
	 */
	@Override
	public void close() throws Exception {
		this.logger.debug("Closing thread-pool ...");
		this.closed = true;

		try {
			this.w.lock();
			
			// loop through all active workers
			for (IWorker<Data> worker : this.activeWorkers) {
				// terminate worker thread
				worker.terminate();				
			}
		} finally {
			this.w.unlock();
		}	
		
		super.close();
		this.logger.debug("Thread-pool closed.");
	}

	/**
	 * @see IPool#closed()
	 */
	public boolean closed() {
		return this.closed;
	}
}
