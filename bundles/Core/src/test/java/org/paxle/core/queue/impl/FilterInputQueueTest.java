package org.paxle.core.queue.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.impl.FilterContext;
import org.paxle.core.queue.ICommand;

public class FilterInputQueueTest extends MockObjectTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	@SuppressWarnings("unchecked")
	public void testDequeueRejectedCommand() throws InterruptedException {
		final ICommand command = mock(ICommand.class);
		final IFilter<ICommand> filter = mock(IFilter.class);
		final IFilterContext filtercontext = new FilterContext(
				Long.valueOf(System.currentTimeMillis()),
				filter,
				"test",
				0,
				null
		);

		// define expectations
		checking(new Expectations(){{
			/* 
			 * Command statis is "passed" before filtering
			 * and "rejected" afterwards.
			 */
			atLeast(1).of(command).getResult();
			will(onConsecutiveCalls(
				returnValue(ICommand.Result.Passed),
				returnValue(ICommand.Result.Rejected),
				returnValue(ICommand.Result.Rejected)
			));			
			
			allowing(command).getResultText(); 
			will(returnValue("Rejected by Test"));
			
			// filtering should be called exactly once
			one(filter).filter(with(same(command)), with(same(filtercontext)));
			
			// metadata about command
			allowing(command).getLocation(); will(returnValue("http://test.xyz"));
			ignoring(command);
		}});		

		// init queue and set filters
		FilterInputQueue<ICommand> queue = new FilterInputQueue<ICommand>(8);
		queue.setFilters(new ArrayList<IFilterContext>(Arrays.asList(filtercontext)));
		
		// enqueue a command
		queue.putData(command);
		
		// dequeue a command
		ICommand cmd = queue.dequeue();
		assertNull(cmd);
	}
	
	@SuppressWarnings("unchecked")
	public void testDequeuePassedCommand() throws InterruptedException {
		final ICommand command = mock(ICommand.class);
		final IFilter<ICommand> filter = mock(IFilter.class);
		final IFilterContext filtercontext = new FilterContext(
				Long.valueOf(System.currentTimeMillis()),
				filter,
				"test",
				0,
				null
		);

		// define expectations
		checking(new Expectations(){{
			/* 
			 * Command status is "passed"
			 */
			atLeast(1).of(command).getResult();
			will(returnValue(ICommand.Result.Passed));			
			
			allowing(command).getResultText(); 
			will(returnValue("OK"));
			
			// filtering should be called exactly once
			one(filter).filter(with(same(command)), with(same(filtercontext)));
			
			// metadata about command
			allowing(command).getLocation(); will(returnValue("http://test.xyz"));
			ignoring(command);
		}});		

		// init queue and set filters
		FilterInputQueue<ICommand> queue = new FilterInputQueue<ICommand>(8);
		queue.setFilters(new ArrayList<IFilterContext>(Arrays.asList(filtercontext)));
		
		// enqueue a command
		queue.putData(command);
		
		// dequeue a command
		ICommand cmd = queue.dequeue();
		assertNotNull(cmd);
		assertSame(command, cmd);
	}
	
	public void testWaitForNext() throws InterruptedException {
		final ICommand command = mock(ICommand.class);
		final FilterInputQueue<ICommand> queue = new FilterInputQueue<ICommand>(8);
		// define expectations
		checking(new Expectations(){{
			atLeast(1).of(command).getResult();
			will(returnValue(ICommand.Result.Passed));	
		}});
		
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
