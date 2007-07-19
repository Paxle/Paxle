package org.paxle.core.threading;

import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.IOutputQueue;

public interface IWorker {
    /**
     * This method is called by the {@link IMaster master-thread} to
     * assign a new command to the worker
     * @param cmd the command to execute
     * @param outQueue the output-queue where the modified command should
     * be enqueue
     */
	public void assign(ICommand cmd);
	
	/**
	 * Terminate the worker
	 */
	public void terminate();
	
	/**
	 * Called from the worker thread pool on worker creation time to set a 
	 * reference to the worker-pool
	 * @param pool the worker-pool
	 */
    public void setPool(IPool pool);
    
    /**
     * Called from the worker-thread-pool on worker creation time to set a
     * reference to the outgoing command queue
     * @param outQueue
     */
    public void setOutQueue(IOutputQueue outQueue);
}
