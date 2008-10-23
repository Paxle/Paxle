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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;

public class DiskspaceMonitoring implements Monitorable {
	public static final String PID = "os.disk";
	public static final String VAR_SPACE_FREE = "disk.space.free";
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());	
	
	public String[] getStatusVariableNames() {
		return new String[] {VAR_SPACE_FREE};
	}	
	
	public String getDescription(String name) throws IllegalArgumentException {
		if (!VAR_SPACE_FREE.equals(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}		
		
		return "Free Disk";
	}

	public StatusVariable getStatusVariable(String name) throws IllegalArgumentException {
		if (!VAR_SPACE_FREE.equals(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		try {
			long freeDisk = FileSystemUtils.freeSpaceKb(new File("/").getCanonicalPath().toString());
			freeDisk /= 1024;
			return new StatusVariable(name, StatusVariable.CM_GAUGE, freeDisk);
		} catch (IOException e) {
			this.logger.error(String.format(
					"Unexpected '%s' while trying to query free disk-space.",
					e.getClass().getName()
			),e);
			return null;
		}
	}

	public boolean notifiesOnChange(String name) throws IllegalArgumentException {
		return false;
	}

	public boolean resetStatusVariable(String name) throws IllegalArgumentException {
		return false;
	}
}
