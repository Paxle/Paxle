
package org.paxle.core.queue.impl;

import java.util.Collections;
import java.util.List;

import org.paxle.core.filter.CommandFilterEvent;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.queue.CommandEvent;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.OutputQueue;

public class CommandFilterOutputQueue<Cmd extends ICommand> extends OutputQueue<Cmd> implements IFilterQueue {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The ID that was used by the {@link MWComponentFactory} to register
	 * this {@link IFilterQueue} to the OSGi framework.
	 * 
	 * @see #setFilterQueueID(String)
	 */
	protected String filterQueueID = null;
	
	/**
	 * A list containing all filters that are active for this output-queue
	 */
	protected List<IFilterContext> filterList = null;
	
	public CommandFilterOutputQueue(int length) {
		super(length);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.core.filter.IFilterQueue#setFilterQueueID(java.lang.String)
	 */
	public void setFilterQueueID(String filterQueueID) {
		if (this.filterQueueID != null) throw new IllegalStateException("The filter-queue was already set.");
		this.filterQueueID = filterQueueID;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.core.filter.IFilterQueue#setFilters(java.util.List)
	 */
	public void setFilters(List<IFilterContext> filters) {
		filterList = filters;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.core.filter.IFilterQueue#getFilters()
	 */
	public List<IFilterContext> getFilters() {
		if (this.filterList==null) return Collections.emptyList();
		return this.filterList;
	}
	
	protected void fireDequeuedEvent(Cmd cmd) {
		this.eventService.postEvent(CommandEvent.createEvent(this.filterQueueID, CommandEvent.TOPIC_ENQUEUED, cmd));
	}
	
	protected void fireDestroyedEvent(Cmd cmd) {
		this.eventService.sendEvent(CommandEvent.createEvent(this.filterQueueID, CommandEvent.TOPIC_DESTROYED, cmd));
	}
	
	protected void firePreFilterEvent(Cmd cmd, IFilterContext filterContext) {
		this.eventService.postEvent(CommandFilterEvent.createEvent(this.filterQueueID, CommandFilterEvent.TOPIC_PRE_FILTER, cmd, filterContext));
	}
	
	protected void firePostFilterEvent(Cmd cmd, IFilterContext filterContext) {
		this.eventService.postEvent(CommandFilterEvent.createEvent(this.filterQueueID, CommandFilterEvent.TOPIC_POST_FILTER, cmd, filterContext));
	}
	
	@Override
	public void enqueue(Cmd command) throws InterruptedException {
		if (command == null) throw new NullPointerException("The command object was null!");
		
		// fire a event
		if (this.eventService != null) {
			fireDequeuedEvent(command);
		} 	
		
		this.filter(command);
		
		switch (command.getResult()) {
			case Passed:  super.enqueue(command); break;
			case Failure:			
			case Rejected: 
			default: 
				// fire a command-destruction event (this _must_ be send synchronous)
				if (this.eventService != null) {
					fireDestroyedEvent(command);
				} 
				
				// signal blocking of message
				return;
		}
	}
	
	protected void filter(final Cmd command) {
		if (this.filterList == null) return;
		
		// post-process the command through filters
		for (IFilterContext filterContext : this.filterList) {
			IFilter<Cmd> filter = null;
			try {				
				// fire a event
				if (this.eventService != null) {
					firePreFilterEvent(command, filterContext);
				} 	
				
				// getting the filter
				filter = (IFilter<Cmd>)filterContext.getFilter();
				
				if (this.logger.isTraceEnabled()) {
					this.logger.trace(String.format(
						"[%s] Passing '%s' to filter '%s' ...",
						this.filterQueueID,
						command,
						(filter == null) ? "null" : filter.getClass().getName()
					));
				}
				
				long start = System.currentTimeMillis();
				
				// process the command by the next filter
				filter.filter(command, filterContext);
				
				if (this.logger.isDebugEnabled()) {
					this.logger.debug(String.format(
						"[%s] Filter '%s' took %d ms processing '%s'.",
						this.filterQueueID,
						filter.getClass().getSimpleName(),
						Long.valueOf(System.currentTimeMillis() - start),
						command
					));
				}
				
				if (command.getResult() == ICommand.Result.Rejected) {
					this.logger.info(String.format(
							"[%s] '%s' rejected by filter '%s'. Reason: %s",
							this.filterQueueID,
							command, 
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
						command
				),e);				
			} finally {
				// fire a event
				if (this.eventService != null) {
					firePostFilterEvent(command, filterContext);
				} 
			}
		}
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
