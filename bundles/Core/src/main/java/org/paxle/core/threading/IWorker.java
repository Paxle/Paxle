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

package org.paxle.core.threading;

import org.paxle.core.queue.IInputQueue;
import org.paxle.core.queue.IOutputQueue;

public interface IWorker<Data> {
    /**
     * This method is called by the {@link IMaster master-thread} to
     * assign a new command to the worker
     * @param cmd the command to execute
     * @param outQueue the output-queue where the modified command should
     * be enqueue
     */
	public void assign(Data cmd);
	
	/**
	 * This method is called by the {@link IMaster master-thread} to
     * signal the worker to fetch a next command from the input-queue
	 */
	public void trigger() throws InterruptedException;
	
	/**
	 * @return the command that was assigned by the {@link IMaster master-thread} to the worker
	 * or <code>null</code> if nothing is assigned at the moment.
	 */
	public Data getAssigned();
	
	/**
	 * Terminate the worker. This function is called by {@link IPool#close()} to terminate
	 * all workers associated to the {@link IPool worker-thread-pool}.
	 */
	public void terminate();
    
    /**
     * Destroys a worker. This function is called by {@link IPool#returnWorker(IWorker)}
     * if there are too many workers in the pool and therefore some need to be terminated
     */
    public void destroy();
	
	/**
	 * Called from the worker thread pool on worker creation time to set a 
	 * reference to the worker-pool
	 * @param pool the worker-pool
	 */
    public void setPool(IPool<Data> pool);
    
    /**
     * Called from the worker-thread-pool on worker creation time to set a
     * reference to the outgoing command queue
     * @param outQueue
     */
    public void setOutQueue(IOutputQueue<Data> outQueue);
    
    /**
     * Called from the worker-thread-pool on worker creatin time to set a
     * reference to the input command queue
     * @param inQueue
     */
    public void setInQueue(IInputQueue<Data> inQueue);
}
