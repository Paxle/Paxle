package org.paxle.core.queue.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterable;
import org.paxle.core.filter.impl.FilterContext;
import org.paxle.core.queue.Command;
import org.paxle.core.queue.ICommand;

public class FilterInputQueueTest extends MockObjectTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	private IFilterContext createDummyFilterContext(IFilter<? extends IFilterable> filter) {
		return new FilterContext(
				Long.toString(System.currentTimeMillis()),
				Long.valueOf(System.currentTimeMillis()),
				filter,
				"test",
				0,
				true,
				null
		);
	}

	@SuppressWarnings("unchecked")
	public void testDequeueRejectedCommand() throws Exception {
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
		CommandFilterInputQueue<ICommand> queue = new CommandFilterInputQueue<ICommand>(8);
		queue.setFilters(new ArrayList<IFilterContext>(Arrays.asList(filtercontext)));
		queue.setFilterQueueID("FilterQUEUE-ID");
		queue.setEventService(eventService);
		
		// enqueue a command
		queue.putData(command);
		
		// dequeue a command
		ICommand cmd = queue.dequeue();
		assertNull(cmd);
	}
	
	@SuppressWarnings("unchecked")
	public void testDequeuePassedCommand() throws InterruptedException {
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
		CommandFilterInputQueue<ICommand> queue = new CommandFilterInputQueue<ICommand>(8);
		queue.setFilters(new ArrayList<IFilterContext>(Arrays.asList(filtercontext)));
		queue.setFilterQueueID("FilterQUEUE-ID");
		queue.setEventService(eventService);		
		
		// enqueue a command
		queue.putData(command);
		
		// dequeue a command
		ICommand cmd = queue.dequeue();
		assertNotNull(cmd);
		assertSame(command, cmd);
	}
	
	public void testWaitForNext() throws InterruptedException {
		final ICommand command = new Command();
		command.setLocation(URI.create("http://testxyz.de"));
		
		final CommandFilterInputQueue<ICommand> queue = new CommandFilterInputQueue<ICommand>(8);
		
		// start a thread to wait for a next message in the queue
		final Thread queueFetcher = new Thread() {
			@Override
			public void run() {
				try {
					queue.waitForNext();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		queueFetcher.start();
		
		// enqueue a new command
		queue.putData(command);
		
		// wait for the queueFetcher to get notified
		queueFetcher.join(500);
		assertTrue(!queueFetcher.isAlive());
		
		// dequeue the command
		ICommand cmd = queue.dequeue();
		assertNotNull(cmd);
		assertSame(command, cmd);
	}
}
