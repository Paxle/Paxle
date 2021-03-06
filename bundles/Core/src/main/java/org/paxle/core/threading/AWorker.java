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

package org.paxle.core.threading;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.ICommand;
import org.paxle.core.queue.ICommandFilterQueue;
import org.paxle.core.queue.ICommandFilteringContext;
import org.paxle.core.queue.IInputQueue;
import org.paxle.core.queue.IOutputQueue;

import edu.umd.cs.findbugs.annotations.OverrideMustInvoke;

/**
 * An abstract class of a {@link IWorker worker-thread}. Components such as
 * <ul>
 * 	<li>Core-Crawler</li>
 * 	<li>Core-Parser</li>
 * 	<li>Indexer</li>
 * </ul>
 * extend this class and provide a component-specific implementation of the
 * {@link #execute(ICommand)} method.
 * 
 * {@link ICommand commands} are assigned to this component by a {@link IMaster master}-thread.
 * 
 */
public abstract class AWorker<Data> extends Thread implements IWorker<Data> {

	private Log logger = LogFactory.getLog(this.getClass());
	
    /**
     * The crawler thread pool
     */
    private IPool<Data> myPool = null;
    
    /**
     * The output-queue where the modified command should
     * be enqueued.
     */
    private IOutputQueue<Data> outQueue = null;
    
    /**
     * The input-queue where incoming commands are fetched
     * from.
     */
    private IInputQueue<Data> inQueue = null;
        
    /**
     * The next {@link ICommand command} that must be processed by the worker  
     */
    private Data command = null;    
	
    /**
     * if <code>true</code> the worker was destroyed using {@link IPool#invalidateWorker(IWorker)}
     */
    public boolean destroyed = false;
    
    /**
     * if <code>true</code> the thread was already started with {@link Thread#start()}
     */
    protected boolean running = false;    
    
    /**
     * if <code>true</code> the thread was terminated with {@link IWorker#terminate()}
     */
    protected boolean stopped = false;
    
    /**
     * if <code>true</code> the execution of the {@link #command current-command} has finished
     */
    protected boolean done = false; 	
    
    /**
     * Setter methods to set the pool
     */
    public void setPool(IPool<Data> pool) {
    	this.myPool = pool;
    }
    
    /**
     * Setter method to set the output-queue
     */
    public void setOutQueue(IOutputQueue<Data> outQueue) {
    	this.outQueue = outQueue;
    }
    
    /**
     * @see IWorker#setInQueue(IInputQueue)
     */
    public void setInQueue(IInputQueue<Data> inQueue) {
    	this.inQueue = inQueue;
    }
    
    @Override
    public void run() {
        this.running = true;

        try {
        	if (this.outQueue == null) throw new IllegalArgumentException("Output-Queue was not set properly.");
        	
            // The thread keeps running.
        	while (!this.stopped && !this.destroyed && !this.isInterrupted()) {   
                if (this.done) {                      
                    if (this.myPool != null && !this.myPool.closed()) {
                        synchronized (this) { 
                            // return thread back into pool
                            this.myPool.returnWorker(this);

                            // We are waiting for a new task now.
                            if (!this.stopped && !this.destroyed && !this.isInterrupted()) { 
                                this.wait(); 
                            }
                        }
                    } else {
                        this.stopped = true;
                    }
                } else {
                    try {
                    	// if we are in trigger mode we fetch the next command on our own
                    	if (this.command == null && this.inQueue != null) {
                    		this.command = this.dequeue();
                    		
                    		/*
                    		 * If the command is null here, then it was rejected by one of the
                    		 * input-queue filters during dequeueing. 
                    		 */
                    		if (this.command == null) {
                    			this.logger.debug("Command was null. Maybe command was consumed by a queue filter");
                    			continue;
                    		}
                    	} else {                    	
	                    	// set threadname
	                    	this.setWorkerName();
                    	}
                    	
                    	// executing the new Command
                        this.execute(this.command);                        
                    } finally {
                        // write the modified command object to the out-queue
                        if (this.command != null) this.outQueue.enqueue(this.command);                    	
                    	
                        // signal that we have finished execution
                        this.done = true;
                        
                        // free memory
                        this.reset();                         
                        
                        // reset threadname
                    	this.setWorkerName();                    	
                    }
                }
            }
            this.logger.debug("Worker thread stopped from outside.");
        } catch (InterruptedException ex) {
        	this.logger.debug("Worker thread interrupted from outside.");
        } catch (Throwable ex) {
        	this.logger.error(String.format(
        			"Unexpected '%s' while processing command '%s'."
        			,ex.getClass().getName()
        			,this.command
        	),ex);
        } finally {
            if (this.myPool != null && !this.destroyed && !this.stopped) 
                this.myPool.invalidateWorker(this);
        }
    }
    
    private Data dequeue() throws InterruptedException {    	
    	if (this.inQueue instanceof ICommandFilterQueue) {
    		ICommandFilteringContext<ICommand> filteringContext = null;
    		try {
	    		// first step: getting the filtering context and pre-dequeue command
	    		filteringContext = ((ICommandFilterQueue<ICommand>)this.inQueue).getFilteringContext();
	    		this.command = (Data) filteringContext.getPreFilteredCmd();
			} finally {
				// notify the master that we have fetched the command
				synchronized (this) {
					this.notify();
				}
			}
    		
    		// changing thread name
    		this.setWorkerName(filteringContext.getLocation().toString());
    		
    		// second step: do the real filtering process
    		return (Data) filteringContext.dequeue();
    	} else {    	
			try {
				// fetch the next command
				return this.inQueue.dequeue();
			} finally {
				// notify the master that we have fetched the command
				synchronized (this) {
					this.notify();
				}
			}
    	}
    }

    /**
     * @see IWorker#assign(ICommand)
     */
    public void assign(Data cmd) {
        synchronized (this) {

            this.command = cmd;
            this.done = false;

            if (!this.running) {
                // if the thread is not running until yet, we need to start it now
                this.start();
            }  else {
                // inform the thread about the new command
                this.notifyAll();
            }
        }
    }
    
    /**
     * @throws InterruptedException 
     * @see IWorker#trigger()
     */
    public void trigger() throws InterruptedException {
    	if (this.inQueue == null) {
    		throw new IllegalStateException("No inputqueue set on this object.");
    	}
    	
        synchronized (this) {
            this.done = false;

            /*
             * Start or notify the thread
             */
            if (!this.running) {
                // if the thread is not running until yet, we need to start it now
                this.start();
            }  else {
                // inform the thread about the new command
                this.notifyAll();
            }
            
            // wait until the worker has dequeued the command
            this.wait(60000);
        }
    }
    
    /**
     * @see IWorker#getAssigned()
     */
    public Data getAssigned() {
    	return this.command;
    }
    
    protected void setWorkerName() {
    	this.setWorkerName((this.command == null)?"":"_"+this.command);
    }
    
    protected void setWorkerName(String postFix) {
    	String className = this.getClass().getSimpleName();    	
    	super.setName(String.format("%s_%s",className,postFix));
    }
    
    /**
     * This method must be extended by a concrete worker class
     * to free all command specific data (if any)
     */
    @OverrideMustInvoke
    protected void reset() {
    	this.command = null;
    }
    
    /**
     * This method must be implemented by the concrete worker class
     * and contains all operations needed for command processing
     * @param cmd the command to execute
     */
    protected abstract void execute(Data cmd);
    
    /**
     * @see IWorker#terminate()
     */
    public void terminate() {
    	if (this.stopped && !this.isAlive()) {
    		// thread already stopped
    		return;
    	}
    	
    	// signal termination to the worker thread
        this.stopped = true;     
        this.interrupt();
        
        // wait for the thread to finish
        // XXX: should we use a timeout here?
        try {
			this.join();
		} catch (InterruptedException e) {/* ignore this */}
    }
    
    /**
     * @see IWorker#destroy()
     */
	@Override
    public void destroy() {
    	this.destroyed = true;
    }
}
