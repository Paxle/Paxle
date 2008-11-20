/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.core.queue.impl;

import java.util.Collections;
import java.util.List;

import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.impl.MWComponentFactory;
import org.paxle.core.queue.CommandEvent;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandFilterQueue;
import org.paxle.core.queue.ICommandFilteringContext;
import org.paxle.core.queue.InputQueue;

public class CommandFilterInputQueue<Cmd extends ICommand> extends InputQueue<Cmd> implements ICommandFilterQueue<Cmd> {
	
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
	
	public CommandFilterInputQueue(int length) {
		super(length);
	}
	
	public CommandFilterInputQueue(final int length, final boolean limited) {
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
	
	public String getFilterQueueID() {
		return this.filterQueueID;
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
		if (this.eventService == null) return;
		this.eventService.postEvent(CommandEvent.createEvent(this.filterQueueID, CommandEvent.TOPIC_DEQUEUED, cmd));
	}
	
	/**
	 * @see org.paxle.core.queue.InputQueue#dequeue()
	 */
	@Override
	public Cmd dequeue() throws InterruptedException {
		// dequeuing command and creating filtering-context
		ICommandFilteringContext<Cmd> cmdFiltering = this.getFilteringContext();
		
		// filter command
		return cmdFiltering.dequeue();		
	}
	
	public ICommandFilteringContext<Cmd> getFilteringContext() throws InterruptedException {
		// get next command
		final Cmd command = super.dequeue();
		
		// fire a event
		this.fireDequeuedEvent(command);
		
		// creating filtering-context
		return new CmdFilterContext(command);
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
	
	/**
	 * Concreate implementation of a {@link CommandFilteringContext}
	 */
	private class CmdFilterContext extends CommandFilteringContext<Cmd> implements ICommandFilteringContext<Cmd> {
		public CmdFilterContext(Cmd command) {
			super(eventService, CommandFilterInputQueue.this, command);
		}
		
		@Override
		public Cmd postDequeuing(Cmd command) {
			// just return the dequeued and already filtered object
			return command;
		}
	}
}
