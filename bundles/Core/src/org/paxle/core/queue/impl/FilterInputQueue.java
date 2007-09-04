package org.paxle.core.queue.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.queue.ICommand;

public class FilterInputQueue<Cmd extends ICommand> extends InputQueue<Cmd> implements IFilterQueue {
	private static final long serialVersionUID = 1L;
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A list containing all filters that are active for this output-queue
	 */
	private List<IFilter> filterList = null;
	
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
		if (this.filterList == null) return;
		
		// post-process the command through filters
		for (IFilter<Cmd> filter : this.filterList) {
			try {
				// process the command by the next filter
				filter.filter(command);
				if (command.getResult() == ICommand.Result.Rejected) {
					this.logger.warn(String.format("Command for URL '%s' rejected by filter '%s'. Reason: '%s'",
							command.getLocation(), filter.getClass().getName(), command.getResultText()
					));
				}				
			} catch (Exception e) {
				this.logger.error(String.format("Filter '%s' terminated with exception.",filter.getClass().getName()),e);
			}
		}
	}

	/**
	 * @see IFilterQueue#setFilters(List)
	 */	
	public void setFilters(List<IFilter> filters) {
		filterList = filters;
	}
}
