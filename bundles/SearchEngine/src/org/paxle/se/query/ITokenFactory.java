package org.paxle.se.query;

import org.paxle.se.query.tokens.ModToken;
import org.paxle.se.query.tokens.NotToken;
import org.paxle.se.query.tokens.Operator;
import org.paxle.se.query.tokens.PlainToken;
import org.paxle.se.query.tokens.QuoteToken;

public interface ITokenFactory {
	
	public abstract QuoteToken toQuoteToken(String str);
	public abstract PlainToken toPlainToken(String str);
	public abstract ModToken   toModToken(PlainToken token, String mod);
	public abstract NotToken   toNotToken(IToken token);
	public abstract Operator   createOrOperator();
	public abstract Operator   createAndOperator();
}
