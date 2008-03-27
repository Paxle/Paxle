package org.paxle.filter.robots.impl.rules;

import java.io.Serializable;

public class DisallowRule extends ARule implements Serializable {
	
	public DisallowRule(String rule) {
		super(rule);
	}

	@Override
	public Boolean isDisallowed(String path) {
		if ((path == null) || (path.length() == 0)) path = "/"; 
		if (path.startsWith(this.rule)) return Boolean.TRUE;
		return null;
	}
	
	@Override
	public String toString() {
		return "Disallow: " + this.rule;
	}
}
