package org.paxle.core.queue;

public interface IInputQueue<Data> extends IQueue {
	public Data dequeue() throws InterruptedException;
}
