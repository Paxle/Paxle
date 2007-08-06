package org.paxle.se.query.tokens;

public class ModToken extends AToken {
	
	protected final AToken token;
	protected final String mod; 
	
	public ModToken(PlainToken token, String mod) {
		this.token = token;
		this.mod = mod;
	}
	
	public AToken getToken() {
		return this.token;
	}
	
	public String getMod() {
		return this.mod;
	}
	
	public String getString() {
		//throw new RuntimeException("Not supported");
		return "(" + getClass().getSimpleName() + ") Mod: '" + this.mod + "' & " + this.token.getString();
	}
}
