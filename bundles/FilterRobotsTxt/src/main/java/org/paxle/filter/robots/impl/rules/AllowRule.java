package org.paxle.filter.robots.impl.rules;

import java.io.Serializable;

public class AllowRule extends ARule implements Serializable {

	public AllowRule(String rule) {
		super(rule);
	}

	@Override
	public Boolean isDisallowed(String path) {
		if ((path == null) || (path.length() == 0)) path = "/"; 
		if (path.startsWith(this.rule)) return Boolean.FALSE;
		return null;
	}

	@Override
	public String toString() {
		return "Allow: " + this.rule;
	}
}