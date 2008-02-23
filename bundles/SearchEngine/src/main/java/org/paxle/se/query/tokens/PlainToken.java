package org.paxle.se.query.tokens;

public abstract class PlainToken extends AToken {
	
	protected final String str;
	
	public PlainToken(String str) {
		this.str = str;
	}
	
	public String toString() {
		return "(" + getClass().getSimpleName() + ") '" + this.str + "'";
	}
}
