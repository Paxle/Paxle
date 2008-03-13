
package org.paxle.se.query.tokens;

abstract class WrapperToken extends AToken {
	
	protected final AToken token;
	
	public WrapperToken(final AToken token) {
		this.token = token;
	}
	
	public AToken getToken() {
		return token;
	}
	
	@Override
	public String toString() {
		return "(" + getClass().getSimpleName() + ") '" + this.token.toString() + "'";
	}
}
