package org.paxle.core.impl;

import org.paxle.core.IMWComponent;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.queue.impl.InputQueue;
import org.paxle.core.queue.impl.OutputQueue;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IPool;

/**
 * @see IMWComponent
 */
public class MWComponent implements IMWComponent {
	private IMaster master;
	private IPool pool;
	private InputQueue inQueue;
	private OutputQueue outQueue;
	
	
	public MWComponent(IMaster master, IPool pool, InputQueue inQueue, OutputQueue outQueue) {
		if (master == null) throw new NullPointerException("The master thread is null.");
		if (pool == null) throw new NullPointerException("The thread-pool is null");
		if (inQueue == null) throw new NullPointerException("The input-queue is null");
		if (outQueue == null) throw new NullPointerException("The output-queue is null");
		this.master = master;
		this.pool = pool;
		this.inQueue = inQueue;
		this.outQueue = outQueue;		
	}

	/**
	 * @see IMWComponent#getDataSink()
	 */
	public IDataSink getDataSink() {
		return this.inQueue;
	}

	/**
	 * @see IMWComponent#getDataSource()
	 */
	public IDataSource getDataSource() {
		return this.outQueue;
	}	

	/**
	 * {@link IMWComponent#getMaster()}
	 */
	public IMaster getMaster() {
		return this.master;
	}
	
	/**
	 * {@link IMWComponent#getPool()}
	 */
	public IPool getPool() {
		return this.pool;
	}

	/**
	 * {@link IMWComponent#terminate()}
	 */
	public void terminate() {
		// terminating the master-thread (this automatically closes the worker-pool)
		this.master.terminate();
		
		// TODO: closing the queues
	}
}
