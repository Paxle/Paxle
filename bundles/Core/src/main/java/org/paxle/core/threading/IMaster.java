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


public interface IMaster<Data> {
	/**
	 * TODO: do we need an explicit start?
	 */
	public void start();
	
	public void terminate();
	
	public void pauseMaster();
	
	public void resumeMaster();
	
	public boolean isPaused();
	
	/**
	 * Function to configure the delay the master thread
	 * should pause between two busy loops.
	 * 
	 * @param delay the delay in ms. set this to <code><=0</code> to disable the delay
	 */
	public void setDelay(int delay);
	
	/**
	 * @return the delay in ms between jobs
	 */
	public int getDelay();
	
	/**
	 * @return the PPM of this component since startup
	 */
	public int getPPM();
	
	/**
	 * Process the next job in the queue if the componend was paused
	 */
	public void processNext();	
	
	/**
	 * @return total number of processed jobs since startup
	 */
	public int processedCount();
	
	/**
	 * Calling this function forces the {@link IMaster} to immediately process the given job object. 
	 * The caller of this function will block until the processing of the job has finished.
	 *  
	 * @param cmd the job to process
	 * @throws Exception
	 */
	public void process(Data cmd) throws Exception;
}
