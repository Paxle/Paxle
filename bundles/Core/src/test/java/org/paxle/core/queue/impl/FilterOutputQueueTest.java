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
import java.util.ArrayList;
import java.util.Arrays;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.Command;
import org.paxle.core.queue.ICommand;

public class FilterOutputQueueTest extends AFilterQueueTest {

	@SuppressWarnings("unchecked")
	public void testEnqueueRejectedCommand() throws Exception {
		final ICommand command = new Command();
		command.setLocation(URI.create("http://testxyz.de"));
		
		final IFilter<ICommand> filter = mock(IFilter.class);
		final EventAdmin eventService = mock(EventAdmin.class);
		final IFilterContext filtercontext = this.createDummyFilterContext(filter);

		// define expectations
		checking(new Expectations(){{
			
			// filtering should be called exactly once
			one(filter).filter(with(same(command)), with(same(filtercontext))); will(new Action(){
				public void describeTo(Description arg0) {}

				public Object invoke(Invocation invocation) throws Throwable {
					((ICommand)invocation.getParameter(0)).setResult(ICommand.Result.Rejected, "Rejected by Test");
					return null;
				}
				
			});
			
			// event service must be called 3 times
			// once for the rejected cmd
			exactly(3).of(eventService).postEvent(with(any(Event.class)));
			one(eventService).sendEvent(with(any(Event.class)));
		}});		

		// init queue and set filters
		CommandFilterOutputQueue<ICommand> queue = new CommandFilterOutputQueue<ICommand>(8);
		queue.setFilters(new ArrayList<IFilterContext>(Arrays.asList(filtercontext)));
		queue.setFilterQueueID("FilterQUEUE-ID");
		queue.setEventService(eventService);
		
		// enqueue a command
		queue.enqueue(command);
		
		// the queue must be empty
		assertEquals(0, queue.size());
	}
	
	@SuppressWarnings("unchecked")
	public void testEnqueuePassedCommand() throws InterruptedException {
		final ICommand command = new Command();
		command.setLocation(URI.create("http://testxyz.de"));
		
		final IFilter<ICommand> filter = mock(IFilter.class);
		final EventAdmin eventService = mock(EventAdmin.class);
		final IFilterContext filtercontext = this.createDummyFilterContext(filter);

		// define expectations
		checking(new Expectations(){{
			// filtering should be called exactly once
			one(filter).filter(with(same(command)), with(same(filtercontext)));
			
			// event service must be called 3 times
			// once for the rejected cmd
			exactly(3).of(eventService).postEvent(with(any(Event.class)));			
		}});		

		// init queue and set filters
		CommandFilterOutputQueue<ICommand> queue = new CommandFilterOutputQueue<ICommand>(8);
		queue.setFilters(new ArrayList<IFilterContext>(Arrays.asList(filtercontext)));
		queue.setFilterQueueID("FilterQUEUE-ID");
		queue.setEventService(eventService);
		
		// enqueue a command
		queue.enqueue(command);
		
		// dequeue a command
		assertEquals(1, queue.size());
		ICommand cmd = queue.getData();
		assertEquals(0, queue.size());
		
		assertNotNull(cmd);
		assertSame(command, cmd);
		assertEquals(ICommand.Result.Passed, cmd.getResult());
	}
}
