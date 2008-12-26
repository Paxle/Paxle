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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.IInputQueue;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.PPM;


public class Master<Data> extends Thread implements IMaster {
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A pool of {@link IWorker worker-threads}
	 */
	protected IPool<Data> pool = null;
	
	/**
	 * An input-queue containing jobs to process 
	 */
	protected IInputQueue<Data> inQueue = null;
	
	/**
	 * indicates if this thread was terminated
	 * @see #terminate()
	 */
	protected boolean stopped = false;
	
	/**
	 * indicates if this thread was paused
	 * @see #pauseMaster()
	 * @see #resumeMaster()
	 * @see #isPaused()
	 */
	protected boolean paused = false;
	
	/**
	 * total number of processed jobs since startup. 
	 */
	protected int processedCount = 0;
	
	/**
	 * timestamp when the master-thread was started
	 */
	protected long startTime = System.currentTimeMillis();
	
	/**
	 * Record statistics for PPM calculation 
	 * @see #getPPM()
	 */
	private PPM ppm = new PPM();
	
	/**
	 * set this to <code>>0</code> to delay
	 * the master thread between busy loops.
	 */
	private int delay = -1;
	
	/**
	 * Specifies whether the workers will be triggerd to fetch
	 * the next command on their own or get the already dequeued
	 * command by the master.
	 */
	private boolean triggerMode = true;
	
	/**
	 * @param threadPool the thread pool containing {@link IWorker worker-threads}
	 * @param commandQueue the queue containing {@link ICommand commands} to process
	 */
	public Master(IPool<Data> threadPool, IInputQueue<Data> commandQueue) {		
		this(threadPool, commandQueue, true);
	}
	
	public Master(IPool<Data> threadPool, IInputQueue<Data> commandQueue, boolean useTrigger) {		
		this.triggerMode = useTrigger;
		this.pool = threadPool;
		this.inQueue = commandQueue;
		this.setName("Master");
		this.start();		
	}
	
	@Override
	public void run() {
        while (!this.stopped && !Thread.interrupted()) {
        	Data command = null;
            try {
            	// check if the master was paused
            	synchronized (this) {
					if (this.paused) this.wait();
				}
            	            	
            	if (!this.triggerMode) {
            		// getting a new command from the queue
            		command = this.inQueue.dequeue();
            	} else {
            		// just wait for the next command
            		this.inQueue.waitForNext();
            	}

                // getting a free worker from pool
                IWorker<Data> worker = this.pool.getWorker();

                if (!this.triggerMode) {
                	// assign the command to the worker
                	worker.assign(command);
                	command = null;
                } else {
                	// force the worker to fetch the next command
                	worker.trigger();
                }
                this.processedCount++;
                
                //add the job to the total job-count and the PPM
                this.ppm.trick();
                
                // delay
                if (this.delay > 0) {
                	Thread.sleep(this.delay);
                }
            } catch (InterruptedException e) {
            	this.logger.debug("Master thread interrupted from outside.");
                Thread.interrupted();
                this.stopped = true;
            } catch (Throwable e) {
            	this.logger.error(String.format("Unexpected '%s' while processing command '%s'."
            			,e.getClass().getName()
            			,command
            	),e);
            }
        }

        // consuming the "is interrupted"-flag
        this.isInterrupted();

        // closing the pool
        try {
        	this.logger.debug("Terminating worker-threads ...");
            this.pool.close();
            this.logger.debug("Worker-threads terminated.");
        } catch (Throwable e) {
        	this.logger.error(String.format("Unexpected '%s' while terminating worker-threads."
        			,e.getClass().getName()
        	),e);
        }
	}
	
	/**
	 * @see IMaster#terminate()
	 */
	public void terminate() {
		this.stopped = true;
		this.interrupt();
		try {
			this.join(15000);
		} catch (InterruptedException e) {
			/* ignore this */
		}		
	}

	/**
	 * @see IMaster#pauseMaster()
	 */
	public synchronized void pauseMaster() {
		this.paused = true;
	}
	
	/**
	 * @see IMaster#resumeMaster()
	 */
	public synchronized void resumeMaster() {
		this.paused = false;
		this.notifyAll();
	}
	
	/**
	 * @see IMaster#processNext()
	 */
	public synchronized void processNext() {
		if (this.paused) this.notifyAll();
	}
	
	/**
	 * @see IMaster#isPaused()
	 */
	public boolean isPaused() {
		return this.paused;
	}
	
	/**
	 * @see IMaster#getPPM()
	 */
	public int getPPM() {
		return this.ppm.getPPM();
	}

	/**
	 * @see IMaster#setDelay(int)
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getDelay() {
		return this.delay;
	}
	
	/**
	 * @see IMaster#processedCount()
	 */
	public int processedCount() {
		return this.processedCount;
	}
}
