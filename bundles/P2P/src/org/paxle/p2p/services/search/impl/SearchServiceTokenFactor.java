package org.paxle.p2p.services.search.impl;

import java.util.Collection;
import java.util.Iterator;

import org.paxle.core.doc.Field;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.query.tokens.AndOperator;
import org.paxle.se.query.tokens.FieldToken;
import org.paxle.se.query.tokens.NotToken;
import org.paxle.se.query.tokens.OrOperator;
import org.paxle.se.query.tokens.PlainToken;
import org.paxle.se.query.tokens.QuoteToken;

public class SearchServiceTokenFactor implements ITokenFactory {
	private static String getOperatorString(Collection<AToken> children, String str) {
		final StringBuilder sb = new StringBuilder();
		sb.append('(');
		final Iterator<AToken> it = children.iterator();
		while (it.hasNext()) {
			sb.append(it.next().getString());
			if (it.hasNext())
				sb.append(' ').append(str).append(' ');
		}
		return sb.append(')').toString();
	}
	
	public AndOperator createAndOperator() {
		return new AndOperator() {
			@Override
			public String getString() {
				return SearchServiceTokenFactor.getOperatorString(super.children, "AND");
			}
		};
	}
	
	public OrOperator createOrOperator() {
		return new OrOperator() {
			@Override
			public String getString() {
				return SearchServiceTokenFactor.getOperatorString(super.children, "OR");
			}
		};
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
				return super.str;
			}
		};
	}
	
	public QuoteToken toQuoteToken(String str) {
		return new QuoteToken(str) {
			@Override
			public String getString() {
				return '"' + super.str + '"';
			}
		};
	}
}

