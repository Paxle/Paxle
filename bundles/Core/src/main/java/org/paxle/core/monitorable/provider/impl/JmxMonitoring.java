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

package org.paxle.core.monitorable.provider.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;

@Component(metatype=false, name=JmxMonitoring.PID)
@Service(Monitorable.class)
@Property(name="Monitorable-Localization", value=JmxMonitoring.RB_BASENAME)
public class JmxMonitoring implements Monitorable {
	/**
	 * The {@link Constants#SERVICE_PID} of this {@link Monitorable}
	 */
	static final String PID = "java.lang.management";
	
	/**
	 * {@link ResourceBundle} basename
	 */
	static final String RB_BASENAME = "OSGI-INF/l10n/JmxMonitoring";		
	
	/**
	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private final ResourceBundle rb = ResourceBundle.getBundle(RB_BASENAME);
	
	/* =========================================================================
	 * OPERATING SYSTEM props
	 * ========================================================================= */
	private static final String VAR_NAME_SYS_LOAD = "os.systemLoadAvg";
	private static final String VAR_NAME_CPU_TIME = "os.process.cpu";
	private static final String VAR_NAME_OPEN_FILE_DESCR_COUNT = "os.fileDescr.open";
	private static final String VAR_NAME_MAX_FILE_DESCR_COUNT = "os.fileDescr.max";
	private static final String VAR_NAME_VIRTUAL_MEMORY = "os.memory.virtual";
	private static final String VAR_NAME_PHYSICAL_MEMORY = "os.memory.physical";
	private static final String VAR_NAME_SWAP_SPACE = "os.memory.swap";

	/* =========================================================================
	 * JAVA RUNTIME props
	 * ========================================================================= */	
	private static final String VAR_NAME_UPTIME = "runtime.uptime";
	private static final String VAR_NAME_STARTTIME = "runtime.starttime";
	
	/**
	 * A mapping between the full-qualified {@link StatusVariable} name and the method of
	 * the jmx bean that needs to be executed to get the status-value.
	 */
    private static final HashMap<String, Method> METHOD_MAP = new HashMap<String, Method>();
    
	/**
	 * A mapping between the full-qualified {@link StatusVariable} name and the bean that
	 * needs to be accessed to get the status-value.
	 */
    private static final HashMap<String, Object> BEAN_MAP = new HashMap<String, Object>();
    
	/**
	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private static final HashSet<String> VAR_NAMES =  new HashSet<String>();
	
	private OperatingSystemMXBean operatingSystem;
	private RuntimeMXBean runtime;
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());	
	
	public JmxMonitoring() {
		this.operatingSystem = ManagementFactory.getOperatingSystemMXBean();
		this.runtime = ManagementFactory.getRuntimeMXBean();
		
		// testing which operations are available
		this.testMethod(VAR_NAME_SYS_LOAD, this.operatingSystem, "systemLoadAverage");
		this.testMethod(VAR_NAME_OPEN_FILE_DESCR_COUNT, this.operatingSystem, "openFileDescriptorCount");
		this.testMethod(VAR_NAME_MAX_FILE_DESCR_COUNT, this.operatingSystem, "maxFileDescriptorCount");
		this.testMethod(VAR_NAME_VIRTUAL_MEMORY, this.operatingSystem, "committedVirtualMemorySize");		
		this.testMethod(VAR_NAME_PHYSICAL_MEMORY, this.operatingSystem, "totalPhysicalMemorySize");
		this.testMethod(VAR_NAME_SWAP_SPACE, this.operatingSystem, "totalSwapSpaceSize");
		this.testMethod(VAR_NAME_CPU_TIME, this.operatingSystem, "processCpuTime");
		this.testMethod(VAR_NAME_UPTIME, this.runtime, "uptime");
		this.testMethod(VAR_NAME_STARTTIME, this.runtime, "startTime");
	}
	
	private void testMethod(String varName, Object bean, String property) {
		try {
			String methodName = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
			Class<?> clazz = bean.getClass();
			Method method = clazz.getMethod(methodName);
			method.setAccessible(true);
			Object test = method.invoke(bean, (Object[])null);
			if(test != null) {
				BEAN_MAP.put(varName, bean);
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
		
		return this.rb.getString(name);
	}

	public StatusVariable getStatusVariable(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		StatusVariable var = null;
		try {
			Object bean = BEAN_MAP.get(name);
			Method method = METHOD_MAP.get(name);
			
			Object value = method.invoke(bean, (Object[])null);
			
			if (name.equals(VAR_NAME_SYS_LOAD)) {
				var = new StatusVariable(name,StatusVariable.CM_GAUGE,((Double)value).floatValue());
			} else if (name.equals(VAR_NAME_STARTTIME)) {
		        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
				var = new StatusVariable(name,StatusVariable.CM_SI, sdf.format(new Date(((Long)value).longValue())));
			} else if (name.equals(VAR_NAME_UPTIME)) {
				int uptime = (int) (((Long)value).longValue() / 1000); // convert value into seconds
				var = new StatusVariable(name,StatusVariable.CM_CC, uptime);
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
