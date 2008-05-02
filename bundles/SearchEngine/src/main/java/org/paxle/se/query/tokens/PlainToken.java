
package org.paxle.se.query.tokens;

public class PlainToken extends AToken {
	
	protected final String str;
	
	public PlainToken(String str) {
		this.str = str;
	}
	
	public String getString() {
		return str;
	}
	
	@Override
	public String toString() {
		return "(" + getClass().getSimpleName() + ") '" + this.str + "'";
	}
}
