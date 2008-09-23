package org.paxle.core.queue.impl;

import org.paxle.core.IMWComponent;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.IInputQueue;

/**
 * This acts as an {@link IInputQueue input-queue} for a {@link IMWComponent master-worker-component}
 * and as a {@link IDataSink data-sink} for a {@link IDataProvider data-provider}.
 * 
 * Elements can be written via {@link #putData(Object)} or {@link #offerData(Object)} and can be 
 * read via {@link #dequeue()}.
 */
public class InputQueue<Data> extends AQueue<Data> implements IInputQueue<Data>, IDataSink<Data> {
	private static final long serialVersionUID = 1L;	
	
	public InputQueue(int length) {
		super(length);
	}

	/**
	 * @see IInputQueue#dequeue()
	 */
	public Data dequeue() throws InterruptedException {
		return this.queue.take();
	}

	/**
	 * @see IDataSink#putData(Object)
	 */
	public void putData(Data data) throws InterruptedException {
		// put data into the internal queue
		this.queue.put(data);
		
		// notify the next consumer blocking on #waitForNext() 
		// about the insertion of the new data		
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * @see IDataSink#offerData(Object)
	 */
	public boolean offerData(Data data) throws Exception {
		// enqueue data into the internal queue (if space is available)
		boolean success = this.queue.offer(data);
		
		// notify the next consumer blocking on #waitForNext() 
		// about the insertion of the new data			
		if (success) {
			synchronized (this) {
				// notify the next thread waiting for new data
				// see: #waitForNext()				
				this.notify();
			}
		}
		return success;
	}	
	
	/**
	 * @see IInputQueue#waitForNext()
	 */
	public void waitForNext() throws InterruptedException {
		synchronized(this) {
			if (super.size() == 0) this.wait();
		}
	}

	/**
	 * @see IDataSink#freeCapacity()
	 */
	public int freeCapacity() throws Exception {
		return super.remainingCapacity();
	}

	/**
	 * @see IDataSink#freeCapacitySupported()
	 */
	public boolean freeCapacitySupported() {
		return true;
	}
}
