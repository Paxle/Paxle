package org.paxle.core;

import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.queue.IInputQueue;
import org.paxle.core.queue.IOutputQueue;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;

/**
 * A Master/Worker Component is created by a {@link IMWComponentManager factory} and
 * consists of the following parts:
 * <ul>
 * 	<li>a {@link IMaster master-thread}</li>
 * 	<li>a {@link IPool pool} of {@link IWorker worker-threads}</li>
 * 	<li>an {@link IDataSink data-sink} to load data into the internal {@link IInputQueue}</li>
 * 	<li>an {@link IDataSource data-source} to read data from the internal {@link IOutputQueue}</li>
 * </ul>
 */
public interface IMWComponent {
	/**
	 * @return the {@link IMaster master-thread} of the component
	 */
	public IMaster getMaster();
	
	/**
	 * @return the {@link IWorker worker-thread}-{@link IPool pool} of the component
	 */
	public IPool getPool();

	public IDataSink getDataSink();
	
	public IDataSource getDataSource();
	
	/**
	 * Function to terminate the component. Calling this function results in:
	 * <ul>
	 * 	<li>Terminating the {@link IMaster master-thread}</li>
	 * 	<li>Terminating all {@link IWorker worker-threads}</li>
	 * 	<li>Closing the {@link IPool therad-pool}</li>
	 * 	<li>Closing the {@link IInputQueue input-}/{@link IOutputQueue output-}queue
	 * </ul>
	 */
	public void terminate();
}
