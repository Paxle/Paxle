package org.paxle.core.threading.impl;

import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.IInputQueue;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;


public class Master<Data> extends Thread implements IMaster {
	
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
	 * total number of processed jobs
	 */
	protected int processedCount = 0;
	
	/**
	 * timestamp when the master-thread was started
	 */
	protected long startTime = System.currentTimeMillis();
	
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
            try {
            	// check if the master was paused
            	synchronized (this) {
					if (this.paused) this.wait();
				}
            	
                // getting a new command from the queue
                Data command = this.inQueue.dequeue();

                // getting a free worker from pool
                IWorker<Data> worker = this.pool.getWorker();

                // assign the command to the worker
                worker.assign(command);
                this.processedCount++;
                
            } catch (InterruptedException e) {
                Thread.interrupted();
                this.stopped = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // consuming the "is interrupted"-flag
        this.isInterrupted();

        // closing the pool
        try {
            this.pool.close();
        } catch (Exception e) {
            e.printStackTrace();
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
	 * TODO: better algorithm required here
	 */
	public int getPPM() {
		long uptime = (System.currentTimeMillis() - this.startTime) / 1000;
		return (int) (this.processedCount * 60 / Math.max(uptime, 1));
	}
}
