
package org.paxle.se.index.lucene.impl;

import org.apache.lucene.queryParser.QueryParser;

import org.paxle.core.doc.Field;
import org.paxle.se.query.IQueryFactory;
import org.paxle.se.query.tokens.AToken;

public class LuceneQueryFactory extends IQueryFactory<String> {
	
	private String getOperatorString(AToken[] children, String str) {
		final StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (int i=0; i<children.length; i++) {
			sb.append(transformToken(children[i], this));
			if (i + 1 < children.length)
				sb.append(' ').append(str).append(' ');
		}
		return sb.append(')').toString();
	}
	
	@Override
	public String and(AToken[] token) {
		return getOperatorString(token, "AND");
	}
	
	@Override
	public String field(AToken token, Field<?> field) {
		return field.getName() + ':' + transformToken(token, this);
	}
	
	@Override
	public String mod(AToken token, String mod) {
		return null;
	}
	
	@Override
	public String not(AToken token) {
		return '-' + transformToken(token, this);
	}
	
	@Override
	public String or(AToken[] token) {
		return getOperatorString(token, "OR");
	}
	
	@Override
	public String plain(String str) {
		return QueryParser.escape(str);
	}
	
	@Override
	public String quote(String str) {
		return '"' + QueryParser.escape(str) + '"';
	}
}
