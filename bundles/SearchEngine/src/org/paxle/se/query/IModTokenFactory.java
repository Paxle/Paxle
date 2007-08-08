package org.paxle.se.query;

import org.paxle.se.query.tokens.AToken;
import org.paxle.se.query.tokens.PlainToken;

public interface IModTokenFactory extends ITokenFactory {
	
	public abstract boolean     isModSupported(String mod);
	public abstract AToken      toToken(PlainToken token, String modifier);
}
