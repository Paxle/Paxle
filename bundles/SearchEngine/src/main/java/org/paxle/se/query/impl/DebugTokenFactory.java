
package org.paxle.se.query.impl;

import org.paxle.core.doc.Field;
import org.paxle.se.query.IQueryFactory;
import org.paxle.se.query.tokens.AToken;

public class DebugTokenFactory extends IQueryFactory<String> {
	
	public String and(final AToken[] children) {
		final StringBuilder sb = new StringBuilder();
		sb.append("(AndOperator) { ");
		if (children.length > 0) {
			for (AToken t : children)
				sb.append(transformToken(t, this)).append(", ");
			sb.deleteCharAt(sb.length() - 2);
		}
		return sb.append('}').toString();
	}
	
	public String or(final AToken[] children) {
		final StringBuilder sb = new StringBuilder();
		sb.append("(OrOperator) { ");
		if (children.length > 0) {
			for (AToken t : children)
				sb.append(transformToken(t, this)).append(", ");
			sb.deleteCharAt(sb.length() - 2);
		}
		return sb.append('}').toString();
	}
	
	public String field(AToken token, Field<?> field) {
		return "(FieldToken) [ Field: '" + field.getName() + "', " + transformToken(token, this) + "]";
	}
	
	public String mod(AToken token, String mod) {
		return "(ModToken) [ Mod: '" + mod + "', " + transformToken(token, this) + "]";
	}
	
	public String not(AToken token) {
		return "(NotToken) [" + transformToken(token, this) + "]";
	}
	
	public String plain(String str) {
		return "(PlainToken) [" + str + "]";
	}
	
	public String quote(String str) {
		return "(QuoteToken) [" + str + "]";
	}
}
