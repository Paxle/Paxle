package org.paxle.core.queue;

public interface IInputQueue extends IQueue {
	public ICommand dequeue() throws InterruptedException;
}
