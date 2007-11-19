package org.paxle.core.threading;

import java.util.List;

public interface IPool<Data> {

	/**
	 * @return a new {@link IWorker worker-thread} from the thread pool
	 * @throws InterruptedException if the thread was interrupted while waiting
	 * for a free worker thread.
	 */
	public IWorker<Data> getWorker() throws Exception;
	
	/**
	 * Returns the {@link IWorker worker-thread} into the thread pool
	 * @param worker
	 */
	public void returnWorker(IWorker<Data> worker);
	
	/**
	 * Notifies the pool that the {@link IWorker worker-thread} should not be used anymore
	 * @param worker
	 */
	public void invalidateWorker(IWorker<Data> worker);
	
	/**
	 * @return the list of active jobs currently processed by the workers of this pool 
	 */
	public List<Data> getActiveJobs();
	
	/**
	 * @return the size of the active job queue
	 */
	public int getActiveJobCount();
	
	/**
	 * Close the thread pool and interrupts all running threads 
	 */
	public void close() throws Exception;
	
	public boolean closed();
}
