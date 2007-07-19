package org.paxle.core.queue.impl;

import java.util.concurrent.ArrayBlockingQueue;

import org.paxle.core.IMWComponent;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.IInputQueue;

/**
 * This acts as an {@link IInputQueue input-queue} for a {@link IMWComponent master-worker-component}
 * and as a {@link IDataSink data-sink} for a {@link IDataProvider data-provider}.
 */
public class InputQueue extends ArrayBlockingQueue<ICommand> 
	implements IInputQueue, IDataSink<ICommand> {
	private static final long serialVersionUID = 1L;	

	public InputQueue(int length) {
		super(length);
	}

	/**
	 * @see IInputQueue#dequeue()
	 */
	public ICommand dequeue() throws InterruptedException {
		return super.take();
	}

	/**
	 * @param data
	 * @throws InterruptedException 
	 * @see IDataSink#setData(Object)
	 */
	public void putData(ICommand data) throws InterruptedException {
		super.put(data);
	}
}
