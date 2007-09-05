package org.paxle.core.queue.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.queue.ICommand;

public class FilteringOutputQueue<Cmd extends ICommand> extends OutputQueue<Cmd> implements IFilterQueue {
	private static final long serialVersionUID = 1L;
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A list containing all filters that are active for this output-queue
	 */
	private List<IFilterContext> filterList = null;
	
	public FilteringOutputQueue(int length) {
		super(length);
	}
	
	@Override
	public void enqueue(Cmd command) throws InterruptedException {
		switch (command.getResult()) {
			case Failure:
			case Rejected:
		}
		
		this.filter(command);
		
		switch (command.getResult()) {
			case Failure:
			case Passed:  super.enqueue(command); break;
			case Rejected: 
		}
	}
	
	private void filter(Cmd command) {
		if (this.filterList == null) return;
		
		// post-process the command through filters
		for (IFilterContext filterContext : this.filterList) {
			IFilter<ICommand> filter = null;
			try {
				filter = filterContext.getFilter();
				
				// process the command by the next filter
				filter.filter(command, filterContext);
				if (command.getResult() == ICommand.Result.Rejected) {
					this.logger.warn(String.format("Command for URL '%s' rejected by filter '%s'. Reason: '%s'",
							command.getLocation(), filter.getClass().getName(), command.getResultText()
					));
				}
			} catch (Exception e) {
				this.logger.error(String.format("Filter '%s' terminated with exception.",(filter==null)?"unknown":filter.getClass().getName()),e);
			}
		}
	}

	/**
	 * @see IFilterQueue#setFilters(List)
	 */	
	public void setFilters(List<IFilterContext> filters) {
		filterList = filters;
	}
}
