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

import org.osgi.service.monitor.StatusVariable;

import junit.framework.TestCase;

public class DiskspaceMonitoringTest extends TestCase {
	private DiskspaceMonitoring mon;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		System.setProperty("paxle.data", "target");		
		this.mon = new DiskspaceMonitoring();
	}
	
	public void testQueryFreeDiskSpace() {
		StatusVariable var = this.mon.getStatusVariable(DiskspaceMonitoring.VAR_SPACE_FREE);
		assertNotNull(var);
		System.out.println(var);
		
		var = this.mon.getStatusVariable(DiskspaceMonitoring.VAR_QUERY_MODE);
		assertNotNull(var);
		System.out.println(var);
	}
}
