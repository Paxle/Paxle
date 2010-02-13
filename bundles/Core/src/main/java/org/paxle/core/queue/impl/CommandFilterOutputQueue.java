/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
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

import org.paxle.core.doc.CommandEvent;
import org.paxle.core.doc.ICommand;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.impl.MWComponentFactory;
import org.paxle.core.queue.ICommandFilterQueue;
import org.paxle.core.queue.ICommandFilteringContext;
import org.paxle.core.queue.OutputQueue;

public class CommandFilterOutputQueue<Cmd extends ICommand> extends OutputQueue<Cmd> implements ICommandFilterQueue<Cmd> {
	
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
	
	/**
	 * @see org.paxle.core.filter.IFilterQueue#setFilterQueueID(java.lang.String)
	 */
	public void setFilterQueueID(String filterQueueID) {
		if (this.filterQueueID != null) throw new IllegalStateException("The filter-queue was already set.");
		this.filterQueueID = filterQueueID;
	}
	
	public String getFilterQueueID() {
		return this.filterQueueID;
	}	
	
	/**
	 * @see org.paxle.core.filter.IFilterQueue#setFilters(java.util.List)
	 */
	public void setFilters(List<IFilterContext> filters) {
		filterList = filters;
	}
	
	/**
	 * @see org.paxle.core.filter.IFilterQueue#getFilters()
	 */
	public List<IFilterContext> getFilters() {
		if (this.filterList==null) return Collections.emptyList();
		return this.filterList;
	}
	
	protected void fireEnqueuedEvent(Cmd cmd) {
		if (this.eventService == null) return;
		this.eventService.postEvent(CommandEvent.createEvent(this.filterQueueID, CommandEvent.TOPIC_ENQUEUED, cmd));
	}

	@Override
	public void enqueue(Cmd command) throws InterruptedException {
		// dequeuing command and creating filtering-context
		ICommandFilteringContext<Cmd> cmdFiltering = this.getFilteringContext();
		
		// filter command
		cmdFiltering.enqueue(command);		
	}	

	public ICommandFilteringContext<Cmd> getFilteringContext() {
		// creating the filtering-context
		return new CmdFilterContext();
	}
	
	private void enqueueInternal(Cmd command) throws InterruptedException{
		// enqueue into internal queue
		super.enqueue(command);
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
		public CmdFilterContext() {
			super(eventService, CommandFilterOutputQueue.this);
		}
		
		@Override
		public void preEnqueue(Cmd command) {			
			// fire cmd-enqueued event
			fireEnqueuedEvent(command);
		}
		
		@Override
		public void postEnqueuing(Cmd command) throws InterruptedException {		
			enqueueInternal(command);
		}
	}
}
