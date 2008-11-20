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

import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.EventAdmin;
import org.paxle.core.filter.CommandFilterEvent;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.queue.CommandEvent;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandFilteringContext;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

abstract class CommandFilteringContext<Cmd extends ICommand> implements ICommandFilteringContext<Cmd> {
	
	/**
	 * Component to send {@link org.osgi.service.event.Event events}
	 */
	private final  EventAdmin eventService;

	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * The {@link IFilterQueue queue} this context object belongs to
	 */
	private final IFilterQueue filterQueue;
	
	/**
	 * The {@link ICommand} that was dequeued from or enqueued into the {@link IFilterQueue} 
	 * and should be filtered via a call to {@link #filterCommand()}
	 */
	private Cmd command;
		
	public CommandFilteringContext(EventAdmin eventService, IFilterQueue filterQueue) {
		this(eventService, filterQueue, null);
	}	
	
	public CommandFilteringContext(EventAdmin eventService, IFilterQueue filterQueue, Cmd command) {
		this.eventService = eventService;
		this.filterQueue = filterQueue;
		this.command = command;
	}
	
	/**
	 * @return the filtered {@link ICommand} or <code>null</code> if the {@link ICommand} 
	 * was {@link ICommand.Result#Rejected rejected} by one of the {@link IFilter filters}
	 * that were applied to the {@link IFilterQueue}
	 */
	public Cmd dequeue() {
		if (this.command == null) throw new IllegalStateException("The command object is null");
		
		this.preDequeue(command);
		
		// filtering
		this.filter(this.command);		
		
		// only return "passed" commands
		switch (this.command.getResult()) {
			case Passed:  return this.postDequeuing(this.command);
			case Failure:
			case Rejected: 
			default: 
				// fire a command-destruction event (this _must_ be send synchronous)
				this.fireDestroyedEvent(this.command);
			
				// signal blocking of message
			return null;
		}
	}
	
	public void preDequeue(Cmd command) {
		// nothing todo here
	}
		
	public Cmd postDequeuing(Cmd command) {
		throw new NotImplementedException();
	}	
	
	public void enqueue(Cmd command) throws InterruptedException {
		if (command == null) throw new NullPointerException("The command object is null");
		
		this.preEnqueue(command);
		
		// filtering
		this.filter(command);
		
		switch (command.getResult()) {
			case Passed: this.postEnqueuing(command); break;
			case Failure:			
			case Rejected: 
			default: 
				// fire a command-destruction event (this _must_ be send synchronous)
				this.fireDestroyedEvent(command);
				
				// signal blocking of message
				return;
		}
	}
	
	public void preEnqueue(Cmd command) {
		// nothing todo here
	}

	public void postEnqueuing(Cmd command) throws InterruptedException {
		throw new NotImplementedException();
	}
	
	/**
	 * The {@link ICommand#getLocation() location} of the {@link ICommand}
	 */
	public URI getLocation() {
		return this.command.getLocation();
	}
	
	protected void firePreFilterEvent(Cmd cmd, IFilterContext filterContext) {
		if (this.eventService == null) return;
		
		this.eventService.postEvent(CommandFilterEvent.createEvent(
				this.filterQueue.getFilterQueueID(), 
				CommandFilterEvent.TOPIC_PRE_FILTER, 
				cmd, 
				filterContext
		));
	}	
	
	protected void firePostFilterEvent(Cmd cmd, IFilterContext filterContext) {
		if (this.eventService == null) return;
		
		this.eventService.postEvent(CommandFilterEvent.createEvent(
				this.filterQueue.getFilterQueueID(), 
				CommandFilterEvent.TOPIC_POST_FILTER, 
				cmd, 
				filterContext
		));
	}	
	
	protected void fireDestroyedEvent(Cmd cmd) {
		if (this.eventService == null) return;
		
		this.eventService.sendEvent(CommandEvent.createEvent(
				this.filterQueue.getFilterQueueID(), 
				CommandEvent.TOPIC_DESTROYED, 
				cmd
		));
	}	
		
	protected void filter(final Cmd command) {
		final String filterQueueID = this.filterQueue.getFilterQueueID();
		final List<IFilterContext> filterList = this.filterQueue.getFilters();
		if (filterList == null) return;
		
		// post-process the command through filters
		for (IFilterContext filterContext : filterList) {
			IFilter<Cmd> filter = null;
			try {				
				// fire a event
				this.firePreFilterEvent(command, filterContext); 	
				
				// getting the filter				
				filter = (IFilter<Cmd>) filterContext.getFilter();
				
				if (this.logger.isTraceEnabled()) {
					this.logger.trace(String.format(
						"[%s] Passing '%s' to filter '%s' ...",
						filterQueueID,
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
						filterQueueID,
						filter.getClass().getSimpleName(),
						Long.valueOf(System.currentTimeMillis() - start),
						command
					));
				}
				
				if (command.getResult() == ICommand.Result.Rejected) {
					this.logger.info(String.format(
							"[%s] '%s' rejected by filter '%s'. Reason: %s",
							filterQueueID,
							command, 
							filter.getClass().getName(), 
							command.getResultText()
					));
				}
			} catch (Throwable e) {
				this.logger.error(String.format(
						"[%s] Filter '%s' throwed an '%s' while processing '%s'.",
						filterQueueID,
						(filter == null) ? "null" : filter.getClass().getName(),
						e.getClass().getName(),
						command
				),e);				
			} finally {
				// fire a event
				this.firePostFilterEvent(command, filterContext); 
			}
		}
	}	
}
