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
