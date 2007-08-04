package org.paxle.se.index.lucene.impl;

import java.util.Iterator;

import org.paxle.se.query.IToken;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.query.tokens.ModToken;
import org.paxle.se.query.tokens.Operator;
import org.paxle.se.query.tokens.PlainToken;
import org.paxle.se.query.tokens.QuoteToken;

public class LuceneTokenFactory implements ITokenFactory {
	
	private static class LuceneOperator extends Operator {
		
		public LuceneOperator(String concat) {
			super(concat);
		}
		
		@Override
		public String getString() {
			final StringBuilder sb = new StringBuilder('(');
			final Iterator<IToken> it = super.children.iterator();
			while (it.hasNext()) {
				sb.append(it.next().getString());
				if (it.hasNext())
					sb.append(' ').append(super.str).append(' ');
			}
			return sb.append(')').toString();
		}
	}
	
	private static final String[] SPECIALS = {
		"\\", "+", "-", "&&", "||", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":" 
	};
	
	private static String escape(String str) {
		for (final String special : SPECIALS)
			str = str.replace(special, '\\' + special);
		return str;
	}
	
	public Operator createAndOperator() {
		return new LuceneOperator("AND");
	}
	
	public Operator createOrOperator() {
		return new LuceneOperator("OR");
	}
	
	public ModToken toModToken(PlainToken token, String mod) {
		return new ModToken(token, escape(mod));
	}
	
	public PlainToken toPlainToken(String str) {
		return new PlainToken(str) {
			@Override
			public String getString() {
				return '+' + escape(super.str);
			}
		};
	}
	
	public QuoteToken toQuoteToken(String str) {
		return new QuoteToken(str) {
			@Override
			public String getString() {
				return '"' + escape(super.str) + '"';
			}
		};
	}
}
