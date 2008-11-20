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

package org.paxle.core.threading.impl;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.IOutputQueue;
import org.paxle.core.queue.impl.CommandFilterInputQueue;
import org.paxle.core.threading.AWorker;
import org.paxle.core.threading.IPool;

public class MasterTest extends MockObjectTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	@SuppressWarnings("unchecked")
	public void testTriggerWorker() throws Exception {
		// init needed mocks
		final ICommand command = mock(ICommand.class);
		final IPool<ICommand> pool = mock(IPool.class);
		final CommandFilterInputQueue<ICommand> inQueue = new CommandFilterInputQueue<ICommand>(8);
		final IOutputQueue<ICommand> outQueue = mock(IOutputQueue.class);
		final DummyWorker worker = new DummyWorker(true);
		worker.setInQueue(inQueue);
		worker.setOutQueue(outQueue);
		
		// define expectations
		checking(new Expectations(){{
			// allow the master to fetch a worker 
			one(pool).getWorker(); will(returnValue(worker));
			one(pool).close();
			
			// allow the worker to enqueue the processed command into the out-queue
			allowing(command).getResult(); will(returnValue(ICommand.Result.Passed));
			one(outQueue).enqueue(with(same(command)));
		}});
		
		// init and start master
		final Master master = new Master<ICommand>(pool, inQueue, true);
		
		// enqueue a command
		inQueue.putData(command);
		
		// sleep
		Thread.sleep(200);
		
		// wait until the worker was triggered
		worker.wasTriggered();
		
		// terminate master
		master.terminate();
	}
}

class DummyWorker extends AWorker<ICommand> {
	private boolean triggerMode = true;
	
	private Object triggerSync = new Object();
	private boolean wasTriggered = false;
	
	public DummyWorker(boolean useTriggerMode) {
		this.triggerMode = useTriggerMode;
	}
	
	@Override
	public void assign(ICommand cmd) {
		if (this.triggerMode) {
			throw new RuntimeException("This function must not be called in this testcase.");
		} else {
			super.assign(cmd);
		}
	}
	
	public void wasTriggered() throws InterruptedException {
		synchronized (triggerSync) {
			if (!this.wasTriggered) triggerSync.wait(1000);
			if (!this.wasTriggered) throw new IllegalStateException("Worker was not triggered!");
		}
	}
	
	@Override
	public void trigger() throws InterruptedException {
		super.trigger();
		synchronized(triggerSync) {
			this.wasTriggered = true;
			triggerSync.notify();		
		}
	}

	@Override
	protected void execute(ICommand cmd) {
		// nothing to do here
	}
	
}