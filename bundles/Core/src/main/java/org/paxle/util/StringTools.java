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
package org.paxle.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

public class StringTools {
	
	/**
	 * Splits the given {@link String} at each of the characters defined in the second argument,
	 * obeying single and double quotes.
	 * @see #quoteSplit(Collection, String, String)
	 * @param s the {@link String} to split
	 * @param splitChars a {@link String} of which each character is a symbol to perform a split upon
	 * @return the resulting parts of the split-operation on the given {@link String} <code>s</code>
	 * @throws ParseException if the quotes are not matched, i.e. an opening but no closing quote is discovered.
	 */
	public static String[] quoteSplit(final String s, final String splitChars) throws ParseException {
		final ArrayList<String> r = new ArrayList<String>();
		quoteSplit(r, s, splitChars);
		return r.toArray(new String[r.size()]);
	}
	
	/**
	 * Behaves like {@link String#split(String)}, but obeys single and double quotes (<code>&apos;</code>, <code>&quot;</code>).
	 * Quotes are treated as stacking markers, no splitting is performed any quotes of the same kind.
	 * For example the following string, splitted at the comma (<code>,</code>):
	 * <pre>
	 *   key1="value1.0,value1.1", key2='key2.0="value2.0.0,value2.0.1",key2.1=value2.1'
	 * </pre>
	 * would result in the following strings:
	 * <ul>
	 *   <li><code>key1="value1.0,value1.1"</code></li>
	 *   <li><code>key2='key2.0="value2.0.0,value2.0.1",key2.1=value2.1'</code></li>
	 * </ul>
	 * <p>Note that splitting on quotes is not supported.
	 * @param collection the {@link Collection} to add the splitted parts to
	 * @param s the {@link String} to split
	 * @param splitChars a {@link String} of which each character is a symbol to perform a split upon
	 * @throws ParseException if the quotes are not matched, i.e. an opening but no closing quote is discovered.
	 */
	public static void quoteSplit(final Collection<String> collection, final String s, final String splitChars) throws ParseException {
		// split the value at whitespace, but obey single and double quotes
		int level = 0;
		char lastLeveled = 0;
		int last = 0;
		
		for (int i=0; i<s.length(); i++) {
			final char c = s.charAt(i);
			if (c == '"' || c == '\'') {
				if (lastLeveled == c) {
					level--;
					lastLeveled = (level == 0) ? 0 : (c == '"') ? '\'' : '"';
				} else {
					level++;
					lastLeveled = c;
				}
			} else if (level == 0 && splitChars.indexOf(c) >= 0) {
				if (last < i)
					collection.add(s.substring(last, i));
				last = i + 1;
			}
		}
		
		if (level != 0)
			throw new ParseException("unmatched " + ((lastLeveled == '"') ? "double" : "single") + " quote", last);
		if (last < s.length() - 1)
			collection.add(s.substring(last));
	}
}
