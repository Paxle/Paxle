
package org.paxle.se.query;

import org.paxle.core.doc.Field;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.query.tokens.AndOperator;
import org.paxle.se.query.tokens.FieldToken;
import org.paxle.se.query.tokens.ModToken;
import org.paxle.se.query.tokens.NotToken;
import org.paxle.se.query.tokens.OrOperator;
import org.paxle.se.query.tokens.PlainToken;
import org.paxle.se.query.tokens.QuoteToken;

public abstract class IQueryFactory<R> {
	
	public static <R> R transformToken(final AToken token, final IQueryFactory<R> factory) {
		if (token instanceof AndOperator) {
			final AndOperator op = (AndOperator)token;
			return factory.and(op.children());
			
		} else if (token instanceof OrOperator) {
			final OrOperator op = (OrOperator)token;
			return factory.or(op.children());
			
		} else if (token instanceof ModToken) {
			final ModToken mtoken = (ModToken)token;
			return factory.mod(mtoken.getToken(), mtoken.getMod());
			
		} else if (token instanceof FieldToken) {
			final FieldToken ftoken = (FieldToken)token;
			return factory.field(ftoken.getToken(), ftoken.getField());
			
		} else if (token instanceof QuoteToken) {
			return factory.quote(((QuoteToken)token).getString());
			
		} else if (token instanceof PlainToken) {
			return factory.plain(((PlainToken)token).getString());
			
		} else if (token instanceof NotToken) {
			return factory.not(((NotToken)token).getToken());
			
		} else {
			throw new RuntimeException("unknown token-type: " + token + " (" + token.getClass() + ")");
		}
	}
	
	public abstract R and(AToken[] token);
	public abstract R or(AToken[] token);
	public abstract R not(AToken token);
	public abstract R plain(String str);
	public abstract R quote(String str);
	public abstract R field(AToken token, Field<?> field);
	public abstract R mod(AToken token, String mod);
}
