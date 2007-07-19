package org.paxle.core.threading;

import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.IOutputQueue;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;

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
public abstract class AWorker extends Thread implements IWorker {

    /**
     * The crawler thread pool
     */
    private IPool myPool = null;
    
    /**
     * The output-queue where the modified command should
     * be enqueued.
     */
    private IOutputQueue outQueue = null;
        
    /**
     * The next {@link ICommand command} that must be processed by the worker  
     */
    private ICommand command = null;    
	
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
    public void setPool(IPool pool) {
    	this.myPool = pool;
    }
    
    /**
     * Setter method to set the output-queue
     */
    public void setOutQueue(IOutputQueue outQueue) {
    	this.outQueue = outQueue;
    }
    
    @Override
    public void run() {
        this.running = true;

        try {
        	if (this.outQueue == null) throw new IllegalArgumentException("Output-Queue was not set properly.");
        	
            // The thread keeps running.
            while (!this.stopped && !this.isInterrupted()) {        
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
                        // executing the new Command
                        execute(this.command);                        
                    } finally {
                        // write the modified command object to the out-queue
                        this.outQueue.enqueue(this.command);                    	
                    	
                        // signal that we have finished execution
                        this.done = true;
                        
                        // free memory
                        reset();
                    }
                }
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            if (this.myPool != null && !this.destroyed) 
                this.myPool.invalidateWorker(this);
        }
    }

    /**
     * @see IWorker#assign(ICommand)
     */
    public void assign(ICommand cmd) {
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
     * This method must be extended by a concrete worker class
     * to free all command specific data (if any)
     */
    protected void reset() {
    	this.command = null;
    }
    
    /**
     * This method must be implemented by the concrete worker class
     * and contains all operations needed for command processing
     * @param cmd the command to execute
     */
    protected abstract void execute(ICommand cmd);
    
    /**
     * @see IWorker#terminate()
     */
    public void terminate() {
        this.stopped = true;     
        this.interrupt();
        try {
			this.join();
		} catch (InterruptedException e) {/* ignore this */}
    }
}
