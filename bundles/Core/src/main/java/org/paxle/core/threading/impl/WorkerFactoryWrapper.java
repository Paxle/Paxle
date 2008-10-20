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
	
	@SuppressWarnings("unchecked")
	public void activateObject(Object obj) throws Exception {
		this.factory.initWorker((E)obj);
	}

	@SuppressWarnings("unchecked")
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
