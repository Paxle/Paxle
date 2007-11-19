package org.paxle.core.impl;

import java.util.Arrays;
import java.util.List;

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
public class MWComponent<Data> implements IMWComponent<Data> {
	private IMaster master;
	private IPool<Data> pool;
	private InputQueue<Data> inQueue;
	private OutputQueue<Data> outQueue;
	
	public MWComponent(IMaster master, IPool<Data> pool, InputQueue<Data> inQueue, OutputQueue<Data> outQueue) {
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
	 * {@inheritDoc}
	 * @see IMWComponent#getDataSink()
	 */
	public IDataSink<Data> getDataSink() {
		return this.inQueue;
	}

	/**
	 * {@inheritDoc}
	 * @see IMWComponent#getDataSource()
	 */
	public IDataSource<Data> getDataSource() {
		return this.outQueue;
	}	

	/**
	 * {@inheritDoc}
	 * @see IMWComponent#getMaster()
	 */
	public IMaster getMaster() {
		return this.master;
	}
	
	/**
	 * {@inheritDoc}
	 * @see IMWComponent#getPool()
	 */
	public IPool<Data> getPool() {
		return this.pool;
	}

	/**
	 * {@inheritDoc}
	 * @see IMWComponent#terminate()
	 */
	public void terminate() {
		// terminating the master-thread (this automatically closes the worker-pool)
		this.master.terminate();
		
		// TODO: closing the queues
	}
	
	/**
	 * {@inheritDoc}
	 * @see IMWComponent#isPaused()
	 */
	public boolean isPaused() {
		return this.master.isPaused();
	}
	
	/**
	 * {@inheritDoc}
	 * @see IMWComponent#pause()
	 */	
	public void pause(){
		this.master.pauseMaster();
	}
	
	/**
	 * {@inheritDoc}
	 * @see IMWComponent#resume()
	 */	
	public void resume() {
		this.master.resumeMaster();
	}
	
	/**
	 * {@inheritDoc}
	 * @see IMWComponent#getPPM()
	 */
	public int getPPM() {
		return this.master.getPPM();
	}

	/**
	 * {@inheritDoc}
	 * @see IMWComponent#getPPM()
	 */	
	public void processNext() {
		this.master.processNext();
	}

	/**
	 * @see IMWComponent#getActiveJobs()
	 */
	public List<Data> getActiveJobs() {
		return this.pool.getActiveJobs();
	}
	
	/**
	 * @see IMWComponent#getActiveJobCount()
	 */
	public int getActiveJobCount() {
		return this.pool.getActiveJobCount();
	}
	
	/**
	 * @see IMWComponent#getEnqueuedJobs()
	 */
	@SuppressWarnings("unchecked")
	public List<Data> getEnqueuedJobs() {
		return (List<Data>) Arrays.asList(this.inQueue.toArray());
	}
	
	/**
	 * @see IMWComponent#getEnqueuedJobCount()
	 */
	public int getEnqueuedJobCount() {
		return this.inQueue.size();
	}
}
