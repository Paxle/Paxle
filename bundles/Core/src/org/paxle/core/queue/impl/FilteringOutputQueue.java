package org.paxle.core.queue.impl;

import java.util.ArrayList;

import org.paxle.core.filter.IFilter;
import org.paxle.core.queue.ICommand;

public class FilteringOutputQueue<Cmd extends ICommand> extends OutputQueue<Cmd> {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * A list containing all filters that are active for this output-queue
	 */
	private ArrayList<IFilter<Cmd>> filterList = new ArrayList<IFilter<Cmd>>();
	
	public FilteringOutputQueue(int length) {
		super(length);
	}
	
	@Override
	public void enqueue(Cmd command) throws InterruptedException {
		// TODO Auto-generated method stub
		filter(command);
		super.enqueue(command);
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
