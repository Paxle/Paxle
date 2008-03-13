
package org.paxle.se.query.tokens;

public class ModToken extends AToken {
	
	protected final PlainToken token;
	protected final String mod; 
	
	public ModToken(PlainToken token, String mod) {
		this.token = token;
		this.mod = mod;
	}
	
	public PlainToken getToken() {
		return this.token;
	}
	
	public String getMod() {
		return this.mod;
	}
	
	@Override
	public String toString() {
		return "(" + getClass().getSimpleName() + ") Mod: '" + this.mod + "' & " + this.token.getString();
	}
}
