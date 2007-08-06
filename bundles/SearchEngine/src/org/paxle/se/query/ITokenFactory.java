package org.paxle.se.query;

import org.paxle.core.doc.Field;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.query.tokens.FieldToken;
import org.paxle.se.query.tokens.NotToken;
import org.paxle.se.query.tokens.AndOperator;
import org.paxle.se.query.tokens.OrOperator;
import org.paxle.se.query.tokens.PlainToken;
import org.paxle.se.query.tokens.QuoteToken;

public interface ITokenFactory {

	public abstract FieldToken  toFieldToken(PlainToken token, Field<?> field);
	public abstract NotToken    toNotToken(AToken token);
	public abstract PlainToken  toPlainToken(String str);
	public abstract QuoteToken  toQuoteToken(String str);
	public abstract OrOperator  createOrOperator();
	public abstract AndOperator createAndOperator();
}
