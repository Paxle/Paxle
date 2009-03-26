/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.filter.CommandFilterEvent;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.queue.CommandEvent;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandFilteringContext;
import org.paxle.core.queue.ICommand.Result;

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
	
	private Cmd preFilteredCommand;
	
	/**
	 * The command-location of the command to process before any filtering was applied
	 */
	private URI commandLocation;
	
	/**
	 * Overall status of command-filtering.
	 */
	private boolean finished = false;
		
	/**
	 * This constructor is called by output-queues where there is no pre-defined command. 
	 * Instead the user of this context has to provide the command to filter via a call
	 * to function {@link #enqueue(ICommand)}
	 * 
	 * @param eventService the OSGi eventAdmin service to send command-filtering events
	 * @param filterQueue the output-queue where the already filtered command should be 
	 * 		  inserted if the command was not blocked by one of the filters
	 * 
	 * @see #enqueue(ICommand)
	 */
	public CommandFilteringContext(EventAdmin eventService, IFilterQueue filterQueue) {
		this.eventService = eventService;
		this.filterQueue = filterQueue;
	}	
	
	/**
	 * This constructor is called by input-queues where the is an already known command
	 * that should be filtered by command-filtering. The filtering process is started by
	 * the user of this context via a call to function {@link #dequeue()}.
	 * 
	 * @param eventService the OSGi eventAdmin service to send command-filtering events
	 * @param filterQueue the input-queue where the command to filter was already fetched from
	 * @param command the command to filter
	 * 
	 * @see #dequeue()
	 */
	public CommandFilteringContext(EventAdmin eventService, IFilterQueue filterQueue, Cmd command) {
		this.eventService = eventService;
		this.filterQueue = filterQueue;
		this.command = command;
		this.commandLocation = command.getLocation();
		this.preFilteredCommand = this.createCmdWrapper(this.command);
	}
	
	/**
	 * @return the filtered {@link ICommand} or <code>null</code> if the {@link ICommand} 
	 * was {@link ICommand.Result#Rejected rejected} by one of the {@link IFilter filters}
	 * that were applied to the {@link IFilterQueue}
	 */
	public Cmd dequeue() {
		if (this.command == null) throw new IllegalStateException("The command object is null");
		try {				
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
		} finally {
			this.finished = true;
			this.preFilteredCommand = null;
		}
	}
	
	public void preDequeue(Cmd command) {
		// nothing todo here
	}
		
	public Cmd postDequeuing(Cmd command) {
		throw new RuntimeException("not implemented");
	}	
	
	public void enqueue(Cmd command) throws InterruptedException {
		try {
			if (command == null) throw new NullPointerException("The command object is null");
			this.commandLocation = command.getLocation();
			this.preFilteredCommand = this.createCmdWrapper(command);
			
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
		} finally {
			this.finished = true;
			this.preFilteredCommand = null;
		}
	}
	
	public void preEnqueue(Cmd command) {
		// nothing todo here
	}

	public void postEnqueuing(Cmd command) throws InterruptedException {
		throw new RuntimeException("not implemented");
	}
	
	/**
	 * The {@link ICommand#getLocation() location} of the {@link ICommand}
	 */
	public URI getLocation() {
		return this.commandLocation;
	}
	
	public Cmd getPreFilteredCmd() {
		return this.preFilteredCommand;
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
	
	protected void firePostFilterEvent(Cmd cmd, IFilterContext filterContext, Throwable error) {
		if (this.eventService == null) return;
		
		// creating an osgi event
		Event e = CommandFilterEvent.createEvent(
				this.filterQueue.getFilterQueueID(), 
				CommandFilterEvent.TOPIC_POST_FILTER, 
				cmd, 
				filterContext,
				error
		);
		
		// post event
		this.eventService.postEvent(e);
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
			Throwable error = null;
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
				Result preFilterResult = command.getResult();
				filter.filter(command, filterContext);
				Result postFilterResult = command.getResult();
				
				if (this.logger.isDebugEnabled()) {
					this.logger.debug(String.format(
						"[%s] Filter '%s' took %d ms processing '%s'.",
						filterQueueID,
						filter.getClass().getSimpleName(),
						Long.valueOf(System.currentTimeMillis() - start),
						command
					));
				}
				
				if (preFilterResult != Result.Rejected && postFilterResult == ICommand.Result.Rejected) {
					this.logger.info(String.format(
							"[%s] '%s' rejected by filter '%s'. Reason: %s",
							filterQueueID,
							command, 
							filter.getClass().getName(), 
							command.getResultText()
					));
				}
			} catch (Throwable e) {
				error = e; // remember error
				this.logger.error(String.format(
						"[%s] Filter '%s' throwed an '%s' while processing '%s'.",
						filterQueueID,
						(filter == null) ? "null" : filter.getClass().getName(),
						e.getClass().getName(),
						command
				),e);				
			} finally {
				// fire a event
				this.firePostFilterEvent(command, filterContext, error); 
			}
		}
	}
	
	public boolean done() {
		return this.finished;
	}
	
	@SuppressWarnings("unchecked")
	private Cmd createCmdWrapper(Cmd cmd) {
		Class<?> i = null;
		Type[] types = cmd.getClass().getInterfaces();
		if (types != null) for (Type type : types) {
			if (ICommand.class.isAssignableFrom((Class<?>) type)) {
				i = (Class<?>) type;
			}
		}
		
		return (Cmd) Proxy.newProxyInstance(
				i.getClassLoader(),
                new Class[] { i },
                new CmdWrapper(cmd)
		);
	}
		
	private static class CmdWrapper <Cmd extends ICommand> implements InvocationHandler {
		@SuppressWarnings("serial")
		private static final HashSet<String> allowedMethods = new HashSet<String>(){{
			add("getProfileOID");
			add("getDepth");
			add("getLocation");
			add("toString");
		}};
		
		private final Cmd cmd;		
		
		public CmdWrapper(Cmd cmd) {
			this.cmd = cmd;
		}		
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (allowedMethods.contains(method.getName())) {
				return method.invoke(this.cmd, args);
			} else if (method.getName().equals("getCrawlerDocument")) {
				ICrawlerDocument cdoc = this.cmd.getCrawlerDocument();
				if (cdoc == null) return null;
				
				return (ICrawlerDocument) Proxy.newProxyInstance(
						ICrawlerDocument.class.getClassLoader(),
		                new Class[] { ICrawlerDocument.class },
		                new CrawlerDocWrapper(cdoc)
				);
			}
			
			throw new RuntimeException("Invalid method call");
		}
		
		@Override
		public String toString() {		
			return this.cmd.toString();
		}
	}
	
	/**
	 * A wrapper class around a {@link ICommand} instance. This wrapper is used
	 * to ensure that the caller of function {@link ICommandFilteringContext#getPreFilteredCmd()}
	 * is not able to modify the state of a pre-filtered {@link ICommand}.
	 * 
	 * Currently only a view method calls are allowed.
	 */
	private static class CrawlerDocWrapper implements InvocationHandler {
		@SuppressWarnings("serial")
		private static final HashSet<String> allowedMethods = new HashSet<String>(){{
			add("getMimeType");
			add("getSize");
			add("toString");
		}};		
		
		private final ICrawlerDocument cDoc;
		
		public CrawlerDocWrapper(ICrawlerDocument cDoc) {
			this.cDoc = cDoc;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (allowedMethods.contains(method.getName())) {
				return method.invoke(this.cDoc, args);
			}
			
			throw new RuntimeException("Invalid method call");
		}
		
		@Override
		public String toString() {
			return this.cDoc.toString();
		}		
	}	
}
