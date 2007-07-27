package org.paxle.core.threading;

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
	
	public void invalidateWorker(IWorker<Data> worker);
	
	/**
	 * Close the thread pool and interrupts all running threads 
	 */
	public void close() throws Exception;
	
	public boolean closed();
}
