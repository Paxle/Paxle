package org.paxle.se.query.tokens;

import org.paxle.se.query.IToken;

public class NotToken implements IToken {
	
	protected final IToken token;
	
	public NotToken(IToken token) {
		this.token = token;
	}
	
	public String getString() {
		return '-' + this.token.getString();
	}
	
	public String toString() {
		return "(" + this.getClass().getSimpleName() + ") '" + this.token.toString() + "'";
	}
}
