package org.paxle.core.queue.impl;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.queue.ICommand;

public class FilterInputQueue<Cmd extends ICommand> extends InputQueue<Cmd> implements IFilterQueue {
	private static final long serialVersionUID = 1L;
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A list containing all filters that are active for this output-queue
	 */
	private List<IFilterContext> filterList = null;
	
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
		for (IFilterContext filterContext : this.filterList) {
			IFilter<ICommand> filter = null;
			try {
				filter = filterContext.getFilter();
				
				// process the command by the next filter
				filter.filter(command, filterContext);
				if (command.getResult() == ICommand.Result.Rejected) {
					this.logger.warn(String.format("Command for URL '%s' rejected by filter '%s'. Reason: %s",
							command.getLocation(), filter.getClass().getName(), command.getResultText()
					));
				}				
			} catch (Throwable e) {
				this.logger.error(String.format("Filter '%s' throwed an '%s' while processing '%s'.",
						filter.getClass().getName(),
						e.getClass().getName(),
						command.getLocation()
				),e);
			}
		}
	}

	/**
	 * @see IFilterQueue#setFilters(List)
	 */	
	public void setFilters(List<IFilterContext> filters) {
		filterList = filters;
	}

	/**
	 * @see IFilterQueue#getFilters()
	 */
	@SuppressWarnings("unchecked")
	public List<IFilterContext> getFilters() {
		return (this.filterList==null)?Collections.EMPTY_LIST:this.filterList;
	}
}
