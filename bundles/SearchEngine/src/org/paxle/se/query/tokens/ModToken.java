package org.paxle.se.query.tokens;

import org.paxle.se.query.IToken;

public class ModToken implements IToken {
	
	protected final IToken token;
	protected final String mod; 
	
	public ModToken(PlainToken token, String mod) {
		this.token = token;
		this.mod = mod;
	}
	
	public IToken getToken() {
		return this.token;
	}
	
	public String getMod() {
		return this.mod;
	}
	
	public String getString() {
		return this.mod + ':' + this.token.getString();
	}
	
	public String toString() {
		return "(" + getClass().getSimpleName() + ") Mod: '" + this.mod + "' & " + this.token.toString();
	}
}
