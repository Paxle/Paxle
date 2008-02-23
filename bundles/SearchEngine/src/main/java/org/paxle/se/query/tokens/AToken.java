package org.paxle.se.query.tokens;

public abstract class AToken {
	
	protected MultiToken parent = null;
	
	protected void setParent(MultiToken token) {
		this.parent = token;
	}
	
	public MultiToken getParent() {
		return this.parent;
	}
	
	public abstract String getString();
}
