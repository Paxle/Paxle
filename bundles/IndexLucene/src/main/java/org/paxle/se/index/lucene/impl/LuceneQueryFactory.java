/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

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
