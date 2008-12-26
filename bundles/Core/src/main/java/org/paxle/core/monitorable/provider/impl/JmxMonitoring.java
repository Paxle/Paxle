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

package org.paxle.core.monitorable.provider.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;

public class JmxMonitoring implements Monitorable {
	public static final String SERVICE_PID = "java.lang.management";
	
	private static final String VAR_NAME_SYS_LOAD = "os.systemLoadAvg";
	private static final String VAR_NAME_CPU_TIME = "os.process.cpu";
	private static final String VAR_NAME_OPEN_FILE_DESCR_COUNT = "os.fileDescr.open";
	private static final String VAR_NAME_MAX_FILE_DESCR_COUNT = "os.fileDescr.max";
	private static final String VAR_NAME_VIRTUAL_MEMORY = "os.memory.virtual";
	private static final String VAR_NAME_PHYSICAL_MEMORY = "os.memory.physical";
	private static final String VAR_NAME_SWAP_SPACE = "os.memory.swap";

    private static final HashMap<String, Method> METHOD_MAP = new HashMap<String, Method>();
    
	/**
	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private static final HashSet<String> VAR_NAMES =  new HashSet<String>(Arrays.asList(new String[] {
			
	}));
	
	/**
	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private static final HashMap<String, String> VAR_DESCRIPTIONS = new HashMap<String, String>();
	static {
		VAR_DESCRIPTIONS.put(VAR_NAME_SYS_LOAD, "The system load average for the last minute.");
		VAR_DESCRIPTIONS.put(VAR_NAME_CPU_TIME, "The CPU time used by the process on which the Java virtual machine is running in nanoseconds.");
		VAR_DESCRIPTIONS.put(VAR_NAME_OPEN_FILE_DESCR_COUNT, "The number of open file descriptors.");
		VAR_DESCRIPTIONS.put(VAR_NAME_MAX_FILE_DESCR_COUNT, "The maximum number of file descriptors.");
		VAR_DESCRIPTIONS.put(VAR_NAME_VIRTUAL_MEMORY, "The amount of virtual memory that is guaranteed to be available to the running process in MB, or -1 if this operation is not supported.");
		VAR_DESCRIPTIONS.put(VAR_NAME_PHYSICAL_MEMORY, "The amount of free physical memory in MB.");
		VAR_DESCRIPTIONS.put(VAR_NAME_SWAP_SPACE, "The amount of free swap space in MB.");
	}	
	
	private OperatingSystemMXBean os;
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());	
	
	public JmxMonitoring() {
		this.os = ManagementFactory.getOperatingSystemMXBean();
		
		// testing which operations are available
		this.testMethod(VAR_NAME_SYS_LOAD, "systemLoadAverage");
		this.testMethod(VAR_NAME_OPEN_FILE_DESCR_COUNT, "openFileDescriptorCount");
		this.testMethod(VAR_NAME_MAX_FILE_DESCR_COUNT, "maxFileDescriptorCount");
		this.testMethod(VAR_NAME_VIRTUAL_MEMORY, "committedVirtualMemorySize");		
		this.testMethod(VAR_NAME_PHYSICAL_MEMORY, "totalPhysicalMemorySize");
		this.testMethod(VAR_NAME_SWAP_SPACE, "totalSwapSpaceSize");
		this.testMethod(VAR_NAME_CPU_TIME, "processCpuTime");
	}
	
	private void testMethod(String varName, String property) {
		try {
			String methodName = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
			Class clazz = this.os.getClass();
			Method method = clazz.getMethod(methodName);
			method.setAccessible(true);
			Object test = method.invoke(this.os, (Object[])null);
			if(test != null) {
				METHOD_MAP.put(varName, method);
				VAR_NAMES.add(varName);
			}
		} catch (Throwable e) {
			this.logger.info(String.format(
					"Property '%s' is not available: %s",
					property,
					e.getMessage()
			));
		}
	}
	
	public String getDescription(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		return VAR_DESCRIPTIONS.get(name);
	}

	public StatusVariable getStatusVariable(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		StatusVariable var = null;
		try {
			Method method = METHOD_MAP.get(name);
			Object value = method.invoke(this.os, (Object[])null);
			
			if (name.equals(VAR_NAME_SYS_LOAD)) {
				var = new StatusVariable(name,StatusVariable.CM_GAUGE,((Double)value).floatValue());
			} else if (name.startsWith("os.memory")){
				long val = ((Long)value).longValue() ;
				if (val > 0) val = val / (1024*1024);
				var = new StatusVariable(name,StatusVariable.CM_GAUGE,(int)val);
			} else if (name.startsWith("os.fileDescr")) {
				var = new StatusVariable(name,StatusVariable.CM_GAUGE,((Long)value).intValue());
			} else {
				var = new StatusVariable(name,StatusVariable.CM_GAUGE,((Long)value).floatValue());
			}

		} catch (Throwable e) {
			this.logger.error(e);
		}
		return var;
	}

	public String[] getStatusVariableNames() {
		return VAR_NAMES.toArray(new String[VAR_NAMES.size()]);
	}

	public boolean notifiesOnChange(String name) throws IllegalArgumentException {
		return false;
	}

	public boolean resetStatusVariable(String name) throws IllegalArgumentException {
		return false;
	}
}
