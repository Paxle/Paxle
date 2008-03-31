package org.paxle.core.queue.impl;

import org.paxle.core.IMWComponent;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataSource;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.IOutputQueue;

/**
 * This acts as an {@link IOutputQueue output-queue} for a {@link IMWComponent master-worker-component}
 * and as a {@link IDataSource data-sink} for a {@link IDataConsumer data-consumer}.
 */
public class OutputQueue<Data> extends AQueue<Data> implements IOutputQueue<Data>, IDataSource<Data> {
	private static final long serialVersionUID = 1L;
	
	public OutputQueue(int length) {		
		super(length);
	}	
	
	/**
	 * @see IOutputQueue#enqueue(ICommand)
	 */
	public void enqueue(Data command) throws InterruptedException {
		if (command == null) throw new NullPointerException("Command is null.");
		// add it to the out buffer
		super.put(command);
	}

	public Data getData() throws InterruptedException {
		return super.take();
	}
}
