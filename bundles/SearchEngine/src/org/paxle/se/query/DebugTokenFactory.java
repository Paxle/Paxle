package org.paxle.se.query;

import org.paxle.core.doc.Field;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.query.tokens.FieldToken;
import org.paxle.se.query.tokens.NotToken;
import org.paxle.se.query.tokens.Operator;
import org.paxle.se.query.tokens.PlainToken;
import org.paxle.se.query.tokens.QuoteToken;

public class DebugTokenFactory implements ITokenFactory {
	
	private class BaseOp extends Operator {
		
		private final boolean and;
		
		public BaseOp(boolean and) {
			super((and) ? "and" : "or");
			this.and = and;			
		}
		
		@Override
		public int getMinMatchCount() {
			return (this.and) ? super.children.size() : 1;
		}
		
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
	}
	
	public Operator createAndOperator() {
		return new BaseOp(true);
	}
	
	public Operator createOrOperator() {
		return new BaseOp(false);
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
