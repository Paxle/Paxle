package org.paxle.core.queue.impl;

import java.util.ArrayList;

import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.queue.ICommand;

public class FilterInputQueue<Cmd extends ICommand> extends InputQueue<Cmd> implements IFilterQueue {
	private static final long serialVersionUID = 1L;
	
	/**
	 * A list containing all filters that are active for this output-queue
	 */
	private ArrayList<IFilter<Cmd>> filterList = new ArrayList<IFilter<Cmd>>();
	
	public FilterInputQueue(int length) {
		super(length);
	}
	
	@Override
	public void putData(Cmd command) throws InterruptedException {
		switch (command.getResult()) {
			case Failure:
			case Rejected:
		}
		
		this.filter(command);
		
		switch (command.getResult()) {
			case Failure:
			case Passed:  super.putData(command); break;
			case Rejected:
		}		
	}
	
	private void filter(Cmd command) {
		// post-process the command through filters
		for (IFilter<Cmd> filter : this.filterList) {
			try {
				// process the command by the next filter
				filter.filter(command);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}	
}
