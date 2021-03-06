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

package org.paxle.data.balancer.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.ICommand;
import org.paxle.data.balancer.IDomainBalancer;

public class DomainBalancer implements IDataProvider<ICommand>, IDataSink<ICommand>, IDomainBalancer {
	
	private final Log logger = LogFactory.getLog(DomainBalancer.class);
	private final Writer writer = new Writer();
	private final Balancer balancer;
	
	private IDataSink<ICommand> sink = null;
	
	public DomainBalancer(final HostManager manager) {
		this.balancer = new Balancer(50, 20, manager);
		writer.start();
	}
	
	public void terminate() throws InterruptedException {
		writer.interrupt();
		writer.join(1000);
	}
	
	public int freeCapacity() throws Exception {
		return -1;
	}
	
	public boolean freeCapacitySupported() {
		return false;
	}
	
	public boolean offerData(ICommand data) throws Exception {
		return balancer.offer(data);
	}
	
	public void putData(ICommand data) throws Exception {
		balancer.put(data);
	}
	
	public void setDataSink(IDataSink<ICommand> dataSink) {
		if (dataSink == null) throw new NullPointerException("The data-sink is null-");
		if (this.sink != null) throw new IllegalStateException("The data-sink was already set.");
		synchronized (this.writer) {
			this.sink = dataSink;
			this.writer.notify();			
		}
	}
	
	private final class Writer extends Thread {
		
		public Writer() {
			super("DomainBalancer.Writer");
		}
		
		@Override
		public void run() {
			try {
				
				synchronized (this) {
					while (sink == null) this.wait();
				}
				
				while (!super.isInterrupted()) try {
					sink.putData(balancer.take());
				} catch (Exception e) {
					if (e instanceof InterruptedException)
						throw (InterruptedException)e;
					logger.error("Error: " + e);
				}
			} catch (InterruptedException e) {
				logger.info(super.getName() + " terminated.");
			}
		}
	}
}
