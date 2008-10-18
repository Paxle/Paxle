package org.paxle.core.queue;

import org.paxle.core.data.IDataSource;

/**
 * This acts as an {@link IOutputQueue output-queue} for a {@link IMWComponent master-worker-component}
 * and as a {@link IDataSource data-sink} for a {@link IDataConsumer data-consumer}.
 */
public class OutputQueue<Data> extends AQueue<Data> implements IOutputQueue<Data>, IDataSource<Data> {
	
	private static final long serialVersionUID = 1L;
	
	public OutputQueue(int length) {		
		super(length);
	}
	
	public OutputQueue(final int length, final boolean limited) {
		super(length, limited);
	}
	
	/**
	 * @see IOutputQueue#enqueue(ICommand)
	 */
	public void enqueue(Data command) throws InterruptedException {
		if (command == null) throw new NullPointerException("Command is null.");
		
		// add it to the out buffer
		this.queue.put(command);
	}

	/**
	 * @see IDataSource#getData()
	 */
	public Data getData() throws InterruptedException {
		return this.queue.take();
	}
}
