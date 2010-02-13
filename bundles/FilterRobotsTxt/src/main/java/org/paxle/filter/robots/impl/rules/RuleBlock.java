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

package org.paxle.filter.robots.impl.rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class RuleBlock implements Serializable {
	
	private HashSet<String> agents = new HashSet<String>();
	private List<ARule> rules = new ArrayList<ARule>();	
	private HashMap<String, String> props = new HashMap<String, String>();

	public void addAgent(String agent) {
		if (agent == null || agent.length() == 0) throw new IllegalArgumentException("Agent is null or empty.");
		this.agents.add(agent.trim());
	}
	
	public List<String> getAgents() {
		return Arrays.asList(this.agents.toArray(new String[this.agents.size()]));
	}
	
	public void addRule(ARule rule) {
		this.rules.add(rule);
	}
	
	public void addProperty(String propName, String propValue) {
		if (propName == null || propName.length() == 0) throw new IllegalArgumentException("Invalid property name.");
		this.props.put(propName, propValue);
	}
	
	/**
	 * @return the amount of different agents to which this rule this block applies 
	 */
	public int agentsCount() {
		return this.agents.size();
	}
	
	public String getProperty(String propertyName) {
		return this.props.get(propertyName);
	}
	
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(this.props);
	}
	
	public Boolean isDisallowed(String agent, String path) {
		if (this.rules == null || this.rules.size() == 0) return null;
		if (!this.agents.contains(agent) && !this.agents.contains("*")) return null;
		
		for (ARule rule : this.rules) {
			Boolean isDisallowed = rule.isDisallowed(path);
			if (isDisallowed != null) return isDisallowed;
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		// append agents
		for (String agent : agents) {
			str.append("User-agent: " ).append(agent).append("\r\n");			
		}
		
		// append optional parameters
		for (String prop : this.props.keySet()) {
			str.append(prop).append(": ").append(this.props.get(prop)).append("\r\n");			
		}
		
		// append rules
		if (this.rules.size() > 0) {
			for (ARule rule : rules) {
				str.append(rule).append("\r\n");
			}
		} else {			
			str.append("Disallow:\r\n");
		}
		
		return str.toString();
	}
}
