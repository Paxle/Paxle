/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.core.queue;

import org.paxle.core.data.IDataSource;

/**
 * This acts as an {@link IOutputQueue output-queue} for a {@link IMWComponent master-worker-component}
 * and as a {@link IDataSource data-sink} for a {@link IDataConsumer data-consumer}.
 */
public class OutputQueue<Data> extends AQueue<Data> implements IOutputQueue<Data>, IDataSource<Data> {
	
	private static final long serialVersionUID = 1L;
	
	public OutputQueue(int length) {		
		super(length);
	}
	
	public OutputQueue(final int length, final boolean limited) {
		super(length, limited);
	}
	
	/**
	 * @see IOutputQueue#enqueue(ICommand)
	 */
	public void enqueue(Data command) throws InterruptedException {
		if (command == null) throw new NullPointerException("Command is null.");
		
		// add it to the out buffer
		this.queue.put(command);
	}

	/**
	 * @see IDataSource#getData()
	 */
	public Data getData() throws InterruptedException {
		return this.queue.take();
	}
}
