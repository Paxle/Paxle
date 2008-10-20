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

package org.paxle.se.query.impl;

import org.paxle.core.doc.Field;
import org.paxle.se.query.IQueryFactory;
import org.paxle.se.query.tokens.AToken;

public class DebugTokenFactory extends IQueryFactory<String> {
	
	@Override
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
	
	@Override
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
	
	@Override
	public String field(AToken token, Field<?> field) {
		return "(FieldToken) [ Field: '" + field.getName() + "', " + transformToken(token, this) + "]";
	}
	
	@Override
	public String mod(AToken token, String mod) {
		return "(ModToken) [ Mod: '" + mod + "', " + transformToken(token, this) + "]";
	}
	
	@Override
	public String not(AToken token) {
		return "(NotToken) [" + transformToken(token, this) + "]";
	}
	
	@Override
	public String plain(String str) {
		return "(PlainToken) [" + str + "]";
	}
	
	@Override
	public String quote(String str) {
		return "(QuoteToken) [" + str + "]";
	}
}
