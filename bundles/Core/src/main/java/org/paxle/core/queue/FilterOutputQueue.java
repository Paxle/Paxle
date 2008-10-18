
package org.paxle.core.queue;

import java.util.Collections;
import java.util.List;

import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.filter.IFilterable;

public class FilterOutputQueue<F extends IFilterable> extends OutputQueue<F> implements IFilterQueue {
	
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
	
	public FilterOutputQueue(int length) {
		super(length);
	}
	
	public FilterOutputQueue(int length, boolean limited) {
		super(length, limited);
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
	
	@SuppressWarnings("unused")
	protected void fireDequeuedEvent(final F cmd) {
	}
	
	@SuppressWarnings("unused")
	protected void fireDestroyedEvent(final F cmd) {
	}
	
	@SuppressWarnings("unused")
	protected void firePreFilterEvent(final F cmd, final IFilterContext filterContext) {
	}
	
	@SuppressWarnings("unused")
	protected void firePostFilterEvent(final F cmd, final IFilterContext filterContext) {
	}
	
	@Override
	public void enqueue(F command) throws InterruptedException {
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
	
	protected void filter(final F command) {
		if (this.filterList == null) return;
		
		// post-process the command through filters
		for (IFilterContext filterContext : this.filterList) {
			IFilter<F> filter = null;
			try {				
				// fire a event
				if (this.eventService != null) {
					firePreFilterEvent(command, filterContext);
				} 	
				
				// getting the filter
				filter = (IFilter<F>)filterContext.getFilter();
				
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
}
