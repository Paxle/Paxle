package org.paxle.core.queue;

public interface IInputQueue<Data> extends IQueue {
	public Data dequeue() throws InterruptedException;
	
	/**
	 * Function blocks the caller until new data
	 * is available for dequeueing via {@link #dequeue()}
	 * @throws InterruptedException
	 */
	public void waitForNext() throws InterruptedException;
}
