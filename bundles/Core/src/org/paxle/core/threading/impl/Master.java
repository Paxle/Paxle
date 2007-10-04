package org.paxle.core.threading.impl;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.IInputQueue;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;


public class Master<Data> extends Thread implements IMaster {
	
	/**
	 * Semaphore used for the PPM update and calculation
	 */
	protected Semaphore ppmsm = new Semaphore(1, true);
	
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
	 * This list stores the number of elements processed since the last 
	 * @see getCleanPPM()
	 */
	private LinkedList<Long> ppm = new LinkedList<Long>();
	
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
                
                //add the job to the total job-count and the PPM
                //this.processedCount++; //commented out, because value is not used atm. Saves CPU time.
                this.ppmsm.acquire();
                this.ppm.addLast(System.currentTimeMillis());
                this.ppmsm.release();
                //Nobody should have more than 200 PPS, so we clean the DB if it gets to big after some time to save memory
                if (this.ppm.size() > 12000) getCleanPPM();

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
	 * Cleans all entries from the PPM-DB that are older than 1 minute.
	 * @return The number of files processed in the last minute
	 */
	private int getCleanPPM() {
		//the timestamp 60 seconds ago
		long maxage = System.currentTimeMillis();
		maxage = maxage - 60000;
		
		try {
			this.ppmsm.acquire();
		} catch (InterruptedException e) {
			//Should only occur on program shutdown
		}
		
		while (this.ppm.size() > 0 && this.ppm.getFirst() < maxage) {
			this.ppm.removeFirst();
		}
		
		//store in special variable so the value is not altered after semaphore release 
		int retval = this.ppm.size();
		this.ppmsm.release();
		return retval;
	}
	
	/**
	 * @see IMaster#getPPM()
	 */
	public int getPPM() {
		return (getCleanPPM());
	}
}
