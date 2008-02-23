package org.paxle.se.query.tokens;

public abstract class NotToken extends AToken {
	
	protected final AToken token;
	
	public NotToken(AToken token) {
		this.token = token;
	}
	
	public String toString() {
		return "(" + getClass().getSimpleName() + ") '" + this.token.toString() + "'";
	}
}
