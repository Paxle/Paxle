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

package org.paxle.tools.sysmon.impl;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import jsysmon.JSysmon;
import jsysmon.MemoryMonitoringData;
import jsysmon.MemoryMonitoringListener;
import junit.framework.TestCase;

/**
 * ATTENTION: if you plan to run this test in eclipse ensure that you have configured
 *			  <code>java.library.path</code> properly.
 */
public class MemMonitoringTest extends TestCase {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		JSysmon.setUpdateDelay(1000);
		JSysmon.startMonitoring();
	}
	
	@Override
	protected void tearDown() throws Exception {	
		super.tearDown();
		JSysmon.stopMonitoring();
	}
	
	public void testMemMonitoring() throws InterruptedException {		
		final Semaphore sync = new Semaphore(0);
		
		JSysmon.addMemoryMonitoringListener(new MemoryMonitoringListener(){
			public void memoryMonitoringUpdate(MemoryMonitoringData data) {				
				System.out.println("TOTAL_MEM_INDEX: \t" + data.getUsage(MemoryMonitoringData.TOTAL_MEM_INDEX));
				System.out.println("USED_MEM_INDEX: \t" + data.getUsage(MemoryMonitoringData.USED_MEM_INDEX));
				System.out.println("FREE_MEM_INDEX: \t" + data.getUsage(MemoryMonitoringData.FREE_MEM_INDEX));
				sync.release();
			}
			
		});

		sync.tryAcquire(5, TimeUnit.SECONDS);
	}
}
