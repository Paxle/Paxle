package org.paxle.se.query;

import java.util.LinkedList;
import java.util.List;

import org.paxle.se.query.impl.DebugTokenFactory;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.query.tokens.ModToken;
import org.paxle.se.query.tokens.AndOperator;
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
			t = (first == '(' && (t = findMatching(query, loff)) > -1 ||
					(loff + 1 < query.length() && first == '"' && (t = findMatching(query, loff + 1)) > -1)
			) ? t + 1 : ((t = query.indexOf(' ', loff)) > -1) ? t : query.length();
			
			tt = query.indexOf(':', loff);
			first = query.charAt(loff);
			if (tt < t && tt > -1) {
				loff = tt + 1;
			} else if (first == '-' || first == '+') {
				loff++;
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
		// add the last AND-sequence to the returned list
		if (andts.size() > 0)
			orts.add(andts.toArray(new String[andts.size()]));
		return orts.toArray(new String[orts.size()][]);
	}
	
	private ITokenFactory factory;
	
	public PaxleQueryParser() {
		this.factory = null;
	}
	
	public PaxleQueryParser(ITokenFactory factory) {
		this.factory = factory;
	}
	
	public ITokenFactory getTokenFactory() {
		return this.factory;
	}
	
	public void setTokenFactory(ITokenFactory factory) {
		this.factory = factory;
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
	public AToken parse(String query) {
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
				final OrOperator or = this.factory.createOrOperator();
				for (String[] andts : aots)
					or.addToken(and(andts));
				return or;
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
	private AToken and(String[] tokens) {
		if (tokens.length == 0) {
			return null;
		} else if (tokens.length == 1) {
			return toToken(tokens[0]);
		} else {
			final AndOperator and = this.factory.createAndOperator();
			for (final String t : tokens)
				and.addToken(parse(t));
			return and;
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
	private AToken toToken(String str) {
		if (str.length() > 1) {
			final char first = str.charAt(0);
			final char last = str.charAt(str.length() - 1);
			if (first == '-') {
				final AToken pt = toToken(str.substring(1));
				if (pt == null) {
					return null;
				} else {
					return this.factory.toNotToken(pt);
				}
			} else if (first == '+') {
				return toToken(str.substring(1));
			} else if (first == '"' && last == '"') {
				final String cnt = str.substring(1, str.length() - 1).trim();
				if (cnt.length() == 0) {
					return null;
				} else if (cnt.indexOf(' ') == -1) {
					return this.factory.toPlainToken(cnt);
				} else {
					return this.factory.toQuoteToken(cnt);
				}
			} else if (first == '(' && last == ')') {
				return parse(str.substring(1, str.length() - 1));
			}
		} else {
			return null;
		}
		
		final int colon = str.indexOf(':');
		if (colon > 0 && colon < str.length() - 1) {
			final AToken pt = toToken(str.substring(colon + 1));
			if (pt instanceof PlainToken) {
				return new ModToken((PlainToken)pt, str.substring(0, colon));
			} else {
				return this.factory.toPlainToken(str);
			}
		} else if (str.equalsIgnoreCase("and") || str.equalsIgnoreCase("or")) {
			return null;
		} else {
			return this.factory.toPlainToken(str);
		}
	}
	
	
	public static void main(String[] args) {
		final String sb = "blubb -\"bla blubb\" +author:\"dies hier\" (ist or hat or \"denkt sich\" and so) ein text -mit (\"leeren zeichen\" or title:leerzeichen) \"ne?!  \"";
		//                 01234567 8901234567 890123456789012345 67890123456 789012345678901234567890 123456789012345 678901234567890123456789 0123456 7
		//                 0          1          2         3          4          5         6         7          8          9         0          1
		//System.out.println(findMatching(sb, 24));
		//System.out.println(findMatching(sb, 6));
		
		final PaxleQueryParser pqp = new PaxleQueryParser(new DebugTokenFactory());
		
		System.out.println(pqp.parse(sb).getString());
		
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
