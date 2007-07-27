package org.paxle.core.queue;

public interface IOutputQueue<Data> {
	public void enqueue(Data command) throws InterruptedException;
}
