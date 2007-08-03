package org.paxle.se.query.tokens;

import org.paxle.se.query.IToken;

public class PlainToken implements IToken {
	
	protected final String str;
	
	public PlainToken(String str) {
		this.str = str;
	}
	
	public final String getString() {
		return this.str;
	}
	
	public String toString() {
		return "(" + getClass().getSimpleName() + ") '" + this.str + "'";
	}
}
