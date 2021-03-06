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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.impl.BasicCommand;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;

public class FilterInputQueueTest extends AFilterQueueTest {

	@SuppressWarnings("unchecked")
	public void testDequeueRejectedCommand() throws Exception {
		final ICommand command = new BasicCommand();
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
			exactly(3).of(eventService).postEvent(with(aNonNull(Event.class)));
			one(eventService).sendEvent(with(aNonNull(Event.class)));
		}});		

		// init queue and set filters
		CommandFilterInputQueue<ICommand> queue = new CommandFilterInputQueue<ICommand>(8);
		queue.setFilters(new ArrayList<IFilterContext>(Arrays.asList(filtercontext)));
		queue.setFilterQueueID("FilterQUEUE-ID");
		queue.setEventService(eventService);
		
		// enqueue a command
		queue.putData(command);
		
		// dequeue a command
		assertEquals(1, queue.size());
		ICommand cmd = queue.dequeue();
		assertEquals(0, queue.size());
		
		assertNull(cmd);
	}
	
	@SuppressWarnings("unchecked")
	public void testDequeuePassedCommand() throws InterruptedException {
		final ICommand command = new BasicCommand();
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
			exactly(3).of(eventService).postEvent(with(aNonNull(Event.class)));			
		}});		

		// init queue and set filters
		CommandFilterInputQueue<ICommand> queue = new CommandFilterInputQueue<ICommand>(8);
		queue.setFilters(new ArrayList<IFilterContext>(Arrays.asList(filtercontext)));
		queue.setFilterQueueID("FilterQUEUE-ID");
		queue.setEventService(eventService);		
		
		// enqueue a command
		queue.putData(command);
		
		// dequeue a command
		assertEquals(1, queue.size());
		ICommand cmd = queue.dequeue();
		assertEquals(0, queue.size());
		
		assertNotNull(cmd);
		assertSame(command, cmd);
		assertEquals(ICommand.Result.Passed, cmd.getResult());
	}
	
	/**
	 * Testing if a {@link BasicCommand} is filtered properly even if an {@link Exception} 
	 * is thrown during filtering.
	 *  
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	public void testDequeueWithException() throws InterruptedException {
		final ICommand command = new BasicCommand();
		command.setLocation(URI.create("http://testxyz.de"));
		
		final IFilter<ICommand> filter = mock(IFilter.class);
		final EventAdmin eventService = mock(EventAdmin.class);
		final IFilterContext filtercontext = this.createDummyFilterContext(filter);

		// define expectations
		checking(new Expectations(){{
			// filtering should be called exactly once
			one(filter).filter(with(same(command)), with(same(filtercontext)));
			will(throwException(new RuntimeException("Unexpected Exception")));
			
			// event service must be called 3 times
			// once for the rejected cmd
			exactly(3).of(eventService).postEvent(with(aNonNull(Event.class)));			
		}});		

		// init queue and set filters
		CommandFilterInputQueue<ICommand> queue = new CommandFilterInputQueue<ICommand>(8);
		queue.setFilters(new ArrayList<IFilterContext>(Arrays.asList(filtercontext)));
		queue.setFilterQueueID("FilterQUEUE-ID");
		queue.setEventService(eventService);		
		
		// enqueue a command
		queue.putData(command);
		
		// dequeue a command
		assertEquals(1, queue.size());
		ICommand cmd = queue.dequeue();
		assertEquals(0, queue.size());
		
		assertNotNull(cmd);
		assertSame(command, cmd);
		assertEquals(ICommand.Result.Passed, cmd.getResult());
	}
	
	public void testWaitForNext() throws InterruptedException {
		final ICommand command = new BasicCommand();
		command.setLocation(URI.create("http://testxyz.de"));
		
		final CommandFilterInputQueue<ICommand> queue = new CommandFilterInputQueue<ICommand>(8);
		
		// start a thread to wait for a next message in the queue
		final Semaphore s = new Semaphore(0);
		new Thread() {
			@Override
			public void run() {
				try {
					queue.waitForNext();
					s.release();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
		
		// enqueue a new command
		queue.putData(command);
		
		// check if waitForNext was called
		assertTrue(s.tryAcquire(3, TimeUnit.SECONDS));
		
		// dequeue the command
		assertEquals(1, queue.size());
		ICommand cmd = queue.dequeue();
		assertEquals(0, queue.size());
		
		assertNotNull(cmd);
		assertSame(command, cmd);
	}
}
