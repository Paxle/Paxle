package org.paxle.se.query;

import java.util.LinkedList;
import java.util.List;

import org.paxle.se.query.tokens.AndOperator;
import org.paxle.se.query.tokens.ModToken;
import org.paxle.se.query.tokens.Operator;
import org.paxle.se.query.tokens.OrOperator;
import org.paxle.se.query.tokens.PlainToken;
import org.paxle.se.query.tokens.QuoteToken;

public class PaxleQueryParser {
	
	private static String[] lex(String query) {
		final LinkedList<String> tokens = new LinkedList<String>();
		query = query.trim();
		int last = -1, loff, t;
		while (last < query.length()) {
			loff = last + 1;
			char first = 0;
			// eat up spaces
			while (loff < query.length() && (first = query.charAt(loff)) == ' ')
				loff++;
			
			last = (first == '(' && (t = findMatching(query, loff)) > -1 ||
					(loff + 1 < query.length() && first == '"' && (t = findMatching(query, loff + 1)) > -1)
			) ? t + 1 : ((t = query.indexOf(' ', loff)) > -1) ? t : query.length();
			
			final String text = query.substring(loff, last).trim();
			if (text.length() > 0)
				tokens.add(text);
		}
		return tokens.toArray(new String[tokens.size()]);
	}
	
	private static int findMatching(String str, int start) {
		char c = str.charAt(start);
		boolean bnq = (c == '(');
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
	
	private static String[][] categorizeAndOrTokens(String[] tokens) {
		final List<String> andts = new LinkedList<String>();
		final List<String[]> orts = new LinkedList<String[]>();
		for (final String t : tokens) {
			if (t.equalsIgnoreCase("and")) {
				continue;
			} else if (t.equalsIgnoreCase("or")) {
				if (andts.size() > 0) {
					orts.add(andts.toArray(new String[andts.size()]));
					andts.clear();
				}
				continue;
			} else {
				andts.add(t);
			}
		}
		if (andts.size() > 0)
			orts.add(andts.toArray(new String[andts.size()]));
		return orts.toArray(new String[orts.size()][]);
	}
	
	public static IToken parse(String query) {
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
				final Operator or = new OrOperator();
				for (String[] andts : aots)
					or.addToken(and(andts));
				return or;
			}
		}
	}
	
	private static IToken and(String[] tokens) {
		if (tokens.length == 0) {
			return null;
		} else if (tokens.length == 1) {
			return toToken(tokens[0]);
		} else {
			final AndOperator and = new AndOperator();
			for (final String t : tokens)
				and.addToken(parse(t));
			return and;
		}
	}
	
	private static IToken toToken(String str) {
		if (str.length() > 1) {
			final char first = str.charAt(0);
			final char last = str.charAt(str.length() - 1);
			if (first == '"' && last == '"') {
				final String cnt = str.substring(1, str.length() - 1).trim();
				return (cnt.length() > 0) ? new QuoteToken(cnt) : null;
			} else if (first == '(' && last == ')') {
				return parse(str.substring(1, str.length() - 1));
			}
		} else {
			return null;
		}
		
		final int colon = str.indexOf(':');
		if (colon > 0 && colon < str.length() - 1) {
			final IToken pt = toToken(str.substring(colon + 1));
			if (pt instanceof PlainToken) {
				return new ModToken((PlainToken)pt, str.substring(0, colon));
			} else {
				return new PlainToken(str);
			}
		} else if (str.equalsIgnoreCase("and") || str.equalsIgnoreCase("or")) {
			return null;
		} else {
			return new PlainToken(str);
		}
	}
	
	public static void main(String[] args) {
		final String sb = "author:dies (ist or hat or \"denkt sich\" and so) ein text mit (\"leeren zeichen\" or title:leerzeichen) \"ne?!  \"";
		//                 012345678901234567890123456 7890123456 78901234567890123 4567890 123456789012345 678901234567890123456789 012345678
		//                 0         1         2          3          4         5          6          7          8         9          0
		//System.out.println(findMatching(sb, 24));
		//System.out.println(findMatching(sb, 6));
		System.out.println(parse(sb));
		/*
		String[] ss = lex(sb);
		int c = 0;
		for (String s : ss) {
			Token t = toToken(s);
			System.out.println(c++ + ": " + t.toString());
		}*/
		
		//PaxleQueryParser pqp = new PaxleQueryParser(new TF());
		//final Expression sse = pqp.parse(sb);
		
		//System.out.println(sse.toString());
	}
}
