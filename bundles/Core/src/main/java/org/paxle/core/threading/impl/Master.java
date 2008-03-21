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
	 * total number of processed jobs since startup. Not used at the moment, if you want to use this, uncomment the appropriate line in run()
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
	 * @param threadPool the thread pool containing {@link IWorker worker-threads}
	 * @param commandQueue the queue containing {@link ICommand commands} to process
	 */
	public Master(IPool<Data> threadPool, IInputQueue<Data> commandQueue) {		
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
            	
                // getting a new command from the queue
                command = this.inQueue.dequeue();

                // getting a free worker from pool
                IWorker<Data> worker = this.pool.getWorker();

                // assign the command to the worker
                worker.assign(command);
                this.processedCount++;
                command = null;
                
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
}
