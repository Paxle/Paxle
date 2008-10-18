
package org.paxle.core.queue.impl;

import org.paxle.core.filter.CommandFilterEvent;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.queue.CommandEvent;
import org.paxle.core.queue.FilterOutputQueue;
import org.paxle.core.queue.ICommand;

public class CommandFilterOutputQueue<Cmd extends ICommand> extends FilterOutputQueue<Cmd> implements IFilterQueue {
	
	private static final long serialVersionUID = 1L;
	
	public CommandFilterOutputQueue(int length) {
		super(length);
	}
	
	@Override
	protected void fireDequeuedEvent(Cmd cmd) {
		this.eventService.postEvent(CommandEvent.createEvent(this.filterQueueID, CommandEvent.TOPIC_ENQUEUED, cmd));
	}
	
	@Override
	protected void fireDestroyedEvent(Cmd cmd) {
		this.eventService.sendEvent(CommandEvent.createEvent(this.filterQueueID, CommandEvent.TOPIC_DESTROYED, cmd));
	}
	
	@Override
	protected void firePreFilterEvent(Cmd cmd, IFilterContext filterContext) {
		this.eventService.postEvent(CommandFilterEvent.createEvent(this.filterQueueID, CommandFilterEvent.TOPIC_PRE_FILTER, cmd, filterContext));
	}
	
	@Override
	protected void firePostFilterEvent(Cmd cmd, IFilterContext filterContext) {
		this.eventService.postEvent(CommandFilterEvent.createEvent(this.filterQueueID, CommandFilterEvent.TOPIC_POST_FILTER, cmd, filterContext));
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
