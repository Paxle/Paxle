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

package org.paxle.core.data.impl;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.queue.InputQueue;
import org.paxle.core.queue.OutputQueue;

public class DataPipeTest extends MockObjectTestCase {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDataPipe() throws InterruptedException {
		IDataSink<String> sink = new InputQueue<String>(5);
		IDataSource<String> source = new OutputQueue<String>(5);
		
		DataPipe<String> pipe = new DataPipe<String>();
		pipe.setDataSink(sink);
		pipe.setDataSource(source);
		
		// writing data into the queue
		for (int i=0; i < 5; i++) {
			((OutputQueue<String>)source).enqueue("DummyData" + i);
		}
		
		// reading data from the queue
		for (int i=0; i < 5; i++) {
			String dummyData = ((InputQueue<String>)sink).dequeue();
			assertNotNull(dummyData);
			assertEquals("DummyData" + i, dummyData);
		}
		
		// shutdown the pipe
		pipe.terminate();
	}
}
