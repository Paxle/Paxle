package org.paxle.se.index.lucene.impl;

import java.util.Iterator;

import org.apache.lucene.queryParser.QueryParser;

import org.paxle.core.doc.Field;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.query.tokens.FieldToken;
import org.paxle.se.query.tokens.NotToken;
import org.paxle.se.query.tokens.Operator;
import org.paxle.se.query.tokens.PlainToken;
import org.paxle.se.query.tokens.QuoteToken;

public class LuceneTokenFactory implements ITokenFactory {
	
	private static class LuceneOperator extends Operator {
		
		private final boolean and;
		
		public LuceneOperator(boolean and) {
			super((and) ? "AND" : "OR");
			this.and = and;
		}
		
		@Override
		public int getMinMatchCount() {
			return (this.and) ? super.children.size() : 1;
		}
		
		@Override
		public String getString() {
			final StringBuilder sb = new StringBuilder('(');
			final Iterator<AToken> it = super.children.iterator();
			while (it.hasNext()) {
				sb.append(it.next().getString());
				if (it.hasNext())
					sb.append(' ').append(super.str).append(' ');
			}
			return sb.append(')').toString();
		}
	}
	
	public Operator createAndOperator() {
		return new LuceneOperator(true);
	}
	
	public Operator createOrOperator() {
		return new LuceneOperator(false);
	}
	
	public FieldToken toFieldToken(PlainToken token, Field<?> field) {
		return new FieldToken(token, field) {
			@Override
			public String getString() {
				return super.field.getName() + ':' + super.token.getString();
			}
		};
	}
	
	public NotToken toNotToken(AToken token) {
		return new NotToken(token) {
			@Override
			public String getString() {
				return '-' + super.token.getString();
			}
		};
	}
	
	public PlainToken toPlainToken(String str) {
		return new PlainToken(str) {
			@Override
			public String getString() {
				return '+' + QueryParser.escape(super.str);
			}
		};
	}
	
	public QuoteToken toQuoteToken(String str) {
		return new QuoteToken(str) {
			@Override
			public String getString() {
				return '"' + QueryParser.escape(super.str) + '"';
			}
		};
	}
}
