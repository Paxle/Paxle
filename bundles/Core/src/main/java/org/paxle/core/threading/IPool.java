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
package org.paxle.core.threading;

import java.util.List;

public interface IPool<Data> {

	/**
	 * @return a new {@link IWorker worker-thread} from the thread pool
	 * @throws InterruptedException if the thread was interrupted while waiting
	 * for a free worker thread.
	 */
	public IWorker<Data> getWorker() throws Exception;
	
	public IWorker<Data> getWorker(boolean fromPool) throws Exception;
	
	/**
	 * Returns the {@link IWorker worker-thread} into the thread pool
	 * @param worker
	 */
	public void returnWorker(IWorker<Data> worker);
	
	/**
	 * Notifies the pool that the {@link IWorker worker-thread} should not be used anymore
	 * @param worker
	 */
	public void invalidateWorker(IWorker<Data> worker);
	
	/**
	 * @return the list of active jobs currently processed by the workers of this pool 
	 */
	public List<Data> getActiveJobs();
	
	/**
	 * @return the size of the active job queue
	 */
	public int getActiveJobCount();

	public int getNotPooledActiveJobCount();	
	
	/**
	 * @return the maximum number of active jobs
	 */
	public int getMaxActiveJobCount();
	
	/**
	 * Close the thread pool and interrupts all running threads .
	 */
	public void close() throws Exception;
	
	/**
	 * Indicates if the thread-pool was closed via a call to {@link #close()}.
	 * @return <code>true</code> if the thread-pool was closed.
	 */
	public boolean closed();
}
