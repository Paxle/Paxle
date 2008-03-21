package org.paxle.filter.robots.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RuleBlock implements Serializable {
	
	private HashSet<String> agents = new HashSet<String>();
	private List<ARule> rules = new ArrayList<ARule>();	
	private HashMap<String, String> props = new HashMap<String, String>();

	void addAgent(String agent) {
		if (agent == null || agent.length() == 0) throw new IllegalArgumentException("Agent is null or empty.");
		this.agents.add(agent.trim());
	}
	
	public List<String> getAgents() {
		return Arrays.asList(this.agents.toArray(new String[this.agents.size()]));
	}
	
	void addRule(ARule rule) {
		this.rules.add(rule);
	}
	
	void addProperty(String propName, String propValue) {
		if (propName == null || propName.length() == 0) throw new IllegalArgumentException("Invalid property name.");
		this.props.put(propName, propValue);
	}
	
	/**
	 * @return the amount of different agents to which this rule this block applies 
	 */
	int agentsCount() {
		return this.agents.size();
	}
	
	public String getProperty(String propertyName) {
		return this.props.get(propertyName);
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
