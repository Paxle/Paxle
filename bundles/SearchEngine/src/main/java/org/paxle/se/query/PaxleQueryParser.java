/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
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

import java.util.LinkedList;
import java.util.List;

import org.paxle.se.query.impl.DebugTokenFactory;
import org.paxle.se.query.impl.DefaultMods;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.query.tokens.ModToken;
import org.paxle.se.query.tokens.AndOperator;
import org.paxle.se.query.tokens.NotToken;
import org.paxle.se.query.tokens.OrOperator;
import org.paxle.se.query.tokens.PlainToken;
import org.paxle.se.query.tokens.QuoteToken;

/**
 * This class provides a method to parse a paxle-query string as entered by a
 * user into an abstract token-tree.
 * 
 * @see #parse(String)
 */
public class PaxleQueryParser {
	
	/**
	 * Splits the given query-term on the 'top level' into tokens. Top level means
	 * that tokens containing whitespaces but semantically evaluate to only one token
	 * (like <code>([...] [...])</code>- or <code>"[...] [...]"</code>-statements) are
	 * being treated as only one token, they are returned including the leading and
	 * trailing braces or quotation marks.
	 *  
	 * @param  query the paxle-query to process
	 * @return an array of top level-tokens as {@link String}s in the order of appearance.
	 */
	private static String[] lex(String query) {
		final LinkedList<String> tokens = new LinkedList<String>();
		query = query.trim();
		/* last will be set to the position in query of the end the currently processed term
		 * loff is the position in query of the end of the last found term */
		int last = -1, loff;
		while (last < query.length()) {
			loff = last + 1;
			/* eat up spaces to next term */
			while (loff < query.length() && query.charAt(loff) == ' ')
				loff++;
			
			if (loff == query.length())
				break;
			
			/* set last to the end of the next term (if it starts with a brace or quotation-mark, the
			 * position is the closing brace respective quotation-mark) */
			last = findTokenEnd(query, loff);
			
			/* add the term to the found terms list */
			final String text = query.substring(loff, last).trim();
			if (text.length() > 0)
				tokens.add(text);
		}
		return tokens.toArray(new String[tokens.size()]);
	}
	
	/**
	 * Searches for the end of the next token. This method is able to recognize the
	 * following kinds of tokens:
	 * <ul>
	 *   <li>Mod-tokens: <code>prefix:token</code></li>
	 *   <li>Quote-tokens: <code>"[...]"</code>, may include whitespaces</li>
	 *   <li>Multi-tokens: <code>([...])</code>, may include whitespaces</li>
	 *   <li>"normal" tokens not including any whitespaces</li>
	 * </ul>
	 * 
	 * @param  query the string to search
	 * @param  loff the start position in <code>query</code>
	 * @return the position of the end of the found token in <code>query</code> plus
	 *         <code>1</code> or the value of <code>query.length()</code> if the token
	 *         does not end correctly
	 */
	private static int findTokenEnd(String query, int loff) {
		int t = -1, tt;
		for (int c=0; c<2; c++) {
			char first = query.charAt(loff);
			if (first == '-' || first == '+')
				loff++;
			first = query.charAt(loff);
			
			/* set t to the end of the next token
			 * specifically tests the following conditions:
			 * - first is '(' -> t = position of closing paranthesis + 1
			 * - first is '"' -> t = position of closing quotation mark + 1
			 * - next whitespace exists -> t = position of next whitespace
			 * - t = end of query */
			t = (first == '(' && (t = findMatching(query, loff)) > -1 ||
					(loff + 1 < query.length() && first == '"' && (t = findMatching(query, loff + 1)) > -1)
			) ? t + 1 : ((t = query.indexOf(' ', loff)) > -1) ? t : query.length();
			
			tt = query.indexOf(':', loff);
			if (tt < t && tt > -1) {
				/* we have to look again for the end of the token beginning after ':' as it may be a token containing whitespace */
				loff = tt + 1;
			} else {
				break;
			}
		}
		return t;
	}
	
	/**
	 * Searches for two kinds of closing symbols depending on the given start-position in
	 * <code>str</code>. If <code>start</code> points to an opening paranthesis, this method
	 * searches for the matching closing paranthesis, ignoring quotation marks. Otherwise
	 * it tries to locate the matching quotation mark considering opening and closing
	 * paranthesis.
	 * 
	 * @param  str the {@link String} to search
	 * @param  start the position to start the search from. Take the special effect of this
	 *         value in account regarding the symbol to search for as explained above.
	 * @return the position in <code>str</code> of the closing symbol (a paranthesis or
	 *         quotation mark depending on <code>start</code> as explained above) or
	 *         <code>-1</code> if <code>str</code> doesn't include the symbol behind
	 *         <code>start</code>
	 * @throws <b>IndexOutOfBoundsException</b> if <code>start</code> is greater or equal
	 *         to the length of <code>str</code>
	 */
	private static int findMatching(String str, int start) {
		final boolean bnq = (str.charAt(start) == '(');
		int bracebal = (bnq) ? 1 : 0;
		for (int i=start+1; i<str.length(); i++) {
			switch (str.charAt(i)) {
				case '(': bracebal++; break;
				case ')': bracebal--; if (bnq && bracebal == 0) return i; break;
				case '"': if (!bnq && bracebal == 0) return i; break;
			}
		}
		return -1;
	}
	
	/**
	 * Sorts the token-{@link String}s regarding operator precedence of <code>or</code>
	 * and <code>and</code>. Tokens not denoting an operator are added to a "and-list",
	 * which is flushed into an "or-list" on occurance of an <code>or</code>-token.
	 * All tokens not explicitely connected by an operator are treated as connected by
	 * <code>and</code>. The check for <code>"and"</code> and <code>"or"</code> is not
	 * case-sensitive.
	 * 
	 * @param  tokens an unprocessed list of token-{@link String}s as returned by the
	 *         {@link #lex(String)}-method
	 * @return a {@link String}-array of two dimensions width. The elements of the first
	 *         (outer) dimension contains are connected with the <code>OR</code>-operator,
	 *         whereas all elements of the second dimension are connected via <code>AND</code>.
	 */
	private static String[][] categorizeAndOrTokens(String[] tokens) {
		final List<String> andts = new LinkedList<String>();
		final List<String[]> orts = new LinkedList<String[]>();
		for (final String t : tokens) {
			if (t.equalsIgnoreCase("and")) {
				/* "and"-tokens are ignored as this is the standard case for follow-ups */
				continue;
			} else if (t.equalsIgnoreCase("or")) {
				/* "or"-tokens are the signal to flush the andts-list into an element of orts */
				if (andts.size() > 0) {
					orts.add(andts.toArray(new String[andts.size()]));
					andts.clear();
				}
				continue;
			} else {
				/* all other tokens are AND-connected */
				andts.add(t);
			}
		}
		/* add the last AND-sequence to the returned list */
		if (andts.size() > 0)
			orts.add(andts.toArray(new String[andts.size()]));
		return orts.toArray(new String[orts.size()][]);
	}
	
	/**
	 * Splits the given query-{@link String} into top-level tokens and processes these
	 * tokens regarding the connection operator. This method recurses indirectly if a
	 * token is a multi-token.
	 * <p>
	 * If the query consists only of one token, no operator-token is returned
	 * but only this single token. If <code>query</code> doesn't contain a
	 * <code>or</code>-token on the top-level, only an {@link AndOperator} is
	 * returned.
	 * 
	 * @param  query the paxle-query to process
	 * @return an {@link IToken} containing all found tokens in <code>query</code>.
	 */
	public static AToken parse(String query) {
		final String[] rts = lex(query);
		if (rts.length == 0) return null;
		if (rts.length == 1) {
			return toToken(rts[0]);
		} else {
			final String[][] aots = categorizeAndOrTokens(rts);
			if (aots.length == 0) {
				return null;
			} else if (aots.length == 1) {
				return and(aots[0]);
			} else {
				final OrOperator or = new OrOperator();
				for (String[] andts : aots)
					or.addToken(and(andts));
				
				final int cc = or.getChildCount();
				return (cc == 0) ? null : (cc == 1) ? or.children()[0] : or;
			}
		}
	}
	
	/**
	 * Connects the given token-{@link String}s with the {@link AndOperator <code>AND</code>-operator}
	 * if there are more than one elements. If <code>tokens</code> contains only element, the
	 * corresponding token is returned. This method recurses indirectly into parsing every token
	 * in the given array.
	 * 
	 * @param  tokens the tokens to <code>AND</code>-connect
	 * @return the resulting {@link IToken} or <code>null</code> if <code>tokens</code> is empty
	 *         or contains only one invalid token.
	 */
	private static AToken and(String[] tokens) {
		if (tokens.length == 0) {
			return null;
		} else if (tokens.length == 1) {
			/* only one token so we don't need to create a multi-token here */
			return toToken(tokens[0]);
		} else {
			final AndOperator and = new AndOperator();
			for (final String t : tokens)
				and.addToken(parse(t));
			
			final int cc = and.getChildCount();
			return (cc == 0) ? null : (cc == 1) ? and.children()[0] : and;
		}
	}
	
	/**
	 * Analyzes the given (trimmed) token-{@link String}, checking to following conditions:
	 * <ul>
	 *   <li>Length of <code>str</code> is smaller than 2 -&gt; returns <code>null</code></li>
	 *   <li>
	 *     <code>str</code> non case-sensitively equals "<code>and</code>" or "<code>or</code>"
	 *     -&gt; returns <code>null</code>
	 *   </li>
	 *   <li><code>"[...]"</code> -&gt; returns a {@link QuoteToken}</li>
	 *   <li>
	 *     <code>([...])</code> -&gt; parses the {@link String} between the parantheses
	 *     into tokens
	 *   </li>
	 *   <li>
	 *     <code>[...]:[...]</code> -&gt; the second part is treated as a plain token, if it
	 *     doesn't evaluate as such, the whole token denoted by <code>str</code> is returned
	 *     as a {@link PlainToken}. Otherwise a {@link ModToken} containing the first part as
	 *     modification-{@link String} and the {@link PlainToken} mentioned above is returned.
	 *   </li>
	 * </ul>
	 * If <code>str</code> doesn't match any of the above, it is returned as a {@link PlainToken}.
	 * 
	 * @param  str the {@link String} to parse into a token
	 * @return the {@link IToken} <code>str</code> has matched the conditions for
	 */
	private static AToken toToken(String str) {
		if (str.length() < 2)
			return null;
		
		/* queries or tokens with a length < 2 are not supported */
		final char first = str.charAt(0);
		final char last = str.charAt(str.length() - 1);
		if (first == '-') {
			/* the NOT operator */
			final AToken pt = toToken(str.substring(1));
			if (pt == null) {
				return null;
			} else {
				return new NotToken(pt);
			}
		} else if (first == '+') {
			/* the AND operator, this is the default, so we parse the token again without '+' */
			return toToken(str.substring(1));
		} else if (first == '"' && last == '"') {
			/* a quote-token containing a string of plain text tokens which must occur in this order in the document */
			final String cnt = str.substring(1, str.length() - 1).trim();
			if (cnt.length() == 0) {
				return null;
			} else if (cnt.indexOf(' ') == -1) {
				/* no whitespace, so we may also treat it like a plain-token */
				return new PlainToken(cnt);
			} else {
				return new QuoteToken(cnt);
			}
		} else if (first == '(' && last == ')') {
			/* will most-likely result in a multi-token */
			return parse(str.substring(1, str.length() - 1));
		}
		
		final int colon = str.indexOf(':');
		if (colon > 0 && colon < str.length() - 1) {
			/* this is a so-called 'mod-token', which includes a normal plain-token and some modifier
			 * selecting where exactly to search for this token */
			final AToken pt = toToken(str.substring(colon + 1));
			final String mod = str.substring(0, colon);
			if (pt instanceof PlainToken) {
				/* "supported" mod-token behind ':', so we treat it as a modified token which can be transformed into
				 * "normal" tokens without deeper knowledge of the search provider's language used */
				if (DefaultMods.isModSupported(mod)) {
					/* this is a modifier designated by the paxle query language */
					return DefaultMods.toToken((PlainToken)pt, mod);
					/*
				} else if (manager != null && manager.isSupported(mod)) {
					/* this is a modifier defined by an internal search plugin *//*
					return manager.getTokenFactory(mod).toToken((PlainToken)pt, mod); */
				} else {
					/* we return it as a plain token because there is no plugin around to handle this modifier */
					// return new ModToken((PlainToken)pt, mod);
					return new PlainToken(str);
				}
			} else {
				/* the user entered something like 'title:(this OR that)', so the part behind ':'
				 * is not a plain-token but a multi-token, which is not supported by the paxle query parser atm.
				 * because we would have to split the multi-token behind into multiple mod-tokens.
				 * some db-backends don't support statements like WHERE 'title' = (`this` or `that`) */
				return new PlainToken(str);
			}
		} else if (str.equalsIgnoreCase("and") || str.equalsIgnoreCase("or")) {
			/* operators in search queries with multiple tokens should have been removed already
			 * if this is a search query with only a single token we can not do much with an operator ;) */
			return null;
		} else {
			return new PlainToken(str);
		}
	}
	
	public static void main(String[] args) {
		final String sb = "blubb -\"bla blubb\" +author:\"dies hier\" (ist or hat or \"denkt sich\" and so) ein text -mit (\"leeren zeichen\" or title:leerzeichen) \"ne?!  \"";
		//                 0123456 7890123456 7890123456 7890123456 78901234567890123 45678901234 5678901234567890123456789 012345678901234 567890123456789012345678 9012345 6
		//                 0          1          2          3          4         5          6          7         8          9         0          1         2          3
		//System.out.println(findMatching(sb, 24));
		//System.out.println(findMatching(sb, 6));
		
		System.out.println(IQueryFactory.transformToken(parse(sb), new DebugTokenFactory()));
		System.out.println(IQueryFactory.transformToken(parse(" b la"), new DebugTokenFactory()));
		
		/*
		String[] ss = lex(sb);
		int c = 0;
		for (String s : ss) {
			//IToken t = pqp.toToken(s);
			System.out.println(c++ + ": " + s);
		}
		*/
		//PaxleQueryParser pqp = new PaxleQueryParser(new TF());
		//final Expression sse = pqp.parse(sb);
		
		//System.out.println(sse.toString());
	}
}
