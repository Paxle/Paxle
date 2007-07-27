package org.paxle.core.threading.impl;

import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.IInputQueue;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;


public class Master<Data> extends Thread implements IMaster {
	
	protected IPool<Data> pool = null;
	protected IInputQueue<Data> inQueue = null;
	protected boolean stopped = false;
	protected boolean paused = false;
	
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
	}
	
	/**
	 * @see IMaster#isPaused()
	 */
	public boolean isPaused() {
		return this.paused;
	}
}
