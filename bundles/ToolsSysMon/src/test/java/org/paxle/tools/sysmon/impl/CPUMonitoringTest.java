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
package org.paxle.tools.sysmon.impl;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import jsysmon.CPUMonitoringData;
import jsysmon.CPUMonitoringListener;
import jsysmon.JSysmon;
import junit.framework.TestCase;

/**
 * ATTENTION: if you plan to run this test in eclipse ensure that you have configured
 *			  <code>java.library.path</code> properly.
 */
public class CPUMonitoringTest extends TestCase {
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
	
	public void testCPUMonitoring() throws InterruptedException {		
		final Semaphore sync = new Semaphore(0);
		
		JSysmon.addCPUMonitoringListener(new CPUMonitoringListener() {
			public void cpuMonitoringUpdate(CPUMonitoringData data) {
				System.out.println(data.toString());
				sync.release();
			}			
		});
		
		assertTrue(sync.tryAcquire(20, TimeUnit.SECONDS));
	}
}
