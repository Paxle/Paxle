package org.paxle.se.query.impl;

import org.paxle.core.doc.Field;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.query.tokens.FieldToken;
import org.paxle.se.query.tokens.NotToken;
import org.paxle.se.query.tokens.AndOperator;
import org.paxle.se.query.tokens.OrOperator;
import org.paxle.se.query.tokens.PlainToken;
import org.paxle.se.query.tokens.QuoteToken;

public class DebugTokenFactory implements ITokenFactory {
	
	public AndOperator createAndOperator() {
		return new AndOperator() {
			@Override
			public String getString() {
				final StringBuilder sb = new StringBuilder();
				sb.append('(').append(Character.toUpperCase(super.str.charAt(0))).append(super.str.substring(1)).append("Operator) { ");
				if (this.children.size() > 0) {
					for (AToken t : this.children)
						sb.append(t.getString()).append(", ");
					sb.deleteCharAt(sb.length() - 2);
				}
				return sb.append('}').toString();
			}
		};
	}
	
	public OrOperator createOrOperator() {
		return new OrOperator() {
			@Override
			public String getString() {
				final StringBuilder sb = new StringBuilder();
				sb.append('(').append(Character.toUpperCase(super.str.charAt(0))).append(super.str.substring(1)).append("Operator) { ");
				if (this.children.size() > 0) {
					for (AToken t : this.children)
						sb.append(t.getString()).append(", ");
					sb.deleteCharAt(sb.length() - 2);
				}
				return sb.append('}').toString();
			}
		};
	}
	
	public FieldToken toFieldToken(PlainToken token, Field<?> field) {
		return new FieldToken(token, field) {
			@Override
			public String getString() {
				return "(FieldToken) [ Field: '" + super.field.getName() + "', " + super.token.getString() + "]";
			}
		};
	}
	
	public NotToken toNotToken(AToken token) {
		return new NotToken(token) {
			@Override
			public String getString() {
				return "(NotToken) [" + this.token.getString() + "]";
			}
		};
	}
	
	public PlainToken toPlainToken(String str) {
		return new PlainToken(str) {
			@Override
			public String getString() {
				return "(PlainToken) [" + super.str + "]";
			}
		};
	}
	
	public QuoteToken toQuoteToken(String str) {
		return new QuoteToken(str) {
			@Override
			public String getString() {
				return "(QuoteToken) [" + super.str + "]";
			}
		};
	}
}
