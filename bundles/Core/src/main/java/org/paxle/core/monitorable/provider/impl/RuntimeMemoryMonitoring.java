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
package org.paxle.core.monitorable.provider.impl;

import java.util.HashMap;

import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;

/**
 * @scr.component name="java.lang.runtime"
 * @scr.service interface="org.osgi.service.monitor.Monitorable"
 */
public class RuntimeMemoryMonitoring implements Monitorable {
	private static final String MEMORY_FREE = "memory.free";
	private static final String MEMORY_MAX = "memory.max";
	private static final String MEMORY_TOTAL = "memory.total";
	private static final String MEMORY_USED = "memory.used";

	@SuppressWarnings("serial")
	private static final HashMap<String, String> VAR_DESCRIPTIONS = new HashMap<String, String>() {{
		put(MEMORY_FREE, "Current amount of free memory in the JVM (in bytes)");
		put(MEMORY_MAX, "Current amount of max memory in the JVM (in bytes)");
		put(MEMORY_TOTAL, "Current amount of total memory in the JVM (in bytes)");
		put(MEMORY_USED, "Current amount of memory used by the JVM (in bytes)");
	}};
	
	/**
	 * @see Monitorable#getStatusVariableNames()
	 */
	public String[] getStatusVariableNames() {
		return VAR_DESCRIPTIONS.keySet().toArray(new String[VAR_DESCRIPTIONS.size()]);
	}	
	
	/**
	 * @see Monitorable#getDescription(String)
	 */
	public String getDescription(String name) throws IllegalArgumentException {
		if (!VAR_DESCRIPTIONS.containsKey(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		return VAR_DESCRIPTIONS.get(name);
	}

	/**
	 * @see Monitorable#getStatusVariable(String)
	 */
	public StatusVariable getStatusVariable(String name) throws IllegalArgumentException {
		if (!VAR_DESCRIPTIONS.containsKey(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		long mem = 0;
		final Runtime rt = Runtime.getRuntime();		

		if (name.equals(MEMORY_TOTAL)) mem = rt.totalMemory(); 
		else if (name.equals(MEMORY_MAX)) mem = rt.maxMemory();
		else if (name.equals(MEMORY_FREE)) mem = rt.maxMemory() - rt.totalMemory() + rt.freeMemory();
		else if (name.equals(MEMORY_USED)) mem = rt.totalMemory() - rt.freeMemory();
		
		return new StatusVariable(
				name,
				StatusVariable.CM_GAUGE,
				(int)mem
			);
	}

	/**
	 * @see Monitorable#notifiesOnChange(String)
	 */
	public boolean notifiesOnChange(String name) throws IllegalArgumentException {
		return false;
	}

	/**
	 * @see Monitorable#resetStatusVariable(String)
	 */
	public boolean resetStatusVariable(String name) throws IllegalArgumentException {
		return false;
	}

}
