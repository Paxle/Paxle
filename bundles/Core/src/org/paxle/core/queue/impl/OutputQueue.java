package org.paxle.core.queue.impl;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import org.paxle.core.IMWComponent;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataSource;
import org.paxle.core.filter.IFilter;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.IOutputQueue;

/**
 * This acts as an {@link IOutputQueue output-queue} for a {@link IMWComponent master-worker-component}
 * and as a {@link IDataSource data-sink} for a {@link IDataConsumer data-consumer}.
 */
public class OutputQueue extends ArrayBlockingQueue<ICommand> 
	implements IOutputQueue, IDataSource<ICommand> {
	private static final long serialVersionUID = 1L;
	
	/**
	 * A list containing all filters that are active for this output-queue
	 */
	private ArrayList<IFilter> filterList = new ArrayList<IFilter>();
	
	public OutputQueue(int length) {		
		super(length);
	}	
	
	/**
	 * @see IOutputQueue#enqueue(ICommand)
	 */
	public void enqueue(ICommand command) throws InterruptedException {
		if (command == null) throw new NullPointerException("Command is null.");
		
		// post-process the command through filters
		for (IFilter<ICommand> filter : this.filterList) {
			try {
				// process the command by the next filter
				filter.filter(command);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// add it to the out buffer
		super.put(command);
	}

	public ICommand getData() throws InterruptedException {
		return super.take();
	}

}
