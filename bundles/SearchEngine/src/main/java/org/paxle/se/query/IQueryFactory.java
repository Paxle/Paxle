/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
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
	
	/**
	 * This methods transforms a given {@link AToken}-tree into a representation of the generic type of
	 * this {@link IQueryFactory}. In the default implementation this method first calls
	 * {@link #beginTransformation()}, then invokes the &quot;real&quot; transformation process by calling
	 * {@link #transformToken(AToken, IQueryFactory)} using this {@link IQueryFactory}, finally calls
	 * {@link #endTransformation()} and returns the obtained transformation. 
	 * @param token the {@link AToken}-tree to transform
	 * @return the transformation of the given {@link AToken}
	 */
	public R transformToken(final AToken token) {
		beginTransformation();
		final R r = transformToken(token, this);
		endTransformation();
		return r;
	}
	
	/**
	 * A static convenience method which calls the methods dealing with transformation of the
	 * standard {@link AToken}s as defined in {@link org.paxle.se.query.tokens}. For the following
	 * types of {@link AToken}s the mentioned methods are called:
	 * <ul>
	 *   <li>for {@link AndOperator}-tokens: {@link #and(AToken[])} is being called, passing all
	 *       {@link AndOperator#children() children} of the operator</li>
	 *   <li>for {@link OrOperator}-tokens: {@link #or(AToken[])} is being called, passing all
	 *       {@link AndOperator#children() children} of the operator</li>
	 *   <li>for {@link ModToken}s: {@link #mod(AToken, String)} is being called with the contained
	 *       {@link AToken} and {@link ModToken#getMod() modifier} string</li>
	 *   <li>for {@link FieldToken}s: {@link #field(AToken, Field)} is being called with the contained
	 *       {@link AToken} and {@link FieldToken#getField() field}</li>
	 *   <li>for {@link QuoteToken}s: {@link #quote(String)} is being called with the contained {@link String}</li>
	 *   <li>for {@link PlainToken}s: {@link #plain(String)} is being called with the contained {@link String}</li>
	 *   <li>for {@link NotToken}s: {@link #not(AToken)} is being called with the contained {@link AToken}</li>
	 * </ul>
	 * <p>
	 * In any other case which is not covered by the list above, {@link #transformNonDefaultToken(AToken)} is being
	 * called, which - in the default implementation - results in a {@link RuntimeException} being thrown to indicate
	 * that the {@link AToken}-type is not supported by this {@link IQueryFactory}.
	 * @param <R> the result type into which the {@link AToken}-tree is transformed by the given {@link IQueryFactory}
	 * @param token the root {@link AToken} to be transformed inclusively all of it's possible children
	 * @param factory the {@link IQueryFactory} to use for the transformation process
	 * @return the result of the transformation
	 */
	protected static <R> R transformToken(final AToken token, final IQueryFactory<R> factory) {
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
			return factory.transformNonDefaultToken(token);
		}
	}
	
	/**
	 * This method shall be overridden by implementations to deal with non-standard {@link AToken}s.
	 * The default implementation throws a {@link RuntimeException}.
	 * @param token the non-standard {@link AToken} to transform
	 * @return the result of the transformation
	 */
	protected R transformNonDefaultToken(final AToken token) {
		throw new RuntimeException("unknown token-type: " + token + " (" + token.getClass() + ")");
	}
	
	/**
	 * Called by the publicly accessible {@link #transformToken(AToken)}-method prior to the start of
	 * the transformation process.
	 * <p>
	 * This method can be overridden by implementations for initialization purposes. The default
	 * implementation does nothing.
	 * @see #transformToken(AToken)
	 * @see #transformTokenImpl(AToken)
	 */
	public void beginTransformation() {
		// NOOP
	}
	
	/**
	 * Called by the publicly accessible {@link #transformToken(AToken)}-method after the transformation
	 * process has ended.
	 * <p>
	 * This method can be overridden by implementations for cleanup purposes. The default implementation
	 * does nothing.
	 * @see #transformToken(AToken)
	 * @see #transformTokenImpl(AToken)
	 */
	public void endTransformation() {
		// NOOP
	}
	
	public abstract R and(AToken[] token);
	public abstract R or(AToken[] token);
	public abstract R not(AToken token);
	public abstract R plain(String str);
	public abstract R quote(String str);
	public abstract R field(AToken token, Field<?> field);
	public abstract R mod(AToken token, String mod);
}
