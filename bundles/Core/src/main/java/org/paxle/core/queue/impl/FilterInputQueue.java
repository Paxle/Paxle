package org.paxle.core.queue.impl;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.impl.MWComponentFactory;
import org.paxle.core.queue.ICommand;

public class FilterInputQueue<Cmd extends ICommand> extends InputQueue<Cmd> implements IFilterQueue {
	private static final long serialVersionUID = 1L;
	
	/**
	 * The ID that was used by the {@link MWComponentFactory} to register
	 * this {@link IFilterQueue} to the OSGi framework.
	 * 
	 * @see #setFilterQueueID(String)
	 */
	private String filterQueueID = null;
	
	/**
	 * for logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A list containing all filters that are active for this output-queue
	 */
	private List<IFilterContext> filterList = null;
	
	public FilterInputQueue(int length) {
		super(length);
	}
	
	/**
	 * @see IFilterQueue#setFilterQueueID(String)
	 */
	public void setFilterQueueID(String filterQueueID) {
		if (this.filterQueueID != null) throw new IllegalStateException("The filter-queue was already set.");
		this.filterQueueID = filterQueueID;
	}

	@Override
	public Cmd dequeue() throws InterruptedException {
		// get next command
		Cmd command = super.dequeue();
		
		switch (command.getResult()) {
			case Failure:
			case Rejected:
		}

		// filtering
		this.filter(command);		

		// only return "passed" commands
		switch (command.getResult()) {
			case Passed:  return command;
			case Failure:
			case Rejected: 
			default: return null;
		}		
	}
	
	@Override
	public void putData(Cmd command) throws InterruptedException {
		super.putData(command); 
	}
	
	private void filter(Cmd command) {
		if (this.filterList == null) return;
		
		// post-process the command through filters
		for (IFilterContext filterContext : this.filterList) {
			IFilter<ICommand> filter = null;
			try {
				
				filter = filterContext.getFilter();
				
				if (this.logger.isTraceEnabled()) {
					this.logger.trace(String.format(
						"[%s] Passing command with URL '%s' to filter '%s' ...",
						this.filterQueueID,
						command.getLocation(),
						(filter == null) ? "null" : filter.getClass().getName()
					));
				}
				
				long start = System.currentTimeMillis();
				
				// process the command by the next filter
				filter.filter(command, filterContext);
				
				if (this.logger.isDebugEnabled()) {
					this.logger.debug(String.format(
							"[%s] Filter '%s' took %d ms processingh URL '%s'.",
							this.filterQueueID,
							(filter == null) ? "null" : filter.getClass().getSimpleName(),
							Long.valueOf(System.currentTimeMillis() - start),
							command.getLocation()
					));
				}
				
				if (command.getResult() == ICommand.Result.Rejected) {
					this.logger.info(String.format(
							"[%s] Command for URL '%s' rejected by filter '%s'. Reason: %s",
							this.filterQueueID,
							command.getLocation(), 
							filter.getClass().getName(), 
							command.getResultText()
					));
				}				
			} catch (Throwable e) {
				this.logger.error(String.format(
						"[%s] Filter '%s' throwed an '%s' while processing '%s'.",
						this.filterQueueID,
						(filter == null) ? "null" : filter.getClass().getName(),
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
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("Queue-ID: ")
		   .append(this.filterQueueID==null?"unknown":this.filterQueueID)
		   .append("\nEnqueued:\n")
		   .append(super.toString())
		   .append("\nFilters:\n")
		   .append(this.filterList==null?"[]":this.filterList.toString());
		
		return buf.toString();
	}
}
