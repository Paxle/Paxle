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

package org.paxle.core.threading.impl;

import java.net.URI;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.impl.BasicCommand;
import org.paxle.core.queue.IOutputQueue;
import org.paxle.core.queue.InputQueue;
import org.paxle.core.queue.OutputQueue;
import org.paxle.core.queue.impl.CommandFilterInputQueue;
import org.paxle.core.threading.AWorker;
import org.paxle.core.threading.IPool;

public class MasterTest extends MockObjectTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	@SuppressWarnings("unchecked")
	public void testTriggerWorker() throws Exception {
		// creating a test command
		final ICommand command = new BasicCommand();
		command.setLocation(URI.create("http://test.xyz"));
		
		final IPool<ICommand> pool = mock(IPool.class);
		final CommandFilterInputQueue<ICommand> inQueue = new CommandFilterInputQueue<ICommand>(8);
		final IOutputQueue<ICommand> outQueue = mock(IOutputQueue.class);
		final Semaphore enqueuedSync = new Semaphore(0);
		
		// creating a dummy worker
		final DummyTriggeredWorker worker = new DummyTriggeredWorker();
		worker.setInQueue(inQueue);
		worker.setOutQueue(outQueue);
		
		// define expectations
		checking(new Expectations(){{
			// allow the master to fetch a worker 
			one(pool).getWorker(); will(returnValue(worker));
			one(pool).close();
			
			// allow the worker to enqueue the processed command into the out-queue
			one(outQueue).enqueue(with(same(command)));
			will(new Action(){
				public void describeTo(Description arg0) {}
				public Object invoke(Invocation invocation) throws Throwable {
					enqueuedSync.release();
					return null;
				}				
			});
		}});
		
		// init and start master
		final Master master = new Master<ICommand>(pool, inQueue, true);
		
		// enqueue a command
		inQueue.putData(command);
		
		// wait until the worker was triggered
		assertTrue(worker.triggerSync.tryAcquire(5000, TimeUnit.SECONDS));
		
		// wait until the worker has enqueued the processed command
		assertTrue(enqueuedSync.tryAcquire(5000, TimeUnit.SECONDS));
		
		// terminate master
		master.terminate();
	}
		
	public void testProcess() throws Exception {
		// creating a test command
		final ICommand command = new BasicCommand();
		command.setLocation(URI.create("http://test.xyz"));
		
		// a dummy worker pool
		@SuppressWarnings("unchecked")
		final IPool<ICommand> pool = mock(IPool.class);
		
		// creating a dummy worker
		final DummyTriggeredWorker worker = new DummyTriggeredWorker();
		
		// define expectations
		checking(new Expectations(){{
			// allow the master to fetch a worker 
			one(pool).getWorker(false); will(returnValue(worker));
			one(pool).close();
		}});
		
		// init and start master
		final Master<ICommand> master = new Master<ICommand>(pool, new CommandFilterInputQueue<ICommand>(1), true);
		master.process(command);
			
		// terminate master
		master.terminate();
		assertEquals(DummyTriggeredWorker.PROCESSING_DONE, command.getResultText());
	}
	
	public void testProcessEnqueued() throws Exception {
		// creating a test command
		final ICommand command = new BasicCommand();
		command.setLocation(URI.create("http://test.xyz"));
		
		// a dummy worker pool
		@SuppressWarnings("unchecked")
		final IPool<ICommand> pool = mock(IPool.class);
		final InputQueue<ICommand> inputQueue = new InputQueue<ICommand>(1);
		final OutputQueue<ICommand> outputQueue = new OutputQueue<ICommand>(1);
		
		// creating a dummy worker
		final DummyTriggeredWorker worker = new DummyTriggeredWorker();
		
		// define expectations
		checking(new Expectations(){{
			// allow the master to fetch a worker 
			one(pool).getWorker(false); will(returnValue(worker));
			one(pool).close();
		}});
		
		// init and start master
		final Master<ICommand> master = new Master<ICommand>(pool, new CommandFilterInputQueue<ICommand>(1), true);
		inputQueue.putData(command);
		master.process(inputQueue, outputQueue, false);

		// wait until the worker was triggered
		assertTrue(worker.executeSync.tryAcquire(5000, TimeUnit.SECONDS));		
		
		// terminate master
		master.terminate();
		assertEquals(DummyTriggeredWorker.PROCESSING_DONE, command.getResultText());
		assertSame(command, outputQueue.getData());
	}
}

class DummyTriggeredWorker extends AWorker<ICommand> {
	public static final String PROCESSING_DONE = "processing done";
	public Semaphore triggerSync = new Semaphore(0);
	public Semaphore executeSync = new Semaphore(0);
		
	@Override
	public void assign(ICommand cmd) {
		throw new RuntimeException("This function must not be called in this testcase.");
	}
	
	@Override
	public void trigger() throws InterruptedException {
		super.trigger();
		this.triggerSync.release();
	}

	@Override
	protected void execute(ICommand cmd) {
		cmd.setResultText(PROCESSING_DONE);
		this.executeSync.release();
	}	
}