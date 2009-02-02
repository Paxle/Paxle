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
package org.paxle.parser.html.impl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.paxle.util.ac.AhoCorasick;
import org.paxle.util.ac.SearchResult;
import org.paxle.util.buffer.ArrayByteBuffer;

/* deReplace(String,String[]) and entity-lists from YaCy SVN 3802
 * XXX license? */
public class HtmlTools {
	
	//This array contains codes (see http://mindprod.com/jgloss/unicode.html for details) 
	//that will be replaced. To add new codes or patterns, just put them at the end
	//of the list.
	public static final String[] xmlentities = {
		// Ampersands _have_ to be replaced first. If they were replaced later,
		// other replaced characters containing ampersands would get messed up.
		"\u0026","&amp;",      //ampersand
		"\"","&quot;",         //quotation mark
		"'","&apos;",          //apostroph
		"\u003C","&lt;",       //less than
		"\u003E","&gt;",       //greater than
	};
	
	private static final AhoCorasick<byte[]> HTML_ENTITY_TREE = new AhoCorasick<byte[]>(1);
	private static final Charset UTF8 = Charset.forName("UTF-8");
	static {
		// This array contains codes (see http://mindprod.com/jgloss/unicode.html for details) and
		// patterns that will be replaced. To add new codes or patterns, just put them at the end
		// of the list.
		final String[] htmlentities = {
				// named entities
				"\u00A0","&nbsp;",		// no-break space = non-breaking space
				"\u00A1","&iexcl;",		// inverted exclamation mark
				"\u00A2","&cent;",		// cent sign
				"\u00A3","&pound;",		// pound sign
				"\u00A4","&curren;",	// currency sign
				"\u00A5","&yen;",		// yen sign = yuan sign
				"\u00A6","&brvbar;",	// broken bar = broken vertical bar
				"\u00A7","&sect;",		// section sign
				"\u00A8","&uml;",		// diaeresis = spacing diaeresis
				"\u00A9","&copy;",		// copyright sign
				"\u00AA","&ordf;",		// feminine ordinal indicator
				"\u00AB","&laquo;",		// left-pointing double angle quotation mark = left pointing guillemet
				"\u00AC","&not;",		// not sign
				"\u00AD","&shy;",		// soft hyphen = discretionary hyphen
				"\u00AE","&reg;",		// registered sign = registered trade mark sign
				"\u00AF","&macr;",		// macron = spacing macron = overline = APL overbar
				"\u00B0","&deg;",		// degree sign
				"\u00B1","&plusmn;",	// plus-minus sign = plus-or-minus sign
				"\u00B2","&sup2;",		// superscript two = superscript digit two = squared
				"\u00B3","&sup3;",		// superscript three = superscript digit three = cubed
				"\u00B4","&acute;",		// acute accent = spacing acute
				"\u00B5","&micro;",		// micro sign
				"\u00B6","&para;",		// pilcrow sign = paragraph sign
				"\u00B7","&middot;",	// middle dot = Georgian comma = Greek middle dot
				"\u00B8","&cedil;",		// cedilla = spacing cedilla
				"\u00B9","&sup1;",		// superscript one = superscript digit one
				"\u00BA","&ordm;",		// masculine ordinal indicator
				"\u00BB","&raquo;",		// right-pointing double angle quotation mark = right pointing guillemet
				"\u00BC","&frac14;",	// vulgar fraction one quarter = fraction one quarter
				"\u00BD","&frac12;",	// vulgar fraction one half = fraction one half
				"\u00BE","&frac34;",	// vulgar fraction three quarters = fraction three quarters
				"\u00BF","&iquest;",	// inverted question mark = turned question mark
				"\u00C0","&Agrave;",	// latin capital letter A with grave = latin capital letter A grave
				"\u00C1","&Aacute;",	// latin capital letter A with acute
				"\u00C2","&Acirc;",		// latin capital letter A with circumflex
				"\u00C3","&Atilde;",	// latin capital letter A with tilde
				"\u00C4","&Auml;",		// latin capital letter A with diaeresis
				"\u00C5","&Aring;",		// latin capital letter A with ring above = latin capital letter A ring
				"\u00C6","&AElig;",		// latin capital letter AE = latin capital ligature AE
				"\u00C7","&Ccedil;",	// latin capital letter C with cedilla
				"\u00C8","&Egrave;",	// latin capital letter E with grave
				"\u00C9","&Eacute;",	// latin capital letter E with acute
				"\u00CA","&Ecirc;",		// latin capital letter E with circumflex
				"\u00CB","&Euml;",		// latin capital letter E with diaeresis
				"\u00CC","&Igrave;",	// latin capital letter I with grave
				"\u00CD","&Iacute;",	// latin capital letter I with acute
				"\u00CE","&Icirc;",		// latin capital letter I with circumflex
				"\u00CF","&Iuml;",		// latin capital letter I with diaeresis
				"\u00D0","&ETH;",		// latin capital letter ETH
				"\u00D1","&Ntilde;",	// latin capital letter N with tilde
				"\u00D2","&Ograve;",	// latin capital letter O with grave
				"\u00D3","&Oacute;",	// latin capital letter O with acute
				"\u00D4","&Ocirc;",		// latin capital letter O with circumflex
				"\u00D5","&Otilde;",	// latin capital letter O with tilde
				"\u00D6","&Ouml;",		// latin capital letter O with diaeresis
				"\u00D7","&times;",		// multiplication sign
				"\u00D8","&Oslash;",	// latin capital letter O with stroke = latin capital letter O slash
				"\u00D9","&Ugrave;",	// latin capital letter U with grave
				"\u00DA","&Uacute;",	// latin capital letter U with acute
				"\u00DB","&Ucirc;",		// latin capital letter U with circumflex
				"\u00DC","&Uuml;",		// latin capital letter U with diaeresis
				"\u00DD","&Yacute;",	// latin capital letter Y with acute
				"\u00DE","&THORN;",		// latin capital letter THORN
				"\u00DF","&szlig;",		// latin small letter sharp s = ess-zed
				"\u00E0","&agrave;",	// latin small letter a with grave = latin small letter a grave
				"\u00E1","&aacute;",	// latin small letter a with acute
				"\u00E2","&acirc;",		// latin small letter a with circumflex
				"\u00E3","&atilde;",	// latin small letter a with tilde
				"\u00E4","&auml;",		// latin small letter a with diaeresis
				"\u00E5","&aring;",		// latin small letter a with ring above = latin small letter a ring
				"\u00E6","&aelig;",		// latin small letter ae = latin small ligature ae
				"\u00E7","&ccedil;",	// latin small letter c with cedilla
				"\u00E8","&egrave;",	// latin small letter e with grave
				"\u00E9","&eacute;",	// latin small letter e with acute
				"\u00EA","&ecirc;",		// latin small letter e with circumflex
				"\u00EB","&euml;",		// latin small letter e with diaeresis
				"\u00EC","&igrave;",	// latin small letter i with grave
				"\u00ED","&iacute;",	// latin small letter i with acute
				"\u00EE","&icirc;",		// latin small letter i with circumflex
				"\u00EF","&iuml;",		// latin small letter i with diaeresis
				"\u00F0","&eth;",		// latin small letter eth
				"\u00F1","&ntilde;",	// latin small letter n with tilde
				"\u00F2","&ograve;",	// latin small letter o with grave
				"\u00F3","&oacute;",	// latin small letter o with acute
				"\u00F4","&ocirc;",		// latin small letter o with circumflex
				"\u00F5","&otilde;",	// latin small letter o with tilde
				"\u00F6","&ouml;",		// latin small letter o with diaeresis
				"\u00F7","&divide;",	// division sign
				"\u00F9","&ugrave;",	// latin small letter u with grave
				"\u00FA","&uacute;",	// latin small letter u with acute
				"\u00FB","&ucirc;",		// latin small letter u with circumflex
				"\u00FC","&uuml;",		// latin small letter u with diaeresis
				"\u00FD","&yacute;",	// latin small letter y with acute
				"\u00FE","&thorn;",		// latin small letter thorn
				"\u00FF","&yuml;",		// latin small letter y with diaeresis
				"\u0192","&fnof;",		// latin small f with hook = function = florin
				"\u0391","&Alpha;",		// greek capital letter alpha
				"\u0392","&Beta;",		// greek capital letter beta
				"\u0393","&Gamma;",		// greek capital letter gamma
				"\u0394","&Delta;",		// greek capital letter delta
				"\u0395","&Epsilon;",	// greek capital letter epsilon
				"\u0396","&Zeta;",		// greek capital letter zeta
				"\u0397","&Eta;",		// greek capital letter eta
				"\u0398","&Theta;",		// greek capital letter theta
				"\u0399","&Iota;",		// greek capital letter iota
				"\u039A","&Kappa;",		// greek capital letter kappa
				"\u039B","&Lambda;",	// greek capital letter lambda
				"\u039C","&Mu;",		// greek capital letter mu
				"\u039D","&Nu;",		// greek capital letter nu
				"\u039E","&Xi;",		// greek capital letter xi
				"\u039F","&Omicron;",	// greek capital letter omicron
				"\u03A0","&Pi;",		// greek capital letter pi
				"\u03A1","&Rho;",		// greek capital letter rho
				"\u03A3","&Sigma;",		// greek capital letter sigma
				"\u03A4","&Tau;",		// greek capital letter tau
				"\u03A5","&Upsilon;",	// greek capital letter upsilon
				"\u03A6","&Phi;",		// greek capital letter phi
				"\u03A7","&Chi;",		// greek capital letter chi
				"\u03A8","&Psi;",		// greek capital letter psi
				"\u03A9","&Omega;",		// greek capital letter omega
				"\u03B1","&alpha;",		// greek small letter alpha
				"\u03B2","&beta;",		// greek small letter beta
				"\u03B3","&gamma;",		// greek small letter gamma
				"\u03B4","&delta;",		// greek small letter delta
				"\u03B5","&epsilon;",	// greek small letter epsilon
				"\u03B6","&zeta;",		// greek small letter zeta
				"\u03B7","&eta;",		// greek small letter eta
				"\u03B8","&theta;",		// greek small letter theta
				"\u03B9","&iota;",		// greek small letter iota
				"\u03BA","&kappa;",		// greek small letter kappa
				"\u03BB","&lambda;",	// greek small letter lambda
				"\u03BC","&mu;",		// greek small letter mu
				"\u03BD","&nu;",		// greek small letter nu
				"\u03BE","&xi;",		// greek small letter xi
				"\u03BF","&omicron;",	// greek small letter omicron
				"\u03C0","&pi;",		// greek small letter pi
				"\u03C1","&rho;",		// greek small letter rho
				"\u03C2","&sigmaf;",	// greek small letter final sigma
				"\u03C3","&sigma;",		// greek small letter sigma
				"\u03C4","&tau;",		// greek small letter tau
				"\u03C5","&upsilon;",	// greek small letter upsilon
				"\u03C6","&phi;",		// greek small letter phi
				"\u03C7","&chi;",		// greek small letter chi
				"\u03C8","&psi;",		// greek small letter psi
				"\u03C9","&omega;",		// greek small letter omega
				"\u03D1","&thetasym;",	// greek small letter theta symbol
				"\u03D2","&upsih;",		// greek upsilon with hook symbol
				"\u03D6","&piv;",		// greek pi symbol
				"\u2022","&bull;",		// bullet = black small circle
				"\u2026","&hellip;",	// horizontal ellipsis = three dot leader
				"\u2032","&prime;",		// prime = minutes = feet
				"\u2033","&Prime;",		// double prime = seconds = inches
				"\u203E","&oline;",		// overline = spacing overscore
				"\u2044","&frasl;",		// fraction slash
				"\u2118","&weierp;",	// script capital P = power set = Weierstrass p
				"\u2111","&image;",		// blackletter capital I = imaginary part
				"\u211C","&real;",		// blackletter capital R = real part symbol
				"\u2122","&trade;",		// trade mark sign
				"\u2135","&alefsym;",	// alef symbol = first transfinite cardinal
				"\u2190","&larr;",		// leftwards arrow
				"\u2191","&uarr;",		// upwards arrow
				"\u2192","&rarr;",		// rightwards arrow
				"\u2193","&darr;",		// downwards arrow
				"\u2194","&harr;",		// left right arrow
				"\u21B5","&crarr;",		// downwards arrow with corner leftwards = carriage return
				"\u21D0","&lArr;",		// leftwards double arrow
				"\u21D1","&uArr;",		// upwards double arrow
				"\u21D2","&rArr;",		// rightwards double arrow
				"\u21D3","&dArr;",		// downwards double arrow
				"\u21D4","&hArr;",		// left right double arrow
				"\u2200","&forall;",	// for all
				"\u2202","&part;",		// partial differential
				"\u2203","&exist;",		// there exists
				"\u2205","&empty;",		// empty set = null set = diameter
				"\u2207","&nabla;",		// nabla = backward difference
				"\u2208","&isin;",		// element of
				"\u2209","&notin;",		// not an element of
				"\u220B","&ni;",		// contains as member
				"\u220F","&prod;",		// n-ary product = product sign
				"\u2211","&sum;",		// n-ary sumation
				"\u2212","&minus;",		// minus sign
				"\u2217","&lowast;",	// asterisk operator
				"\u221A","&radic;",		// square root = radical sign
				"\u221D","&prop;",		// proportional to
				"\u221E","&infin;",		// infinity
				"\u2220","&ang;",		// angle
				"\u2227","&and;",		// logical and = wedge
				"\u2228","&or;",		// logical or = vee
				"\u2229","&cap;",		// intersection = cap
				"\u222A","&cup;",		// union = cup
				"\u222B","&int;",		// integral
				"\u2234","&there4;",	// therefore
				"\u223C","&sim;",		// tilde operator = varies with = similar to
				"\u2245","&cong;",		// approximately equal to
				"\u2248","&asymp;",		// almost equal to = asymptotic to
				"\u2260","&ne;",		// not equal to
				"\u2261","&equiv;",		// identical to
				"\u2264","&le;",		// less-than or equal to
				"\u2265","&ge;",		// greater-than or equal to
				"\u2282","&sub;",		// subset of
				"\u2283","&sup;",		// superset of
				"\u2284","&nsub;",		// not a subset of
				"\u2286","&sube;",		// subset of or equal to
				"\u2287","&supe;",		// superset of or equal to
				"\u2295","&oplus;",		// circled plus = direct sum
				"\u2297","&otimes;",	// circled times = vector product
				"\u22A5","&perp;",		// up tack = orthogonal to = perpendicular
				"\u22C5","&sdot;",		// dot operator
				"\u2308","&lceil;",		// left ceiling = apl upstile
				"\u2309","&rceil;",		// right ceiling
				"\u230A","&lfloor;",	// left floor = apl downstile
				"\u230B","&rfloor;",	// right floor
				"\u2329","&lang;",		// left-pointing angle bracket = bra
				"\u232A","&rang;",		// right-pointing angle bracket = ket
				"\u25CA","&loz;",		// lozenge
				"\u2660","&spades;",	// black spade suit
				"\u2663","&clubs;",		// black club suit = shamrock
				"\u2665","&hearts;",	// black heart suit = valentine
				"\u2666","&diams;",		// black diamond suit
				"\u0152","&OElig;",		// latin capital ligature OE
				"\u0153","&oelig;",		// latin small ligature oe
				"\u0160","&Scaron;",	// latin capital letter S with caron
				"\u0161","&scaron;",	// latin small letter s with caron
				"\u0178","&Yuml;",		// latin capital letter Y with diaeresis
				"\u02C6","&circ;",		// modifier letter circumflex accent
				"\u02DC","&tilde;",		// small tilde
				"\u2002","&ensp;",		// en space
				"\u2003","&emsp;",		// em space
				"\u2009","&thinsp;",	// thin space
				"\u200C","&zwnj;",		// zero width non-joiner
				"\u200D","&zwj;",		// zero width joiner
				"\u200E","&lrm;",		// left-to-right mark
				"\u200F","&rlm;",		// right-to-left mark
				"\u2013","&ndash;",		// en dash
				"\u2014","&mdash;",		// em dash
				"\u2018","&lsquo;",		// left single quotation mark
				"\u2019","&rsquo;",		// right single quotation mark
				"\u201A","&sbquo;",		// single low-9 quotation mark
				"\u201C","&ldquo;",		// left double quotation mark
				"\u201D","&rdquo;",		// right double quotation mark
				"\u201E","&bdquo;",		// double low-9 quotation mark
				"\u2020","&dagger;",	// dagger
				"\u2021","&Dagger;",	// double dagger
				"\u2030","&permil;",	// per mille sign
				"\u2039","&lsaquo;",	// single left-pointing angle quotation mark
				"\u203A","&rsaquo;",	// single right-pointing angle quotation mark
				"\u20AC","&euro;",		// euro sign
		};
		for (int i=0; i<htmlentities.length; i+=2) {
			final ByteBuffer bb1 = UTF8.encode(htmlentities[i+1]);
			final ByteBuffer bb0 = UTF8.encode(htmlentities[i]);
			HTML_ENTITY_TREE.addPattern(
					bb1.array(), 0, bb1.limit(),
					ArrayByteBuffer.wrap(bb0.array(), bb0.limit()).toByteArray());
		}
		HTML_ENTITY_TREE.createFailTransitions();
	}
	
	/*
	public static void main(String[] args) throws Exception {
		final File file = new File("/home/kane/entities");
		final BufferedReader br = new BufferedReader(new FileReader(file));
		
		final CharBuffer cb = CharBuffer.allocate((int)file.length());
		br.read(cb);
		br.close();
		
		final Pattern pattern = Pattern.compile("<!ENTITY\\s+([a-zA-Z0-9]+)[^-]+--([^,]+),\\s+U\\+([A-Z0-9]{4})[^-]+-->");
		cb.flip();
		final Matcher m = pattern.matcher(cb.toString().replace('\n', ' '));
		while (m.find()) {
			System.out.println("\"\\u" + m.group(3) + "\",\"&" + m.group(1) + ";\",\t//" + m.group(2).replaceAll("\\s+", " "));
		}
	}
	*/
	
	public static String deReplaceHTML(String text) {
		if (text != null && text.indexOf('&') >= 0) {
			text = deReplaceNumericEntities(text);
			text = deReplaceHTMLEntities(text);
			text = deReplaceXMLEntities(text);
		}
		return text;
	}
	
	public static String deReplaceHTMLEntities(String text) {
		// return deReplace(text, htmlentities);
		
		int last = 0;
		final ByteBuffer bb = UTF8.encode(text);		// creates a HeapByteBuffer which uses a backing array
		final byte[] data = bb.array();
		final int len = bb.limit();
		final ArrayByteBuffer abb = new ArrayByteBuffer(len);
		for (final SearchResult<byte[]> r : HTML_ENTITY_TREE.search(data, 0, len)) {
			abb.append(data, last, r.getMatchBegin() - last).append(r.getValue());
			last = r.getMatchEnd() + 1;
		}
		
		if (last > 0) {
			return abb.append(data, last, len - last).toString(UTF8);
		} else {
			return text;
		}
	}
	
	public static String deReplaceXMLEntities(String text) {
		return deReplace(text, xmlentities);
	}
	
	public static String deReplace(String text, String[] entities) {
		if (text == null) return null;
		final StringBuffer sb = new StringBuffer(text);
		for (int i=entities.length-1; i>0; i-=2) {
			int p = 0;
			while ((p = sb.indexOf(entities[i])) >= 0) {
				// text = text.substring(0, p) + entities[i - 1] + text.substring(p + entities[i].length());
				sb.replace(p, p + entities[i].length(), entities[i - 1]);
				p += entities[i - 1].length();
			}
		}
		return sb.toString();
	}
	
	private static final Pattern NumericEntityPattern = Pattern.compile("&#((\\d+)|(x)([0-9a-fA-F]{4}));");
	
	// TODO: still fails at constructs like '&#x0301;e' - a possible representation of '&eacute;' respectively '&#233;'
	// 26.02.2008: [FB]
	// - added support for character-replacements requiring more than one char to represent
	// [FB]
	public static String deReplaceNumericEntities(String text) {
		if (text == null) return null;
		if (text.length() == 0) return text;
		final Matcher m = NumericEntityPattern.matcher(text);
		final StringBuffer sb = new StringBuffer(text.length());
		final char[] cbuf = new char[2];
		while (m.find()) {
			String repl;
			final int radix;
			if (m.group(3) == null) {
				radix = 10;
				repl = m.group(2);
			} else {
				radix = 16;
				repl = m.group(4);
			}
			
			final int nChars = Character.toChars(Integer.parseInt(repl, radix), cbuf, 0);
			repl = String.valueOf(cbuf, 0, nChars);
			m.appendReplacement(sb, Matcher.quoteReplacement(repl));
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
