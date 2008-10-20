/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.core.data.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;

/**
 * This pipe acts as {@link IDataConsumer data-consumer} and {@link IDataProvider data-provider}
 * and just copies data from a {@link IDataSource data-source} to a {@link IDataSink data-sink}
 */
public class DataPipe<Data> extends Thread implements IDataProvider<Data>, IDataConsumer<Data> {
	
	private IDataSink<Data> sink = null;
	private IDataSource<Data> source = null;
	
	/**
	 * for logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Indicates that the current thread was stopped through a call
	 * to {@link #terminate()}
	 */
	private boolean stopped = false;
	
	public DataPipe() {
		this.start();
	}
	
	/**
	 * @see IDataProvider#setDataSink(IDataSink)
	 */
	public synchronized void setDataSink(IDataSink<Data> dataSink) {
		if (dataSink == null) throw new NullPointerException("The data-sink is null.");
		if (this.sink != null) throw new IllegalStateException("The data-sink was already set.");
		this.sink = dataSink;
		this.notify();
	}

	/**
	 * @see IDataConsumer#setDataSource(IDataSource)
	 */
	public synchronized void setDataSource(IDataSource<Data> dataSource) {
		if (dataSource == null) throw new NullPointerException("The data-source is null.");
		if (this.source != null) throw new IllegalStateException("The data-source was already set.");
		this.source = dataSource;
		this.notify();
	}
	
	/**
	 * Function to terminate the {@link DataPipe} thread.
	 * @throws InterruptedException 
	 */
	public void terminate() throws InterruptedException {
		// setting stop flag
		this.stopped = true;
		
		// interrupt thread if required
		this.interrupt();
		
		// wait for termination
		this.join(200);
	}
	
	/**
	 * @see Thread#run()
	 */
	@Override
	public void run() {
		try {
			synchronized (this) {
				while ((this.sink == null) || (this.source == null)) this.wait();
			}
			
			while (!this.stopped && !this.isInterrupted()) {
				Data data = this.source.getData();
				this.sink.putData(data);
			}
		} catch (Exception e) {
			if (!(e instanceof InterruptedException)) {
				this.logger.error(String.format(
						"%s: Unexpected '%s' while copying data.",
						this.getName(),
						e.getClass().getName()
				),e);
			} else {
				this.logger.info("Thread stopped successfully.");
			}
		}
	}

}
