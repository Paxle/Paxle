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
package org.paxle.core.monitorable.observer.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.monitor.MonitorAdmin;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.MonitoringJob;
import org.osgi.service.monitor.StatusVariable;
import org.paxle.core.monitorable.observer.IObserverCondition;
import org.paxle.core.monitorable.observer.IObserverRule;

public class MonitorableObserver implements EventHandler, ServiceListener {
	/**
	 * The current state of this observer. The key of this {@link Dictionary} is the fullpath
	 * of a {@link StatusVariable}, the value is the last received value
	 * of this variable.
	 */
	private Hashtable<String, Object> currentState = new Hashtable<String, Object>();
	
	/**
	 * A map containing the fullpath of a {@link StatusVariable} as key and the it's 
	 * {@link StatusVariable#getType() type} as {@link Integer} 
	 * 
	 * This list is required by {@link #handleEvent(Event)} to parse the received value
	 * 
	 * @see #handleEvent(Event)
	 */
	private Map<String,Integer> typeList = new HashMap<String, Integer>();
	
	/**
	 * The list of {@link StatusVariable variables} monitored by this observer
	 */
	private Map<String,Set<String>> variableTree;
	
	/**
	 * The Observer rules to use
	 */
	private ArrayList<IObserverRule> rules;
	
	/**
	 * Currently active monitoring job
	 */
	private MonitoringJob currentMonitorJob;	
	
	/**
	 * OSGi bundle context
	 */
	private BundleContext bc;
	
	/**
	 * OSGi Monitorable-admin service
	 */
	private MonitorAdmin monitorAdmin;
	
	/**
	 * For logging
	 */
	protected Log logger = LogFactory.getLog(this.getClass());
	
	public MonitorableObserver(BundleContext bc, IObserverRule... observerRules) throws InvalidSyntaxException {
		ServiceReference monitorAdminRef = bc.getServiceReference(MonitorAdmin.class.getName());
		MonitorAdmin ma = (MonitorAdmin) bc.getService(monitorAdminRef);
		
		this.init(bc, ma, observerRules);
	}
	
	MonitorableObserver(BundleContext bc, MonitorAdmin monitorAdmin, IObserverRule... observerRules) throws InvalidSyntaxException {
		this.init(bc, monitorAdmin, observerRules);
	}
	
	/**
	 * @param filter an ldap filter expression that must be matched by monitorable variables
	 * @throws InvalidSyntaxException 
	 */
	private void init(BundleContext bc, MonitorAdmin monitorAdmin, IObserverRule... observerRules) throws InvalidSyntaxException {
		if (bc == null) throw new NullPointerException("The bundle-context is null");
		if (monitorAdmin == null) throw new NullPointerException("The monitor-admin service is null");
		if (observerRules == null) throw new NullPointerException("The filter expression is null");
		
		this.bc = bc;
		this.monitorAdmin = monitorAdmin;
		this.rules = new ArrayList<IObserverRule>(Arrays.asList(observerRules));				
		
		// extracting monitorable variables		
		this.variableTree = extractMonitorables(observerRules);
		
		// registering this observer as event-handler: required to receive monitoring-events
		Dictionary<String,Object> properties = new Hashtable<String,Object>();
		properties.put(EventConstants.EVENT_TOPIC, new String[]{"org/osgi/service/monitor"});
		properties.put(EventConstants.EVENT_FILTER, String.format("(mon.listener.id=%s)",this.getObserverID()));
		this.bc.registerService(EventHandler.class.getName(), this, properties);				
		
		// detecting already registered monitorables and
		// determine which of their variables we need to monitor 
		final HashSet<String> variableNames = new HashSet<String>();
		final ServiceReference[] services = bc.getServiceReferences(Monitorable.class.getName(), null);
		if (services != null) {
			for (ServiceReference reference : services) {
				this.addVariables4Monitor(reference, variableNames, true);
			}
			this.startScheduledJob(variableNames);
		}
		
		// registering this class as service-listener
		this.bc.addServiceListener(this, String.format("(%s=%s)",Constants.OBJECTCLASS, Monitorable.class.getName()));	
	}
	
	static Map<String,Set<String>> extractMonitorables(IObserverRule... observerRules) {
		final Map<String,Set<String>> variables = new HashMap<String,Set<String>>();
		
		for (IObserverRule rule : observerRules) {
			IObserverCondition condition = rule.getCondition();
			List<String> variableIDs = condition.getMonitorableVariableIDs();
			for (String variableID : variableIDs) {
				int idx = variableID.indexOf('/');
				String monitorableId = variableID.substring(0, idx);
				String variableId = variableID.substring(idx+1);
				
				Set<String> variableIds;
				if (variables.containsKey(monitorableId)) {
					variableIds = variables.get(monitorableId);
				} else {
					variableIds = new HashSet<String>();
					variables.put(monitorableId, variableIds);
				}				
				variableIds.add(variableId);
			}
		}
		
		return variables;
	}


	/**
	 * @see EventHandler#handleEvent(Event)
	 */
	public void handleEvent(Event event) {
		try {
			// getting the variable full-name
			String pid = (String) event.getProperty("mon.monitorable.pid");
			String name = (String) event.getProperty("mon.statusvariable.name");
			final String fullPath = pid + "/" + name;

			// getting the variable value as string
			String valueStr = (String) event.getProperty("mon.statusvariable.value");

			// updating ovserver state
			this.updateState(fullPath, valueStr);

			// testing if one filter matches current state
			for (IObserverRule rule : this.rules) {
				if (rule.match(this.currentState)) {
					this.logger.info(String.format(
							"Matching rule found: %s\r\nCurrent state: %s",
							rule.toString(),
							this.currentState.toString()
					));
					rule.triggerAction(this.currentState);				
				}
			}
		} catch (Throwable e) {
			this.logger.error(String.format(
					"Unexpected '%s' while processing event '%s'.",
					e.getClass().getName(),
					event
			));
		}
	}
	
	/**
	 * Converts the given {@link StatusVariable} value into a proper type and
	 * updates the internal {@link #currentState state-map}
	 * @param fullPath the full path of the {@link StatusVariable}
	 * @param valueStr the current value of the {@link StatusVariable} as string
	 */
	private void updateState(String fullPath, String valueStr) {
		Object value = null;
		Integer type = this.typeList.get(fullPath);
		if (type != null) {
			switch (type.intValue()) {
				case StatusVariable.TYPE_FLOAT:					
					value = Float.valueOf(valueStr);
					break;
				
				case StatusVariable.TYPE_INTEGER:
					value = Integer.valueOf(valueStr);
					break;							
					
				case StatusVariable.TYPE_BOOLEAN:
					value = Boolean.valueOf(valueStr);
					break;
					
				case StatusVariable.TYPE_STRING:
					value = valueStr;
					break;
					
				default:
					break;
			}
		}
		
		// removing old value
		currentState.remove(fullPath);
		
		if (value != null) {
			currentState.put(fullPath, value);
		}
	}

	/**
	 * @see ServiceListener#serviceChanged(ServiceEvent)
	 */
	public void serviceChanged(ServiceEvent event) {
		// getting the service reference
		ServiceReference reference = event.getServiceReference();
		this.serviceChanged(reference, event.getType());
	}		
	
	/**
	 * @param reference
	 * @param eventType
	 */
	private void serviceChanged(ServiceReference reference, int eventType) {
		if (reference == null) return;
		if (eventType == ServiceEvent.MODIFIED) return;
		
		// ignoring unknown services
		String pid = (String) reference.getProperty(Constants.SERVICE_PID);
		if (!variableTree.containsKey(pid)) return;
		
		// getting currently monitored variables
		final HashSet<String> currentVariableNames = new HashSet<String>();
		if (this.currentMonitorJob != null) {
			String[] temp = this.currentMonitorJob.getStatusVariableNames();
			if (temp != null) currentVariableNames.addAll(Arrays.asList(temp));
			
			// stopping old monitoring-job
			this.currentMonitorJob.stop();
			this.currentMonitorJob = null;
		}		
		
		// getting variables of changed service
		final HashSet<String> diffVariableNames = new HashSet<String>();
		this.addVariables4Monitor(
				reference, 
				diffVariableNames, 
				eventType == ServiceEvent.REGISTERED
		);
		
		if (eventType == ServiceEvent.REGISTERED) {			
			// adding new variable
			currentVariableNames.addAll(diffVariableNames);
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			currentVariableNames.removeAll(diffVariableNames);
			
			// deleting old values from status map
			for (String varName : diffVariableNames) {
				this.currentState.remove(varName);
			}
		}
		
		// restarting monitoring job
		this.startScheduledJob(currentVariableNames);
	}
	
	/**
	 * Add the full-path of all {@link StatusVariable variables} of the given {@link Monitorable} into the set
	 * @param reference a reference to a {@link Monitorable}
	 * @param variableNames the set where the variable-names should be appended
	 * @param determineType if <code>true</code> the type of the {@link StatusVariable} is determined via call to
	 * 					    {@link StatusVariable#getType()} and stored into {@link #typeList}
	 */
	private void addVariables4Monitor(ServiceReference reference, HashSet<String> variableNames, boolean determineType) {
		String pid = (String) reference.getProperty(Constants.SERVICE_PID);
		if (!variableTree.containsKey(pid)) return;
		
		for (String name : variableTree.get(pid)) {
			final String fullPath = pid + "/" + name;
			
			// append full-path
			variableNames.add(fullPath);
			
			// getting the type of the status-variable
			if (determineType) {
				try {
					Monitorable mon = (Monitorable) this.bc.getService(reference);
					StatusVariable var = mon.getStatusVariable(name);	
					Integer type = Integer.valueOf(var.getType());
					
					this.typeList.put(fullPath, type);
				} catch (Exception e) {
					this.logger.error(String.format(
							"Unexpected '%s' while trying to determine type of statusvariable '%s'.",
							e.getClass().getName(),
							fullPath
					), e);
				}
			}
		}
	}
	
	/**
	 * Starting a new monitoring job with the given variables to monitor
	 * @param variableNames full-path of the {@link StatusVariable variables} to monitor
	 */
	private void startScheduledJob(Set<String> variableNames) {
		if (variableNames.size() == 0) return;
		this.currentMonitorJob = this.monitorAdmin.startScheduledJob(
				this.getObserverID(), // listener.id
				variableNames.toArray(new String[variableNames.size()]),
				5, // seconds
				0  // Forever
		);		
	}
	
	private String getObserverID() {
		return this.getClass().getSimpleName() + "_" + this.hashCode();
	}
}
