/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.core;

import java.util.List;

import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.IInputQueue;
import org.paxle.core.queue.IOutputQueue;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;

/**
 * A Master/Worker Component is created by a {@link IMWComponentFactory factory} and
 * consists of the following parts:
 * <ul>
 * 	<li>a {@link IMaster master-thread}</li>
 * 	<li>a {@link IPool pool} of {@link IWorker worker-threads}</li>
 * 	<li>an {@link IDataSink data-sink} to load data into the internal {@link IInputQueue}</li>
 * 	<li>an {@link IDataSource data-source} to read data from the internal {@link IOutputQueue}</li>
 * </ul>
 */
public interface IMWComponent<Data> {
	public static final String COMPONENT_ID = "mwcomponent.ID";
	public static final String POSTFIX_SOURCE_ID = ".source";
	public static final String POSTFIX_SINK_ID = ".sink";
	
	/**
	 * @return the {@link IMaster master-thread} of the component
	 */
	public IMaster<Data> getMaster();
	
	/**
	 * @return the {@link IWorker worker-thread}-{@link IPool pool} of the component
	 */
	public IPool<Data> getPool();
	
	/**
	 * This function returns a {@link IDataSink data-sink} that can be used to 
	 * write {@link ICommand commands} into the input-queue of the  Master/Worker Component.
	 * @return the data-sink associated with the Master/Worker
	 */
	public IDataSink<Data> getDataSink();
	
	/**
	 * This function returns a {@link IDataSource data-source} that can be used to 
	 * read {@link ICommand commands} from the output-queue of the  Master/Worker Component.
	 * @return the data-source associated with the Master/Worker
	 */
	public IDataSource<Data> getDataSource();
	
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
	
	/**
	 * This function calls the {@link IMaster#isPaused()} method
	 * @return <code>true</code> if the Master/Worker Component was paused.
	 */
	public boolean isPaused();
	
	/**
	 * Function to pause the Master/Worker Component.
	 * This function calls the {@link IMaster#pauseMaster()} method.
	 */
	public void pause();
	
	/**
	 * Function to resume the Master/Worker Component.
	 * This function calls the {@link IMaster#resumeMaster()} method.
	 */	
	public void resume();
	
	/**
	 * Process the next job in the queue if the component was paused
	 */
	public void processNext();
	
	/**
	 * @return the PPM of this component since startup
	 */
	public int getPPM();
	
	/**
	 * @return the list of active jobs currently processed by the workers of this pool 
	 */
	public List<Data> getActiveJobs();
	
	/**
	 * @return the size of the active-job queue 
	 */
	public int getActiveJobCount();
	
	/**
	 * @return the list of currently enqueued jobs
	 */
	public List<Data> getEnqueuedJobs();
	
	/**
	 * @return the size of the enqueued-job queue
	 */
	public int getEnqueuedJobCount();

	/**
	 * Calling this function forces the {@link IMWComponent} to immediately process the given job object. 
	 * The caller of this function will block until the processing of the job has finished.
	 *  
	 * @param cmd the job to process
	 * @throws Exception
	 */
	public void process(Data cmd) throws Exception;
}
