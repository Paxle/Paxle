/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
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
	
	/**
	 * A list of workers not borrowed from pool.
	 * @see #createWorker()
	 */
	private ArrayList<IWorker<Data>> notPooledWorkers = new ArrayList<IWorker<Data>>();
	
	/**
	 * A factory to create new poolable objects
	 */
	private final PoolableObjectFactory factory;
	
	public Pool(PoolableObjectFactory factory) {
		super(factory);
		this.factory = factory;
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
	
	public int getNotPooledActiveJobCount() {
		return this.notPooledWorkers.size();
	}
	
	/**
	 * @see IPool#getMaxActiveJobCount()
	 */
	public int getMaxActiveJobCount() {
		return this.getMaxActive();
	}
	
	private void addActiveWorker(IWorker<Data> worker, boolean pooled) {
		try {
			this.w.lock();			
			this.activeWorkers.add(worker);
			if (!pooled) this.notPooledWorkers.add(worker);
		} finally {
			this.w.unlock();
		}
	}
	
	private void removeActiveWorker(IWorker<Data> worker) {
		try {
			this.w.lock();
			this.activeWorkers.remove(worker);
			this.notPooledWorkers.remove(worker);
		} finally {
			this.w.unlock();
		}		
	}
	
	private boolean isPooledWorker(IWorker<Data> worker) {
		try {
			this.r.lock();
			return !this.notPooledWorkers.contains(worker);
		} finally {
			this.r.unlock();
		}	
	}
	
	/**
	 * @see IPool#getWorker()
	 * @see GenericObjectPool#borrowObject()
	 */	
	public IWorker<Data> getWorker() throws Exception {
		return this.getWorker(true);
	}
	
	public IWorker<Data> getWorker(boolean fromPool) throws Exception {
		if (this.closed) {
			// thread pool already closed. Notify the master thread about this circumstance
			throw new InterruptedException("Thread pool was closed.");
		}
		
		// creating or borrowing a new worker
		@SuppressWarnings("unchecked")
		IWorker<Data> newWorker = (fromPool)
							    ? (IWorker<Data>) super.borrowObject()
							    : this.createWorker();
							    
        // remember the new active worker
		this.addActiveWorker(newWorker, fromPool);
		
		// return worker
		return newWorker;
	}

	/**
	 * Creates and returns a new worker <strong>without</strong> borrowing it from the internal pool.<br/>
	 * The code of this function was partially copied from {@link GenericObjectPool#borrowObject()}
	 * 
	 * @return
	 * @throws Exception
	 */	
	public IWorker<Data> createWorker() throws Exception {		
		// creating the new object 
		@SuppressWarnings("unchecked")
		IWorker<Data> newWorker = (IWorker<Data>) this.factory.makeObject();
		
        try {
        	// activating worker
        	this.factory.activateObject(newWorker);
        	
        	// validating worker
            if(this.getTestOnBorrow() && !this.factory.validateObject(newWorker)) {
                throw new Exception("ValidateObject failed");
            }
            
            // return worker
            return newWorker;
        } catch (Throwable e) {
            // object cannot be activated or is invalid
            try {
                this.factory.destroyObject(newWorker);
            } catch (Throwable e2) {
                // cannot destroy broken object
            }
            throw new Exception(e);
        }
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
			
			if (this.isPooledWorker(worker)) {
				super.returnObject(worker);	
			} else {
		        this.factory.destroyObject(worker);
			}
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
			if (this.closed) return;
			
			if (this.isPooledWorker(worker)) {
				super.invalidateObject(worker);
			} else {
				this.factory.destroyObject(worker);
			}
		} catch (Exception e) {
        	this.logger.error(String.format(
        			"Unexpected '%s' while invalidating worker thread.",
        			e.getClass().getName()),
        			e
        	);
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
