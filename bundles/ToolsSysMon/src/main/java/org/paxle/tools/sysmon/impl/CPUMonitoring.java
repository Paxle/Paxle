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

import java.util.HashMap;
import java.util.ResourceBundle;

import jsysmon.CPUMonitoringData;
import jsysmon.CPUMonitoringListener;
import jsysmon.JSysmon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;

@Component(metatype=false, name=CPUMonitoring.PID)
@Service(Monitorable.class)
@Property(name="Monitorable-Localization", value=CPUMonitoring.RB_BASENAME)
public class CPUMonitoring implements CPUMonitoringListener, Monitorable {
	/**
	 * The {@link Constants#SERVICE_PID} of this {@link Monitorable}
	 */
	static final String PID = "os.usage.cpu";
	
	/**
	 * {@link ResourceBundle} basename
	 */
	static final String RB_BASENAME = "OSGI-INF/l10n/CPUMonitoring";
	
	/**
	 * XXXX CPU Usage
	 */
	private static final String VAR_PREFIX_USAGE = "cpu.usage";
	
	/**
	 * Textual description of all available {@link #VAR_NAMES monitoring-variables}
	 */
	private final ResourceBundle rb = ResourceBundle.getBundle(RB_BASENAME);
	
	/**
	 * A map containing the {@link Monitorable#getStatusVariableNames() variable-name} as key
	 * and the corresponding {@link CPUMonitoringData}-ID as value.
	 */
	private static final HashMap<String, Integer> VAR_NAMES = new HashMap<String, Integer>();
	static {
		fillNameMap(VAR_PREFIX_USAGE);
	}
	
	private static void fillNameMap(String varNamePrefix) {
		for (int varIdx=0;varIdx<CPUMonitoringData.NB_INDEXES;varIdx++) {
			String varName = null;
			switch (varIdx) {
				case CPUMonitoringData.USER_INDEX:
					varName = "user";
					break;

				case CPUMonitoringData.NICE_INDEX:
					varName = "nice";
					break;
					
				case CPUMonitoringData.SYSTEM_INDEX:
					varName = "system";
					break;	
					
				case CPUMonitoringData.IDLE_INDEX:
					varName = "idle";
					break;						
					
				case CPUMonitoringData.IRQ_INDEX:
					varName = "irq";
					break;
					
				case CPUMonitoringData.SOFT_IRQ_INDEX:
					varName = "soft.irq";
					break;					
					
				case CPUMonitoringData.STEAL_INDEX:
					varName = "steal";
					break;
					
				case CPUMonitoringData.IO_INDEX:
					varName = "io";
					break;		
					
				case CPUMonitoringData.TOTAL_INDEX:
					varName = "total";
					break;						
					
				default:
					break;
			}
			if (varName != null) {
				VAR_NAMES.put(varNamePrefix + "." + varName, Integer.valueOf(varIdx));
			}
		}
	}
	
	/**
	 * Last CPU load measurement
	 */
	private volatile CPUMonitoringData lastCPUData;
	
	/**
	 * Timestamp when the last update occurred
	 */
	private volatile long lastCPUDateTime;
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	protected void activate(ComponentContext context) {
		// starting jsysmon daemon
		JSysmon.setUpdateDelay(60000);
		JSysmon.startMonitoring();
		JSysmon.addCPUMonitoringListener(this);
	}

	protected void deactivate(ComponentContext context) throws Exception {
		// stopping jsysmon daemon
		JSysmon.stopMonitoring();
	}
	
	/**
	 * @see CPUMonitoringListener
	 */
	public void cpuMonitoringUpdate(CPUMonitoringData data) {		
		if (this.logger.isDebugEnabled()) {
			this.logger.debug(data.toString());
		}
				
		this.lastCPUData = data;
		this.lastCPUDateTime = System.currentTimeMillis();
	}

	/**
	 * @see Monitorable#getStatusVariableNames()
	 */
	public String[] getStatusVariableNames() {
		return VAR_NAMES.keySet().toArray(new String[VAR_NAMES.size()]);
	}	
	
	/**
	 * @see Monitorable#getDescription(String)
	 */
	public String getDescription(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.containsKey(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		return this.rb.getString(name);
	}

	/**
	 * @see Monitorable#getStatusVariable(String)
	 */
	public StatusVariable getStatusVariable(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.containsKey(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
				
		if (System.currentTimeMillis()-lastCPUDateTime > 1000) {
			// forcing a value refresh
			JSysmon.updateData();
		}
		
		// getting data
		Integer id = VAR_NAMES.get(name);
		double usage = 0.0;
		if (this.lastCPUData != null) {
			if (name.startsWith(VAR_PREFIX_USAGE)) {
				usage = this.lastCPUData.getTotalUsage(id.intValue());
			}
		}
		return new StatusVariable(name, StatusVariable.CM_GAUGE,(float)usage);
	}

	/**
	 * @see Monitorable#notifiesOnChange(String)
	 */
	public boolean notifiesOnChange(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.containsKey(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		return false;
	}

	/**
	 * @see Monitorable#resetStatusVariable(String)
	 */
	public boolean resetStatusVariable(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.containsKey(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		return false;
	}

}
