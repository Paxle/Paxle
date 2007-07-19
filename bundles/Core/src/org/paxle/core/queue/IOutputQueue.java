package org.paxle.core.queue;

public interface IOutputQueue {
	public void enqueue(ICommand command) throws InterruptedException;
}
