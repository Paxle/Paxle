package org.paxle.filter.robots.impl;

import java.io.Serializable;


public abstract class ARule implements Serializable {
	protected String rule = null; 
	
	protected ARule(String rule) {
		this.rule = rule;
	}
	
	public abstract Boolean isDisallowed(String path);
}
